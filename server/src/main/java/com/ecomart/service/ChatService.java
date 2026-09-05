package com.ecomart.service;

import com.ecomart.common.SecurityUtils;
import com.ecomart.domain.entity.ChatMessage;
import com.ecomart.domain.entity.ChatSession;
import com.ecomart.domain.entity.User;
import com.ecomart.domain.enums.ChatRole;
import com.ecomart.dto.request.ChatRequest;
import com.ecomart.dto.response.ChatResponse;
import com.ecomart.dto.response.ChatSessionResponse;
import com.ecomart.exception.ResourceNotFoundException;
import com.ecomart.repository.ChatMessageRepository;
import com.ecomart.repository.ChatSessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ChatService {

    private final SecurityUtils securityUtils;
    private final ChatSessionRepository sessionRepository;
    private final ChatMessageRepository messageRepository;
    private final ChatBot chatBot;

    public ChatService(SecurityUtils securityUtils,
                       ChatSessionRepository sessionRepository,
                       ChatMessageRepository messageRepository,
                       ChatBot chatBot) {
        this.securityUtils = securityUtils;
        this.sessionRepository = sessionRepository;
        this.messageRepository = messageRepository;
        this.chatBot = chatBot;
    }

    @Transactional(readOnly = true)
    public List<ChatSessionResponse> mySessions() {
        return sessionRepository.findByUserIdOrderByCreatedAtDesc(securityUtils.currentUserId()).stream()
                .map(this::toSession)
                .toList();
    }

    @Transactional
    public ChatResponse send(ChatRequest request) {
        User user = securityUtils.currentUser();
        ChatSession session;
        if (request.sessionId() != null) {
            session = sessionRepository.findById(request.sessionId())
                    .filter(s -> s.getUser().getId().equals(user.getId()))
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phiên trò chuyện"));
        } else {
            session = new ChatSession();
            session.setUser(user);
            session.setTitle(firstLine(request.message()));
            session = sessionRepository.save(session);
        }

        ChatMessage userMsg = new ChatMessage();
        userMsg.setSession(session);
        userMsg.setRole(ChatRole.USER);
        userMsg.setContent(request.message());
        messageRepository.save(userMsg);
        session.getMessages().add(userMsg);

        String botReply = chatBot.answer(request.message());

        ChatMessage botMsg = new ChatMessage();
        botMsg.setSession(session);
        botMsg.setRole(ChatRole.BOT);
        botMsg.setContent(botReply);
        messageRepository.save(botMsg);
        session.getMessages().add(botMsg);
        sessionRepository.save(session);

        List<ChatResponse.MessageResponse> messages = messageRepository.findBySessionIdOrderByCreatedAtAsc(session.getId())
                .stream().map(com.ecomart.common.Mapper::toChatMessage).toList();
        return new ChatResponse(botReply, session.getId(), messages);
    }

    private ChatSessionResponse toSession(ChatSession s) {
        List<ChatResponse.MessageResponse> messages = s.getMessages().stream()
                .map(com.ecomart.common.Mapper::toChatMessage)
                .toList();
        return new ChatSessionResponse(s.getId(), s.getTitle(), s.getCreatedAt(), messages);
    }

    private String firstLine(String message) {
        String trimmed = message == null ? "" : message.trim();
        return trimmed.length() > 40 ? trimmed.substring(0, 40) + "..." : (trimmed.isEmpty() ? "Cuộc trò chuyện mới" : trimmed);
    }
}
