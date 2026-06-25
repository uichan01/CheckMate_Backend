package com.CheckMate.checkmate_server.study.task.ai.service;

import com.CheckMate.checkmate_server.study.task.ai.dto.AiFeedbackResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class AiService {

    private static final String SYSTEM_PROMPT = """
            당신은 스터디 과제 첨삭 도우미입니다.
            제출된 과제물을 분석하여 반드시 아래 JSON 형식으로만 응답하세요.
            {
              "strength": "잘한 점을 2-3문장으로 작성",
              "weakness": "부족한 점을 2-3문장으로 작성",
              "suggestion": "개선 방향을 2-3문장으로 작성"
            }
            """;

    private final RestClient restClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String model;

    public AiService(
            @Value("${openai.api-key}") String apiKey,
            @Value("${openai.model:gpt-4o-mini}") String model) {
        this.model = model;
        this.restClient = RestClient.builder()
                .baseUrl("https://gms.ssafy.io/gmsapi/api.openai.com/v1")
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .build();
    }

    public AiFeedbackResult generateFeedback(
            String taskTitle, String taskContent,
            String submissionTitle, String submissionContent,
            List<String> attachmentUrls,
            String attachmentText) {

        String attachmentUrlText = attachmentUrls == null || attachmentUrls.isEmpty()
                ? ""
                : String.join("\n", attachmentUrls);

        String userMessage = """
                [과제 제목] %s
                [과제 내용] %s

                [제출물 제목] %s
                [제출물 내용] %s
                """.formatted(
                taskTitle,
                taskContent != null ? taskContent : "",
                submissionTitle,
                submissionContent != null ? submissionContent : "");

        if (!attachmentUrlText.isBlank()) {
            userMessage += "\n[Submission Attachment File URLs]\n" + attachmentUrlText;
        }
        if (StringUtils.hasText(attachmentText)) {
            userMessage += "\n\n[Submission Attachment Text]\n" + attachmentText;
        }

        Map<String, Object> requestBody = Map.of(
                "model", model,
                "messages", List.of(
                        Map.of("role", "system", "content", SYSTEM_PROMPT),
                        Map.of("role", "user", "content", userMessage)
                ),
                "response_format", Map.of("type", "json_object")
        );

        try {
            String responseBody = restClient.post()
                    .uri("/chat/completions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);

            JsonNode root = objectMapper.readTree(responseBody);
            String content = root.path("choices").get(0)
                    .path("message").path("content").asText();

            return objectMapper.readValue(content, AiFeedbackResult.class);
        } catch (Exception e) {
            log.error("OpenAI API 호출 실패", e);
            throw new RuntimeException("AI 피드백 생성 실패: " + e.getMessage(), e);
        }
    }
}
