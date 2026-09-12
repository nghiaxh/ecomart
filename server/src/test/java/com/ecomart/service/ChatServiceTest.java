package com.ecomart.service;

import com.ecomart.common.VietText;
import com.ecomart.config.ShopProperties;
import com.ecomart.domain.entity.Category;
import com.ecomart.domain.entity.Product;
import com.ecomart.dto.request.ChatRequest;
import com.ecomart.integration.gemini.GeminiClient;
import com.ecomart.repository.CategoryRepository;
import com.ecomart.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ChatServiceTest {

    private ProductRepository productRepository;
    private CategoryRepository categoryRepository;
    private GeminiClient geminiClient;
    private ChatService service;

    @BeforeEach
    void setUp() {
        productRepository = mock(ProductRepository.class);
        categoryRepository = mock(CategoryRepository.class);
        geminiClient = mock(GeminiClient.class);
        ChatKnowledge knowledge = mock(ChatKnowledge.class);
        when(knowledge.entries()).thenReturn(List.of(
                new ChatKnowledge.ChatEntry("e1", "shipping", List.of("giao hang bao lau", "nhan hang"), "Đơn hàng thường giao trong 1-3 ngày."),
                new ChatKnowledge.ChatEntry("e2", "payment", List.of("hoa don", "tinh tien"), "Tổng tiền hiển thị đầy đủ tại bước thanh toán.")));
        when(productRepository.search(anyString(), any(), any(), any(), eq(true), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));
        service = new ChatService(knowledge, geminiClient, productRepository, categoryRepository,
                new ShopProperties(20000), 1.0, 300_000);
    }

    private List<ChatRequest.Message> messages(String content) {
        return List.of(new ChatRequest.Message("user", content));
    }

    private static class RecordingWriter implements ChatService.ChatStreamWriter {
        final List<String> chunks = new ArrayList<>();
        String source;
        boolean done;

        @Override
        public void text(String chunk) {
            chunks.add(chunk);
        }

        @Override
        public void source(String source) {
            this.source = source;
        }

        @Override
        public void done() {
            this.done = true;
        }

        String fullText() {
            return String.join("", chunks);
        }
    }

    private RecordingWriter answerTo(String content) {
        RecordingWriter writer = new RecordingWriter();
        service.answer(messages(content), writer);
        return writer;
    }

    @Test
    void greetingIsLocalAndStreamedInChunks() {
        RecordingWriter writer = answerTo("Chào bạn");

        assertEquals(ChatService.SOURCE_LOCAL, writer.source);
        assertTrue(writer.done);
        assertTrue(writer.chunks.size() > 1);
        assertTrue(writer.fullText().contains("trợ lý ảo của EcoMart"));
    }

@Test
    void greetingBeatIsNotMixedWithBestSeller() {
        Product carrot = product("Cà rốt Đà Lạt", 12);
        when(productRepository.findTop8ByIsActiveTrueOrderByCreatedAtDesc()).thenReturn(List.of(carrot));

        RecordingWriter writer = answerTo("Cậu giới thiệu sản phẩm bán chạy đi");

        assertEquals(ChatService.SOURCE_LOCAL, writer.source);
        assertTrue(writer.fullText().contains("sản phẩm mới tại EcoMart: Cà rốt Đà Lạt"));
    }

    @Test
    void shippingFeeIntentUsesLiveConfigValue() {
        RecordingWriter writer = answerTo("phí giao hàng là bao nhiêu?");

        assertEquals(ChatService.SOURCE_LOCAL, writer.source);
        assertTrue(writer.fullText().contains("20.000₫"));
    }

    @Test
    void noDiacriticsStillDetectsIntent() {
        RecordingWriter writer = answerTo("phi giao hang bao nhieu");

        assertEquals(ChatService.SOURCE_LOCAL, writer.source);
        assertTrue(writer.fullText().contains("20.000₫"));
    }

    @Test
    void outOfStockIntentListsSoldOutProducts() {
        Product missing = product("Bắp cải tím", 0);
        when(productRepository.findByIsActiveTrueAndStockOrderByNameAsc(0)).thenReturn(List.of(missing));

        RecordingWriter writer = answerTo("bạn còn hàng nào hết hàng?");

        assertEquals(ChatService.SOURCE_LOCAL, writer.source);
        assertTrue(writer.fullText().contains("Bắp cải tím"));
    }

    @Test
    void bm25RetrievalAnswersKnowledgeQuestion() {
        RecordingWriter writer = answerTo("khi nào tôi nhận được hàng?");

        assertEquals(ChatService.SOURCE_BM25, writer.source);
        assertTrue(writer.fullText().contains("1-3 ngày"));
    }

    @Test
    void geminiFallsBackWhenKnowledgeHasNoMatch() {
        when(geminiClient.generate(anyString(), any(), anyInt())).thenReturn(Optional.of("EcoMart không bán trà sữa."));

        RecordingWriter writer = answerTo("bạn bán trà sữa không?");

        assertEquals(ChatService.SOURCE_GEMINI, writer.source);
        assertTrue(writer.fullText().contains("trà sữa"));
    }

    @Test
    void defaultReplyWhenEverySourceMissing() {
        when(geminiClient.generate(anyString(), any(), anyInt())).thenReturn(Optional.empty());

        RecordingWriter writer = answerTo("bạn bán trà sữa không?");

        assertEquals(ChatService.SOURCE_DEFAULT, writer.source);
        assertTrue(writer.fullText().contains("hotline 0900 000 000"));
    }

    @Test
    void blankMessageReturnsHelp() {
        RecordingWriter writer = answerTo("   ");

        assertEquals(ChatService.SOURCE_LOCAL, writer.source);
        assertTrue(writer.fullText().contains("Mình là trợ lý ảo của EcoMart"));
    }

    private Product product(String name, int stock) {
        Product p = new Product();
        p.setName(name);
        p.setSlug(VietText.normalize(name).replace(' ', '-'));
        p.setPrice(45000);
        p.setStock(stock);
        p.setOrigin("Đà Lạt");
        Category category = new Category();
        category.setName("Rau củ sạch");
        p.setCategory(category);
        return p;
    }
}