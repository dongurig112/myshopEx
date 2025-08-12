package com.example.myShop.util.slack;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class SlackNotifier {

    @Value("${slack.webhook.url}")
    private String webhookUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public void sendMessage(String text) {
        try {
            System.out.println("🔔 SlackNotifier 진입함!");
            System.out.println("Webhook URL: " + webhookUrl);
            System.out.println("Slack 메시지 내용: " + text);

            ObjectMapper objectMapper = new ObjectMapper();

            Map<String, String> payload = new HashMap<>();
            payload.put("text", text); // 메시지 내용

            String jsonPayload = objectMapper.writeValueAsString(payload);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> entity = new HttpEntity<>(jsonPayload, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(webhookUrl, entity, String.class);
            System.out.println("Slack 응답 코드: " + response.getStatusCode());
        } catch (Exception e) {
            System.out.println("Slack 전송 중 에러 발생:");
            e.printStackTrace();
        }
    }
}
