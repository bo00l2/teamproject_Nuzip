package com.teamproject.GeminiAPI;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Service
public class GeminiService {

    private final GeminiConfig config;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public GeminiService(GeminiConfig config) {
        this.config = config;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(config.getTimeoutSeconds()))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    public String generateText(String prompt) {
        try {
            String apiKey = config.getApiKey();
            if (apiKey == null || apiKey.isEmpty()) {
                return "⚠ 환경 변수 GEMINI_API_KEY가 설정되지 않았습니다.";
            }

            String url = "https://generativelanguage.googleapis.com/v1beta/models/"
                    + config.getModel() + ":generateContent";

            String jsonBody = """
                {
                  "contents": [
                    { "parts": [{ "text": "%s" }] }
                  ],
                  "generationConfig": {
                    "temperature": %s,
                    "maxOutputTokens": %s
                  }
                }
                """.formatted(prompt, config.getTemperature(), config.getMaxTokens());

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json; charset=utf-8")
                    .header("x-goog-api-key", apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .timeout(Duration.ofSeconds(30))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                return "❌ API Error (" + response.statusCode() + "): " + response.body();
            }

            return extractTextFromResponse(response.body());

        } catch (Exception e) {
            return "⚠ 요청 중 오류 발생: " + e.getMessage();
        }
    }

    private String extractTextFromResponse(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode candidates = root.path("candidates");

            if (candidates.isArray() && candidates.size() > 0) {
                JsonNode content = candidates.get(0).path("content");
                JsonNode parts = content.path("parts");

                if (parts.isArray() && parts.size() > 0) {
                    return parts.get(0).path("text").asText();
                } else {
                    return "⚠ No 'parts' found. Full content: " + content.toString();
                }
            } else {
                return "⚠ No 'candidates' found. Full response: " + root.toString();
            }
        } catch (Exception e) {
            return "⚠ 응답 파싱 중 오류 발생: " + e.getMessage();
        }
    }

}
