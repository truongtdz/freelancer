package com.freelancer.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class GeminiService {

    @Value("${app.gemini.api-key}")
    private String apiKey;

    @Value("${app.gemini.model:gemini-2.0-flash}")
    private String model;

    private final RestTemplate restTemplate = new RestTemplate();

    @SuppressWarnings("unchecked")
    public String generateText(String prompt) {
        String url = "https://generativelanguage.googleapis.com/v1beta/models/"
                + model + ":generateContent";

        Map<String, Object> body = Map.of(
                "contents", List.of(Map.of(
                        "parts", List.of(Map.of("text", prompt))
                )),
                "generationConfig", Map.of(
                        "temperature", 0.7,
                        "maxOutputTokens", 1024
                )
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-goog-api-key", apiKey);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
            if (response.getBody() != null) {
                var candidates = (List<Map<String, Object>>) response.getBody().get("candidates");
                if (candidates != null && !candidates.isEmpty()) {
                    var content = (Map<String, Object>) candidates.get(0).get("content");
                    var parts   = (List<Map<String, Object>>) content.get("parts");
                    if (parts != null && !parts.isEmpty()) {
                        return parts.get(0).get("text").toString().trim();
                    }
                }
            }
        } catch (Exception e) {
            log.error("Gemini API error: {}", e.getMessage());
            throw new RuntimeException("Dịch vụ AI tạm thời không khả dụng");
        }
        throw new RuntimeException("Không nhận được phản hồi từ AI");
    }
}
