package com.ecomart.controller;

import com.ecomart.dto.request.ChatRequest;
import com.ecomart.service.ChatService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private static final long SSE_TIMEOUT_MS = 90_000L;
    private static final long PACE_MS = 18L;

    private final ChatService chatService;
    private final TaskExecutor taskExecutor;
    private final ObjectMapper objectMapper;

    public ChatController(ChatService chatService,
                          @Qualifier("applicationTaskExecutor") TaskExecutor taskExecutor,
                          ObjectMapper objectMapper) {
        this.chatService = chatService;
        this.taskExecutor = taskExecutor;
        this.objectMapper = objectMapper;
    }

    @PostMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chat(@RequestBody ChatRequest request) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MS);
        taskExecutor.execute(() -> dispatch(emitter, request));
        return emitter;
    }

    private void dispatch(SseEmitter emitter, ChatRequest request) {
        try {
            chatService.answer(request.messages(), new SseWriter(emitter, objectMapper));
            emitter.complete();
        } catch (Exception ex) {
            try {
                emitter.completeWithError(ex);
            } catch (Exception closed) {
                // client already disconnected
            }
        }
    }

    private static final class SseWriter implements ChatService.ChatStreamWriter {

        private final SseEmitter emitter;
        private final ObjectMapper objectMapper;
        private boolean disconnected;

        SseWriter(SseEmitter emitter, ObjectMapper objectMapper) {
            this.emitter = emitter;
            this.objectMapper = objectMapper;
        }

        @Override
        public void text(String chunk) {
            if (disconnected) {
                return;
            }
            try {
                emitter.send(SseEmitter.event().data(objectMapper.writeValueAsString(Map.of("text", chunk))));
                Thread.sleep(PACE_MS);
            } catch (Exception ex) {
                disconnected = true;
            }
        }

        @Override
        public void source(String source) {
            if (disconnected) {
                return;
            }
            try {
                emitter.send(SseEmitter.event().data(objectMapper.writeValueAsString(Map.of("source", source))));
            } catch (Exception ex) {
                disconnected = true;
            }
        }

        @Override
        public void done() {
            if (disconnected) {
                return;
            }
            try {
                emitter.send(SseEmitter.event().data("[DONE]"));
            } catch (Exception ex) {
                disconnected = true;
            }
        }
    }
}