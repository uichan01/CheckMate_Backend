"""Run four 100-job recovery experiments against dedicated disposable containers.

Prerequisite: gradlew -I scripts/recovery/recovery.init.gradle writeRecoveryClasspath
Containers: checkmate-recovery-mysql (13317), checkmate-recovery-redis (16389).
Only these containers and recovery_* databases are accessed. Existing data is not used.
"""
import collections
import datetime
import json
import pathlib
import subprocess
import time

ROOT = pathlib.Path(__file__).resolve().parents[2]
OUT = ROOT / "docs" / "recovery-test-evidence" / datetime.datetime.now().strftime("%Y%m%d-%H%M%S")
OUT.mkdir(parents=True, exist_ok=False)
CP = (ROOT / "build/recovery/classpath.txt").read_text()
JAVA_ARGS = OUT / "java-classpath.args"
# Java argument files avoid Windows' command-line length limit.
JAVA_ARGS.write_text('-cp\n"' + CP.replace('\\', '/') + '"\n', encoding="utf-8")
MYSQL = "checkmate-recovery-mysql"
REDIS = "checkmate-recovery-redis"
CHILDREN = []


def run(*args, check=True):
    return subprocess.run(args, capture_output=True, text=True, encoding="utf-8",
                          errors="replace", check=check, timeout=30)


def sql(db, query):
    return run("docker", "exec", MYSQL, "mysql", "-uroot", "-precovery-only",
               "-N", "-B", db, "-e", query).stdout.strip()


def redis(*args):
    result = run("docker", "exec", REDIS, "redis-cli", "--json", *args)
    if result.stdout.startswith("error:"):
        return "error:" + json.loads(result.stdout[6:])
    return json.loads(result.stdout)


def last_delivered_id():
    group = redis("XINFO", "GROUPS", "ai-feedback-stream")[0]
    if isinstance(group, list):
        group = dict(zip(group[::2], group[1::2]))
    return group["last-delivered-id"]


def wait_for(fn, timeout=90):
    end = time.monotonic() + timeout
    while time.monotonic() < end:
        result = fn()
        if result:
            return result
        time.sleep(.4)
    raise TimeoutError("Condition did not become true")


def start(mode, db, label):
    logfile = OUT / (label + ".log")
    calls = OUT / (label + "-calls.txt")
    handle = logfile.open("w", encoding="utf-8")
    process = subprocess.Popen(["java", "-Drecovery.hold=" + str(mode == "hold").lower(),
        "-Drecovery.check-leases=" + str(label == "outbox_before_publish-seed").lower(),
        "-Drecovery.calls=" + str(calls), "@" + str(JAVA_ARGS), "recovery.RecoveryWorker", mode, db],
        cwd=ROOT, stdout=handle, stderr=subprocess.STDOUT)
    CHILDREN.append((process, handle))

    def ready():
        if process.poll() is not None:
            raise RuntimeError(f"{label} JVM exited ({process.returncode}); see {logfile}")
        return "RECOVERY_READY " in logfile.read_text(encoding="utf-8", errors="replace")
    wait_for(ready, 120)
    return process


def kill(process):
    # Windows TerminateProcess: no Spring shutdown callback or exception-based retry.
    process.kill()
    process.wait(timeout=15)


def snapshot(db):
    states = dict(line.split("\t") for line in sql(db,
        "SELECT status, COUNT(*) FROM task_submission_ai_feedbacks GROUP BY status;").splitlines())
    outbox = dict(line.split("\t") for line in sql(db,
        "SELECT status, COUNT(*) FROM ai_feedback_outbox GROUP BY status;").splitlines())
    pending = redis("XPENDING", "ai-feedback-stream", "ai-feedback-group")
    if isinstance(pending, str):
        pending = {"group_not_created": pending}
    return {"timestamp": datetime.datetime.now().astimezone().isoformat(),
        "submissions": int(sql(db, "SELECT COUNT(*) FROM task_submissions;")),
        "feedback": {k: int(states.get(k, 0)) for k in ["PENDING", "PROCESSING", "COMPLETED", "FAILED"]},
        "outbox": {k: int(outbox.get(k, 0)) for k in ["PENDING", "PUBLISHED"]},
        "stream_length": redis("XLEN", "ai-feedback-stream"), "redis_pending": pending}


def details(db, record):
    record["redis_pending_details"] = redis("XPENDING", "ai-feedback-stream", "ai-feedback-group", "-", "+", "100")
    record["feedback_rows"] = sql(db, "SELECT id, submission_id, status, started_at, completed_at FROM task_submission_ai_feedbacks ORDER BY id;")
    record["nonempty_results"] = int(sql(db, "SELECT COUNT(*) FROM task_submission_ai_feedbacks WHERE strength IS NOT NULL AND weakness IS NOT NULL AND suggestion IS NOT NULL;"))


def prepare(db):
    # Unique schema per scenario and run; no DROP DATABASE or shared-service cleanup.
    sql("mysql", f"CREATE DATABASE {db};")
    # This is a dedicated test-only Redis container; only this experiment's stream is reset.
    redis("DEL", "ai-feedback-stream")
    entry = redis("XADD", "ai-feedback-stream", "*", "bootstrap", "1")
    redis("XDEL", "ai-feedback-stream", entry)


results = {"started_at": datetime.datetime.now().astimezone().isoformat(),
           "git_head": run("git", "rev-parse", "HEAD").stdout.strip(),
           "jobs_per_scenario": 100, "recovery_settings_ms": {"idle": 3000, "lease": 6000, "heartbeat": 1000},
           "scenarios": []}
try:
    wait_for(lambda: run("docker", "exec", MYSQL, "mysqladmin", "ping", "-uroot",
                        "-precovery-only", check=False).returncode == 0, 120)
    for scenario in ["outbox_before_publish", "stream_before_receive", "during_processing", "active_lease"]:
        db = "recovery_" + str(int(time.time())) + "_" + str(len(results["scenarios"]))
        record = {"name": scenario, "database": db}
        results["scenarios"].append(record)
        print("START " + scenario, flush=True)
        prepare(db)
        seed = start("seed", db, scenario + "-seed")
        record["after_seed"] = snapshot(db)
        kill(seed)
        if scenario != "outbox_before_publish":
            publisher = start("publish", db, scenario + "-publish")
            record["before_kill"] = snapshot(db)
            kill(publisher)
        if scenario in ("during_processing", "active_lease"):
            worker = start("hold", db, scenario + "-hold")
            wait_for(lambda: int(sql(db, "SELECT COUNT(*) FROM task_submission_ai_feedbacks WHERE status='PROCESSING';")) == 2)
            record["before_kill"] = snapshot(db)
            held_worker = worker
            if scenario == "during_processing":
                kill(worker)
        elif scenario == "outbox_before_publish":
            record["before_kill"] = record["after_seed"]
        record["after_kill"] = snapshot(db)
        launch = time.monotonic()
        try:
            worker = start("recover", db, scenario + "-recover")
        except RuntimeError as error:
            record["restart_error"] = str(error)
            record["final"] = snapshot(db)
            details(db, record)
            print("RESTART FAILED " + scenario, flush=True)
            continue
        ready = time.monotonic()
        record["restart_ready_seconds"] = round(ready - launch, 3)
        if scenario == "active_lease":
            # Another live JVM is processing two held AI calls; recovery must leave those alone.
            wait_for(lambda: int(sql(db, "SELECT COUNT(*) FROM task_submission_ai_feedbacks WHERE status='COMPLETED';")) == 98, 90)
            time.sleep(8)  # Longer than the 6s lease while heartbeats are still running.
            record["while_original_alive"] = snapshot(db)
            record["active_attempts"] = sql(db, "SELECT id, processing_attempts FROM task_submission_ai_feedbacks WHERE status='PROCESSING' ORDER BY id;")
            assert record["while_original_alive"]["feedback"] == dict(PENDING=0, PROCESSING=2, COMPLETED=98, FAILED=0)
            assert record["active_attempts"].splitlines() == ["1\t1", "2\t1"]
            kill(held_worker)
            record["after_original_kill"] = snapshot(db)
        target = 100
        wait_for(lambda: int(sql(db, "SELECT COUNT(*) FROM task_submission_ai_feedbacks WHERE status='COMPLETED';")) == target, 90)
        record["target_reached_seconds_from_launch"] = round(time.monotonic() - launch, 3)
        record["target_reached_seconds_from_ready"] = round(time.monotonic() - ready, 3)
        record["after_drain"] = snapshot(db)
        record["final"] = snapshot(db)
        details(db, record)
        assert record["final"]["feedback"] == dict(PENDING=0, PROCESSING=0, COMPLETED=100, FAILED=0)
        wait_for(lambda: redis("XPENDING", "ai-feedback-stream", "ai-feedback-group")[0] == 0)
        record["final"] = snapshot(db)
        record["attempt_counts"] = sql(db, "SELECT processing_attempts, COUNT(*) FROM task_submission_ai_feedbacks GROUP BY processing_attempts ORDER BY processing_attempts;")
        if scenario == "during_processing":
            # Redelivery after completion must ACK without another AI call.
            payloads = sql(db, "SELECT payload FROM ai_feedback_outbox ORDER BY id;").splitlines()
            for payload in payloads:
                last_duplicate = redis("XADD", "ai-feedback-stream", "*", "payload", payload)
            wait_for(lambda: last_delivered_id() == last_duplicate)
            wait_for(lambda: redis("XPENDING", "ai-feedback-stream", "ai-feedback-group")[0] == 0)
            record["after_100_completed_duplicates"] = snapshot(db)
        kill(worker)
        calls = (OUT / (scenario + "-recover-calls.txt")).read_text().splitlines()
        record["recovery_ai_calls"] = len(calls)
        record["recovery_unique_submissions"] = len(set(calls))
        record["duplicate_call_titles"] = {k: v for k, v in collections.Counter(calls).items() if v > 1}
        assert record["recovery_ai_calls"] == record["recovery_unique_submissions"] == 100
        print(json.dumps({"name": scenario, "final": record["final"]}, ensure_ascii=False), flush=True)
    assert all("restart_error" not in record for record in results["scenarios"]), "At least one JVM failed to restart"
except Exception as error:
    results["execution_error"] = repr(error)
    raise
finally:
    for process, handle in CHILDREN:
        if process.poll() is None:
            kill(process)
        handle.close()
    results["finished_at"] = datetime.datetime.now().astimezone().isoformat()
    (OUT / "results.json").write_text(json.dumps(results, indent=2, ensure_ascii=False), encoding="utf-8")
    print("EVIDENCE " + str(OUT), flush=True)
