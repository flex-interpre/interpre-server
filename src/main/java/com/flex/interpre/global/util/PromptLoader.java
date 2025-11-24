package com.flex.interpre.global.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class PromptLoader {

    private static final String PROMPT_BASE_PATH = "prompts/interview/";
    private final Map<String, String> promptCache = new HashMap<>();

    public String loadPrompt(String fileName) {
        if (promptCache.containsKey(fileName)) {
            return promptCache.get(fileName);
        }

        try {
            ClassPathResource resource = new ClassPathResource(PROMPT_BASE_PATH + fileName);
            String content = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            promptCache.put(fileName, content);
            return content;
        } catch (IOException e) {
            log.error("프롬프트 로드 실패: {}", fileName, e);
            throw new RuntimeException("프롬프트 파일을 찾을 수 없습니다: " + fileName, e);
        }
    }

    public String fillTemplate(String template, Map<String, String> variables) {
        String result = template;
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            result = result.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return result;
    }

    public String getAnswerCompletionCheckPrompt() {
        return loadPrompt("answer-completion-check.txt");
    }

    public String getQuestionGenerationSystemPrompt() {
        return loadPrompt("question-generation-system.txt");
    }

    public String getQuestionGenerationFirstPrompt(String document) {
        String template = loadPrompt("question-generation-first.txt");
        return fillTemplate(template, Map.of("document", document));
    }

    public String getFollowupDecisionPrompt() {
        return loadPrompt("followup-decision.txt");
    }

    public String getQuestionGenerationFollowupPrompt(String strategy, String document) {
        String template = loadPrompt("question-generation-followup.txt");
        return fillTemplate(template, Map.of(
                "strategy", strategy,
                "document", document
        ));
    }

    public String getAnalysisPrompt() {
        return loadPrompt("analysis.txt");
    }
}
