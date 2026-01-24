package org.example.kaoyanplatform.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.kaoyanplatform.entity.dto.QuestionDTO;
import org.example.kaoyanplatform.service.GLMService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

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

    /**
     * 修复AI返回的JSON字符串中的转义问题
     * 1. LaTeX公式的反斜杠（如 \frac 应该是 \\frac）
     * 2. 控制字符（如换行符应该被转义为 \n）
     *
     * @param json AI返回的JSON字符串
     * @return 修复后的JSON字符串
     */
    private String fixLaTeXEscaping(String json) {
        StringBuilder result = new StringBuilder();
        boolean inString = false;
        boolean escapeNext = false;

        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);

            if (!inString) {
                // 不在字符串内
                if (c == '"') {
                    inString = true;
                    result.append(c);
                } else {
                    result.append(c);
                }
            } else {
                // 在字符串内
                if (escapeNext) {
                    // 上一个字符是反斜杠，检查是否是有效的JSON转义
                    // 检查是否是有效的JSON转义序列
                    if (c == '"' || c == '\\' || c == '/' || c == 'b' ||
                        c == 'f' || c == 'n' || c == 'r' || c == 't' || c == 'u') {
                        // 这是有效的JSON转义，保持不变
                        result.append('\\').append(c);
                    } else {
                        // 不是有效的JSON转义，说明这是LaTeX反斜杠，需要转义
                        // 例如 \frac 应该变成 \\frac
                        result.append("\\\\").append(c);
                    }
                    escapeNext = false;
                } else if (c == '\\') {
                    escapeNext = true;
                } else if (c == '"') {
                    inString = false;
                    result.append(c);
                } else {
                    // 处理控制字符
                    switch (c) {
                        case '\n': result.append("\\n"); break;
                        case '\r': result.append("\\r"); break;
                        case '\t': result.append("\\t"); break;
                        case '\b': result.append("\\b"); break;
                        case '\f': result.append("\\f"); break;
                        default:
                            // 其他控制字符（ASCII 0-31，除了空格）
                            if (c < 32) {
                                result.append(String.format("\\u%04x", (int) c));
                            } else {
                                result.append(c);
                            }
                            break;
                    }
                }
            }
        }

        // 处理最后一个字符是反斜杠的情况
        if (escapeNext) {
            result.append("\\\\");
        }

        return result.toString();
    }

    @Override
    public QuestionDTO recognizeQuestionFromImage(MultipartFile file) {
        try {
            if (file.isEmpty()) {
                throw new IllegalArgumentException("请上传图片文件");
            }

            // 验证文件类型
            String contentType = file.getContentType();
            if (contentType == null || (!contentType.startsWith("image/") && !contentType.equals("application/octet-stream"))) {
                throw new IllegalArgumentException("只支持图片文件");
            }

            // 读取图片并转换为 base64
            byte[] imageBytes = file.getBytes();
            String base64Image = Base64.getEncoder().encodeToString(imageBytes);

            // 构造请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "glm-4.6v-flash"); // 使用免费的 Flash 模型

            // 构造消息（GLM 格式：content 是数组，包含 text 和 image_url）
            List<Map<String, Object>> messages = new ArrayList<>();

            // 用户消息（包含图片和提示词）
            Map<String, Object> userMessage = new HashMap<>();
            userMessage.put("role", "user");

            // content 是一个数组，包含文本提示词和图片
            List<Map<String, Object>> messageContent = new ArrayList<>();

            // 添加提示词
            Map<String, Object> textContent = new HashMap<>();
            textContent.put("type", "text");
            textContent.put("text", "你是一个专业的数学题目识别助手。请识别图片中的题目内容，并以JSON格式返回。\n\n" +
                    "【题目类型】1-单选, 2-多选, 3-填空, 4-简答\n\n" +
                    "【LaTeX公式处理要求】\n" +
                    "- 题目中的数学公式必须用LaTeX语法表示\n" +
                    "- 行内公式用 $...$ 包裹\n" +
                    "- 独立公式用 $$...$$ 包裹\n" +
                    "- 支持常见LaTeX数学符号：分数\\frac{}{}、上下标_^{}、积分\\int、求和\\sum、根号\\sqrt{}、希腊字母等\n\n" +
                    "【JSON格式要求】\n" +
                    "```json\n" +
                    "{\n" +
                    "  \"type\": 1,\n" +
                    "  \"content\": \"题目内容（含LaTeX公式）\",\n" +
                    "  \"options\": [\"选项A（含LaTeX公式）\", \"选项B\", \"选项C\", \"选项D\"],\n" +
                    "  \"answer\": \"答案（含LaTeX公式）\",\n" +
                    "  \"analysis\": \"解析（含LaTeX公式）\",\n" +
                    "  \"tags\": [\"标签1\", \"标签2\"],\n" +
                    "  \"difficulty\": 3\n" +
                    "}\n" +
                    "```\n\n" +
                    "【示例】\n" +
                    "题目：设函数 $f(x) = x^3 - 3x + 1$，求 $f'(x)$\n" +
                    "选项应转换为：[\"$$3x^2-3$$\", \"$$3x^2+3$$\", \"$$x^2-3$$\", \"$$x^2+3$$\"]\n" +
                    "答案：$$3x^2-3$$\n\n" +
                    "【注意事项】\n" +
                    "1. type: 单选题填1，多选题填2，填空题填3，简答题填4\n" +
                    "2. 如果是选择题，必须包含4个选项\n" +
                    "3. 简答题和填空题可以没有options\n" +
                    "4. 所有数学符号、公式、表达式必须用LaTeX表示并用$或$$包裹\n" +
                    "5. 纯粹返回JSON，不要有其他说明文字");
            messageContent.add(textContent);

            // 添加图片
            Map<String, Object> imageContent = new HashMap<>();
            imageContent.put("type", "image_url");
            imageContent.put("image_url", Map.of("url", base64Image)); // GLM 支持直接传 base64
            messageContent.add(imageContent);

            userMessage.put("content", messageContent);
            messages.add(userMessage);

            requestBody.put("messages", messages);
            requestBody.put("temperature", 0.3);
            requestBody.put("max_tokens", 2000);

            // 发送 HTTP 请求
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

            // 解析响应
            if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
                throw new RuntimeException("AI识别服务异常: " + response.getBody());
            }

            // 解析智谱AI返回的 JSON
            Map<String, Object> glmResponse = objectMapper.readValue(response.getBody(), new TypeReference<Map<String, Object>>() {});

            // 提取 AI 返回的内容
            List<Map<String, Object>> responseMessages = (List<Map<String, Object>>) glmResponse.get("choices");
            if (responseMessages == null || responseMessages.isEmpty()) {
                throw new RuntimeException("AI未能返回识别结果");
            }

            String content = (String) ((Map<String, Object>) responseMessages.get(0).get("message")).get("content");

            // 解析 JSON 内容
            // 去除可能的 markdown 代码块标记
            content = content.replaceAll("```json\\s*", "").replaceAll("```\\s*", "").trim();

            // 处理LaTeX公式中的反斜杠转义问题
            // AI返回的JSON中，LaTeX的反斜杠可能没有被正确转义（如 \frac 应该是 \\frac）
            // 我们需要先处理这个问题，否则JSON解析会失败
            // 策略：查找JSON字符串值中的反斜杠，并正确转义
            content = fixLaTeXEscaping(content);

            // 将字符串转换为 QuestionDTO
            Map<String, Object> recognizedData = objectMapper.readValue(content, new TypeReference<Map<String, Object>>() {});

            // 构造 QuestionDTO
            QuestionDTO questionDTO = new QuestionDTO();
            questionDTO.setType(((Number) recognizedData.get("type")).intValue());
            questionDTO.setContent((String) recognizedData.get("content"));

            // 处理选项
            if (recognizedData.get("options") != null) {
                List<String> optionsList = (List<String>) recognizedData.get("options");
                if (optionsList != null && !optionsList.isEmpty()) {
                    // 转换为新的对象格式
                    String[] labels = {"A", "B", "C", "D"};
                    List<Map<String, String>> formattedOptions = new ArrayList<>();
                    for (int i = 0; i < optionsList.size() && i < labels.length; i++) {
                        Map<String, String> option = new HashMap<>();
                        option.put("label", labels[i]);
                        option.put("text", optionsList.get(i));
                        formattedOptions.add(option);
                    }
                    questionDTO.setOptions(formattedOptions);
                }
            }

            questionDTO.setAnswer((String) recognizedData.get("answer"));
            questionDTO.setAnalysis((String) recognizedData.get("analysis"));
            questionDTO.setTags((List<String>) recognizedData.get("tags"));
            if (recognizedData.get("difficulty") != null) {
                questionDTO.setDifficulty(((Number) recognizedData.get("difficulty")).intValue());
            } else {
                questionDTO.setDifficulty(3);
            }

            return questionDTO;

        } catch (Exception e) {
            throw new RuntimeException("AI识别失败: " + e.getMessage(), e);
        }
    }
}
