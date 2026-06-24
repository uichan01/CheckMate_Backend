package recovery;

import com.CheckMate.checkmate_server._common.service.S3FileUploadService;
import com.CheckMate.checkmate_server.study.task.ai.domain.TaskAiFeedbackEntity;
import com.CheckMate.checkmate_server.study.task.ai.dto.*;
import com.CheckMate.checkmate_server.study.task.ai.outbox.service.*;
import com.CheckMate.checkmate_server.study.task.ai.repository.TaskAiFeedbackRepository;
import com.CheckMate.checkmate_server.study.task.ai.service.*;
import com.CheckMate.checkmate_server.study.task.domain.*;
import com.CheckMate.checkmate_server.study.task.repository.TaskSubmissionAttachmentRepository;
import com.CheckMate.checkmate_server.user.domain.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.*;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.context.ConfigurableApplicationContext;
import com.CheckMate.checkmate_server.study.task.ai.outbox.repository.AiFeedbackOutboxRepository;
import com.CheckMate.checkmate_server.study.task.ai.domain.AiFeedbackStatus;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/** Isolated process harness: production repositories, publisher and consumer, fake AI only. */
public class RecoveryWorker {
    @Configuration(proxyBeanMethods = false)
    @EnableAutoConfiguration
    @EntityScan("com.CheckMate.checkmate_server")
    @EnableJpaRepositories("com.CheckMate.checkmate_server")
    @Import({AiFeedbackProducer.class, AiFeedbackOutboxService.class})
    static class Base {
        @Bean ObjectMapper objectMapper() { return new ObjectMapper(); }
        @Bean S3FileUploadService s3() { return org.mockito.Mockito.mock(S3FileUploadService.class); }
        @Bean AiService ai() {
            return new AiService("unused-test-key", "fake") {
                @Override public AiFeedbackResult generateFeedback(String a, String b, String title,
                        String d, List<String> urls, String text) {
                    try {
                        Files.writeString(Path.of(System.getProperty("recovery.calls")), title + "\n",
                                StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                        if (Boolean.getBoolean("recovery.hold")) new CountDownLatch(1).await();
                        Thread.sleep(100);
                        return new ObjectMapper().readValue(
                                "{\"strength\":\"test strength\",\"weakness\":\"test weakness\",\"suggestion\":\"test suggestion\"}",
                                AiFeedbackResult.class);
                    } catch (Exception e) { throw new RuntimeException(e); }
                }
            };
        }
    }

    @Configuration(proxyBeanMethods = false)
    @EnableScheduling
    @Import(AiFeedbackOutboxPublisher.class)
    static class Publishing {}

    @Configuration(proxyBeanMethods = false)
    @Import(AiFeedbackConsumer.class)
    static class Consuming {}

    public static void main(String[] args) throws Exception {
        String mode = args[0];
        var builder = new SpringApplicationBuilder(Base.class).web(WebApplicationType.NONE);
        if (mode.equals("recover")) builder.sources(Publishing.class, Consuming.class);
        if (mode.equals("hold")) builder.sources(Consuming.class);
        // Do not read the developer's application settings or contact external services.
        var context = builder.run(
                "--spring.config.location=optional:classpath:/recovery-isolated.properties",
                "--spring.datasource.url=jdbc:mysql://127.0.0.1:13317/" + args[1] + "?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC",
                "--spring.datasource.username=root", "--spring.datasource.password=recovery-only",
                "--spring.jpa.hibernate.ddl-auto=" + (mode.equals("seed") ? "create-only" : "validate"),
                "--cloud.aws.s3.bucket=unused-recovery-test",
                "--spring.data.redis.host=127.0.0.1", "--spring.data.redis.port=16389",
                "--spring.data.redis.repositories.enabled=false", "--spring.jpa.open-in-view=false",
                "--ai.feedback.recovery.idle-ms=3000", "--ai.feedback.recovery.lease-ms=6000",
                "--ai.feedback.recovery.heartbeat-ms=1000",
                "--logging.level.root=WARN", "--logging.level.com.CheckMate.checkmate_server.study.task.ai=INFO");
        if (mode.equals("seed")) {
            var em = context.getBean(EntityManager.class);
            var tx = new TransactionTemplate(context.getBean(PlatformTransactionManager.class));
            Long[] ids = tx.execute(s -> {
                var user = new UserEntity("recovery@test.invalid", "unused", "", "recovery", UserRole.ROLE_USER);
                em.persist(user);
                var task = TaskEntity.builder().userEntity(user).title("Recovery test")
                        .content("Synthetic fixture").dueDate(LocalDateTime.now().plusDays(1)).build();
                em.persist(task);
                return new Long[]{user.getUserId(), task.getTaskId()};
            });
            for (int i = 1; i <= 100; i++) {
                final int number = i;
                tx.executeWithoutResult(s -> {
                    var submission = TaskSubmissionEntity.builder().title("submission-" + number)
                            .content("Synthetic submission " + number)
                            .userEntity(em.getReference(UserEntity.class, ids[0]))
                            .taskEntity(em.getReference(TaskEntity.class, ids[1])).build();
                    em.persist(submission);
                    var feedback = TaskAiFeedbackEntity.builder().taskSubmissionEntity(submission).build();
                    em.persist(feedback);
                    context.getBean(AiFeedbackOutboxService.class).enqueue(new AiFeedbackMessage(
                            feedback.getId(), submission.getSubmissionId(), "Recovery test", "Synthetic task",
                            submission.getTitle(), submission.getContent(), List.of()));
                });
            }
            if (Boolean.getBoolean("recovery.check-leases")) verifyLeases(context);
        }
        if (mode.equals("publish")) {
            // Single transaction, same production publisher; do not start consumers yet.
            var factory = context.getAutowireCapableBeanFactory();
            var publisher = factory.createBean(AiFeedbackOutboxPublisher.class);
            new TransactionTemplate(context.getBean(PlatformTransactionManager.class))
                    .executeWithoutResult(s -> publisher.publishPending());
        }
        System.out.println("RECOVERY_READY " + mode);
        System.out.flush();
        new CountDownLatch(1).await();
    }

    private static void verifyLeases(ConfigurableApplicationContext context) throws Exception {
        var repo = context.getBean(TaskAiFeedbackRepository.class);
        var outbox = context.getBean(AiFeedbackOutboxService.class);
        var outboxRepo = context.getBean(AiFeedbackOutboxRepository.class);
        var tx = new TransactionTemplate(context.getBean(PlatformTransactionManager.class));
        var em = context.getBean(EntityManager.class);
        // Logical future timestamps make expiry assertions deterministic without sleeping.
        var now = LocalDateTime.now().plusHours(1);
        check(repo.claimAvailable(1L, "owner-a", now, now.plusSeconds(6), now.minusSeconds(6), 4) == 1, "first claim");
        check(repo.claimAvailable(1L, "owner-b", now, now.plusSeconds(6), now.minusSeconds(6), 4) == 0, "active owner protected");
        check(repo.renewLease(1L, "owner-b", now, now.plusSeconds(20)) == 0, "wrong heartbeat rejected");
        check(repo.renewLease(1L, "owner-a", now.plusSeconds(1), now.plusSeconds(20)) == 1, "heartbeat extends lease");
        check(repo.claimAvailable(1L, "owner-b", now.plusSeconds(7), now.plusSeconds(27), now, 4) == 0, "renewed lease protected");
        check(repo.renewLease(1L, "owner-a", now.plusSeconds(21), now.plusSeconds(30)) == 0, "expired owner cannot revive");
        check(repo.claimAvailable(1L, "owner-b", now.plusSeconds(21), now.plusSeconds(27), now, 4) == 1, "expired lease reclaimed");
        check(repo.completeOwned(1L, "owner-a", now.plusSeconds(22), "stale", "stale", "stale") == 0, "stale completion rejected");
        check(repo.failOwned(1L, "owner-a", now.plusSeconds(22), "stale") == 0, "stale failure rejected");
        var message = new AiFeedbackMessage(1L, 1L, "task", "", "submission-1", "", List.of());
        check(!outbox.enqueueRetry(message.nextRetry(), "owner-a"), "stale retry rejected");
        check(outboxRepo.count() == 100, "stale retry creates no outbox");

        var brokenMapper = org.mockito.Mockito.mock(ObjectMapper.class);
        org.mockito.Mockito.when(brokenMapper.writeValueAsString(org.mockito.ArgumentMatchers.any()))
                .thenThrow(new com.fasterxml.jackson.core.JsonProcessingException("injected serialization failure") {});
        var failingOutbox = new AiFeedbackOutboxService(outboxRepo, brokenMapper, repo);
        boolean rolledBack = false;
        try {
            tx.executeWithoutResult(s -> failingOutbox.enqueueRetry(message.nextRetry(), "owner-b"));
        } catch (IllegalStateException expected) { rolledBack = true; }
        check(rolledBack && repo.findById(1L).orElseThrow().getStatus() == AiFeedbackStatus.PROCESSING,
                "retry publication failure rolls DB transition back");
        check(outboxRepo.count() == 100, "retry rollback keeps outbox count");
        check(repo.completeOwned(1L, "owner-b", now.plusSeconds(22), "ok", "ok", "ok") == 1, "current owner completes");
        check(repo.claimAvailable(1L, "owner-c", now.plusSeconds(30), now.plusSeconds(36), now, 4) == 0, "completed cannot be reclaimed");
        tx.executeWithoutResult(s -> em.find(TaskAiFeedbackEntity.class, 1L).cleanUp());
        for (int i = 0; i < 4; i++) {
            var at = now.plusSeconds(i * 10L);
            check(repo.claimAvailable(1L, "attempt-" + i, at, at.plusSeconds(6), now.minusSeconds(6), 4) == 1, "bounded claim " + i);
        }
        check(repo.claimAvailable(1L, "attempt-5", now.plusSeconds(50), now.plusSeconds(56), now, 4) == 0, "fifth attempt rejected");
        check(repo.failExhausted(1L, now.plusSeconds(50), now, 4) == 1, "exhausted job fails");
        check(repo.findById(1L).orElseThrow().getStatus() == AiFeedbackStatus.FAILED, "failure persisted");
        tx.executeWithoutResult(s -> em.find(TaskAiFeedbackEntity.class, 1L).cleanUp());
        System.out.println("LEASE_ASSERTIONS_PASSED active lease, stale owner, rollback, completion, attempt limit");
    }

    private static void check(boolean condition, String name) {
        if (!condition) throw new AssertionError(name);
    }
}
