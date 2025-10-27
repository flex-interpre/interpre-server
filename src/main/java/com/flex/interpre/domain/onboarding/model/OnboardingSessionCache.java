package com.flex.interpre.domain.onboarding.model;

import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OnboardingSessionCache implements Serializable {

    private String userId;

    @Builder.Default
    private List<ChatMessage> messages = new ArrayList<>();

    private Boolean completed;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ChatMessage implements Serializable {
        private String role;  // "user" or "assistant"
        private String content;
        private LocalDateTime timestamp;
    }

    public void addMessage(String role, String content) {
        if (this.messages == null) {
            this.messages = new ArrayList<>();
        }
        this.messages.add(ChatMessage.builder()
                .role(role)
                .content(content)
                .timestamp(LocalDateTime.now())
                .build());
        this.updatedAt = LocalDateTime.now();
    }

    // Claude API 형식으로 메시지 변환
    public List<Map<String, String>> getMessagesForClaude() {
        List<Map<String, String>> claudeMessages = new ArrayList<>();
        for (ChatMessage msg : messages) {
            claudeMessages.add(Map.of(
                    "role", msg.getRole(),
                    "content", msg.getContent()
            ));
        }
        return claudeMessages;
    }
}