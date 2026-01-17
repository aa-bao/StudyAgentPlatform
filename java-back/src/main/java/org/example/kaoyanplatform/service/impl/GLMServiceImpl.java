package org.example.kaoyanplatform.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.kaoyanplatform.service.GLMService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class GLMServiceImpl implements GLMService {

    @Value("${zhipu.api.key}")
    private String apiKey;

    private static final String API_URL = "https://open.bigmodel.cn/api/paas/v4/chat/completions";

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String callGLMAPI(String prompt) {
        int maxRetries = 3;
        int retryCount = 0;
        long baseDelay = 1000;

        while (retryCount < maxRetries) {
            try {
                return callGLMAPIInternal(prompt);
            } catch (Exception e) {
                retryCount++;
                if (retryCount >= maxRetries) {
                    throw new RuntimeException("GLM API 调用失败，已重试 " + maxRetries + " 次", e);
                }

                long delay = (long) (baseDelay * Math.pow(2, retryCount - 1));
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("重试被中断", ie);
                }
            }
        }

        throw new RuntimeException("GLM API 调用失败");
    }

    private String callGLMAPIInternal(String prompt) throws Exception {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "glm-4");

        Map<String, String> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", prompt);

        requestBody.put("messages", new Object[]{message});
        requestBody.put("temperature", 0.3);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                API_URL,
                HttpMethod.POST,
                entity,
                String.class
        );

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode choices = root.path("choices");
            if (choices.isArray() && choices.size() > 0) {
                return choices.get(0).path("message").path("content").asText();
            }
        }

        throw new RuntimeException("GLM API 返回格式错误");
    }

    @Override
    public String generateGradingPrompt(String question, String userAnswer, String standardAnswer, Double scoreValue) {
        return String.format(
                "你是一位资深的考研数学阅卷组长。请按照以下标准对学生的解答进行评分：\n\n" +
                "【题目内容】\n%s\n\n" +
                "【标准答案】\n%s\n\n" +
                "【学生解答】\n%s\n\n" +
                "【评分要求】\n" +
                "1. 本题满分：%.1f分\n" +
                "2. 请按步骤给分，关注解题思路的正确性和完整性\n" +
                "3. 即使最终答案错误，也要考虑过程分\n" +
                "4. 对于计算错误、逻辑错误要明确指出\n" +
                "5. 给出详细的建设性反馈\n\n" +
                "【输出格式】\n" +
                "请以JSON格式返回评分结果，格式如下：\n" +
                "{\n" +
                "  \"score\": 得分(数字),\n" +
                "  \"feedback\": \"详细的评分反馈\",\n" +
                "  \"strengths\": [\"优点1\", \"优点2\"],\n" +
                "  \"weaknesses\": [\"不足点1\", \"不足点2\"]\n" +
                "}\n\n" +
                "请确保输出的是有效的JSON格式。",
                question, standardAnswer, userAnswer, scoreValue
        );
    }

    @Override
    public Map<String, Object> parseGradingResult(String response) {
        try {
            String jsonContent = extractJSONFromResponse(response);
            if (jsonContent != null) {
                JsonNode root = objectMapper.readTree(jsonContent);
                Map<String, Object> result = new HashMap<>();
                result.put("score", root.path("score").asDouble(0.0));
                result.put("feedback", root.path("feedback").asText(""));
                
                JsonNode strengths = root.path("strengths");
                if (strengths.isArray()) {
                    result.put("strengths", objectMapper.convertValue(strengths, Object.class));
                }
                
                JsonNode weaknesses = root.path("weaknesses");
                if (weaknesses.isArray()) {
                    result.put("weaknesses", objectMapper.convertValue(weaknesses, Object.class));
                }
                
                return result;
            }
        } catch (Exception e) {
            Map<String, Object> fallback = new HashMap<>();
            fallback.put("score", 0.0);
            fallback.put("feedback", "AI批改解析失败：" + e.getMessage());
            fallback.put("strengths", new Object[]{});
            fallback.put("weaknesses", new Object[]{});
            return fallback;
        }

        Map<String, Object> fallback = new HashMap<>();
        fallback.put("score", 0.0);
        fallback.put("feedback", "无法解析AI返回结果");
        fallback.put("strengths", new Object[]{});
        fallback.put("weaknesses", new Object[]{});
        return fallback;
    }

    private String extractJSONFromResponse(String response) {
        int startIndex = response.indexOf("{");
        int endIndex = response.lastIndexOf("}");

        if (startIndex != -1 && endIndex != -1 && endIndex > startIndex) {
            return response.substring(startIndex, endIndex + 1);
        }
        return null;
    }
}
