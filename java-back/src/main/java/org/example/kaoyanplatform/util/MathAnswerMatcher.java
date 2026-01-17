package org.example.kaoyanplatform.util;

import org.example.kaoyanplatform.service.GLMService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * 数学答案对比工具
 * 用于对比用户输入的数学答案与标准答案（可能包含 LaTeX 格式）
 */
@Component
public class MathAnswerMatcher {

    @Autowired
    private GLMService glmService;

    /**
     * 对比两个数学答案是否等价
     * @param standardAnswer 标准答案（可能包含 LaTeX）
     * @param userAnswer 用户答案
     * @return 是否正确
     */
    public boolean matchMathAnswer(String standardAnswer, String userAnswer) {
        if (standardAnswer == null || userAnswer == null) {
            return false;
        }

        standardAnswer = standardAnswer.trim();
        userAnswer = userAnswer.trim();

        // 1. 完全匹配（包括 LaTeX 格式）
        if (standardAnswer.equalsIgnoreCase(userAnswer)) {
            return true;
        }

        // 2. 去掉 LaTeX 标记后匹配
        String standardPlain = removeLatexMarkup(standardAnswer);
        if (standardPlain.equalsIgnoreCase(userAnswer)) {
            return true;
        }

        // 3. 尝试将用户输入转换为 LaTeX 格式后匹配
        String userLatex = convertToLatex(userAnswer);
        if (standardAnswer.contains(userLatex) || userLatex.contains(standardAnswer)) {
            return true;
        }

        // 4. 数值匹配（提取数字进行对比）
        if (compareNumerical(standardAnswer, userAnswer)) {
            return true;
        }

        // 5. 使用 LLM 进行智能判断
        return compareByLLM(standardAnswer, userAnswer);
    }

    /**
     * 移除 LaTeX 标记
     * \frac{1}{2} -> 1/2
     * \sqrt{2} -> sqrt(2)
     * x^{2} -> x^2
     */
    private String removeLatexMarkup(String latex) {
        if (latex == null) return "";

        String result = latex;

        // 处理分数 \frac{a}{b} -> a/b
        result = result.replaceAll("\\\\frac\\{([^}]+)\\}\\{([^}]+)\\}", "$1/$2");

        // 处理根号 \sqrt{a} -> sqrt(a)
        result = result.replaceAll("\\\\sqrt\\{([^}]+)\\}", "sqrt($1)");

        // 处理上标 x^{a} -> x^a
        result = result.replaceAll("\\^\\{([^}]+)\\}", "^$1");

        // 处理其他常用 LaTeX 符号
        result = result.replaceAll("\\\\pi", "π");
        result = result.replaceAll("\\\\infty", "∞");
        result = result.replaceAll("\\\\times", "×");
        result = result.replaceAll("\\\\div", "÷");
        result = result.replaceAll("\\\\leq", "≤");
        result = result.replaceAll("\\\\geq", "≥");
        result = result.replaceAll("\\\\neq", "≠");
        result = result.replaceAll("\\\\approx", "≈");

        // 移除剩余的反斜杠
        result = result.replaceAll("\\\\", "");

        return result.trim();
    }

    /**
     * 将用户输入转换为 LaTeX 格式
     */
    private String convertToLatex(String input) {
        if (input == null) return "";

        String result = input;

        // sqrt(2) -> \sqrt{2}
        result = result.replaceAll("sqrt\\(([^)]+)\\)", "\\\\sqrt{$1}");

        // x^2 -> x^{2}
        result = result.replaceAll("\\^([0-9a-zA-Z]+)", "^{$1}");

        return result;
    }

    /**
     * 数值对比（提取数字进行对比）
     */
    private boolean compareNumerical(String answer1, String answer2) {
        try {
            // 提取两个答案中的数字
            Double num1 = extractFirstNumber(answer1);
            Double num2 = extractFirstNumber(answer2);

            if (num1 != null && num2 != null) {
                // 允许 0.001 的误差
                return Math.abs(num1 - num2) < 0.001;
            }
        } catch (Exception e) {
            // 忽略错误，继续其他对比方法
        }
        return false;
    }

    /**
     * 提取字符串中的第一个数字
     */
    private Double extractFirstNumber(String str) {
        if (str == null) return null;

        // 匹配整数和小数
        java.util.regex.Pattern pattern = Pattern.compile("-?\\d+\\.?\\d*");
        java.util.regex.Matcher matcher = pattern.matcher(str);

        if (matcher.find()) {
            try {
                return Double.parseDouble(matcher.group());
            } catch (NumberFormatException e) {
                return null;
            }
        }

        return null;
    }

    /**
     * 使用 LLM 判断两个数学表达式是否等价
     */
    private boolean compareByLLM(String standardAnswer, String userAnswer) {
        try {
            String prompt = String.format(
                "请判断以下两个数学答案是否等价（意思相同即可）。\n" +
                "标准答案：%s\n" +
                "用户答案：%s\n" +
                "只需要回答\"是\"或\"否\"。",
                standardAnswer, userAnswer
            );

            String response = glmService.callGLMAPI(prompt);

            // 判断 LLM 的回复
            return response.contains("是") || response.toLowerCase().contains("yes") || response.toLowerCase().contains("true");

        } catch (Exception e) {
            // LLM 调用失败，返回 false
            return false;
        }
    }
}
