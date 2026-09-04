package com.ecomart.controller;

import com.ecomart.dto.request.ChatRequest;
import com.ecomart.dto.response.ChatResponse;
import com.ecomart.dto.response.ChatSessionResponse;
import com.ecomart.service.ChatService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@PreAuthorize("isAuthenticated()")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping("/sessions")
    public List<ChatSessionResponse> mySessions() {
        return chatService.mySessions();
    }

    @PostMapping("/send")
    public ChatResponse send(@Valid @RequestBody ChatRequest request) {
        return chatService.send(request);
    }
}
