package com.ecomart.service;

import com.ecomart.common.Bm25;
import com.ecomart.common.VietText;
import com.ecomart.config.ShopProperties;
import com.ecomart.domain.entity.Category;
import com.ecomart.domain.entity.Product;
import com.ecomart.dto.request.ChatRequest;
import com.ecomart.integration.gemini.GeminiClient;
import com.ecomart.repository.CategoryRepository;
import com.ecomart.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ChatService {

    public static final String SOURCE_LOCAL = "local";
    public static final String SOURCE_BM25 = "bm25";
    public static final String SOURCE_GEMINI = "gemini";
    public static final String SOURCE_DEFAULT = "default";

    private static final String DEFAULT_REPLY = "Mình chưa hiểu rõ ý bạn. Bạn thử hỏi về sản phẩm, phí giao hàng, "
            + "thanh toán hoặc đơn hàng nhé, hoặc gọi hotline 0900 000 000 để được hỗ trợ trực tiếp.";
    private static final String GEMINI_SYSTEM_PROMPT = """
            Bạn là trợ lý ảo của siêu thị xanh EcoMart (siêu thị rau củ quả, trái cây, thực phẩm tươi sạch).
            Chỉ trả lời bằng tiếng Việt, ngắn gọn, thân thiện.
            Chỉ trả lời về EcoMart: sản phẩm, danh mục, giá, phí giao hàng, thanh toán, đơn hàng, tài khoản, chính sách.
            Không bịa thông tin; nếu không biết, nói bạn sẽ chuyển tới nhân viên hỗ trợ hotline 0900 000 000.
            """;

    public interface ChatStreamWriter {
        void text(String chunk);

        void source(String source);

        void done();
    }

    private record AnswerDoc(String text, String source) {
    }

    private final ChatKnowledge knowledge;
    private final GeminiClient geminiClient;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ShopProperties shopProperties;
    private final double bm25Threshold;
    private final long productRefreshMs;

    private volatile Bm25 index;
    private volatile List<AnswerDoc> answers;
    private volatile Instant lastProductRefresh;

    public ChatService(ChatKnowledge knowledge,
                       GeminiClient geminiClient,
                       ProductRepository productRepository,
                       CategoryRepository categoryRepository,
                       ShopProperties shopProperties,
                       @Value("${app.chat.bm25-threshold:1.0}") double bm25Threshold,
                       @Value("${app.chat.product-refresh-ms:300000}") long productRefreshMs) {
        this.knowledge = knowledge;
        this.geminiClient = geminiClient;
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.shopProperties = shopProperties;
        this.bm25Threshold = bm25Threshold;
        this.productRefreshMs = productRefreshMs;
    }

    public void answer(List<ChatRequest.Message> messages, ChatStreamWriter writer) {
        String latestUser = latestUserMessage(messages);
        if (latestUser == null || latestUser.isBlank()) {
            stream(writer, replyForIntent(ChatIntent.HELP), SOURCE_LOCAL);
            return;
        }

        ChatIntent intent = detectIntent(latestUser);
        if (intent != null) {
            stream(writer, replyForIntent(intent), SOURCE_LOCAL);
            return;
        }

        Optional<String> bm25Reply = bm25Reply(latestUser);
        if (bm25Reply.isPresent()) {
            stream(writer, bm25Reply.get(), SOURCE_BM25);
            return;
        }

        Optional<String> geminiReply = geminiReply(messages);
        if (geminiReply.isPresent()) {
            stream(writer, geminiReply.get(), SOURCE_GEMINI);
            return;
        }

        stream(writer, DEFAULT_REPLY, SOURCE_DEFAULT);
    }

    private void stream(ChatStreamWriter writer, String reply, String source) {
        String singleLine = reply.replace('\r', ' ').replace('\n', ' ');
        for (String chunk : chunk(singleLine)) {
            writer.text(chunk);
        }
        writer.source(source);
        writer.done();
    }

    private List<String> chunk(String text) {
        List<String> words = List.of(text.split(" "));
        List<String> chunks = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        for (String word : words) {
            if (!current.isEmpty() && current.length() + word.length() + 1 > 42) {
                chunks.add(current + " ");
                current.setLength(0);
            }
            if (!current.isEmpty()) {
                current.append(' ');
            }
            current.append(word);
        }
        if (!current.isEmpty()) {
            chunks.add(current.toString());
        }
        return chunks;
    }

    private String latestUserMessage(List<ChatRequest.Message> messages) {
        if (messages == null) {
            return null;
        }
        for (int i = messages.size() - 1; i >= 0; i--) {
            ChatRequest.Message message = messages.get(i);
            if (message != null && "user".equalsIgnoreCase(message.role())) {
                return message.content();
            }
        }
        return null;
    }

    private ChatIntent detectIntent(String raw) {
        List<String> tokens = VietText.tokenize(VietText.normalize(raw));
        if (tokens.isEmpty()) {
            return null;
        }
        for (ChatIntent intent : ChatIntent.ordered()) {
            if (intent == ChatIntent.GREETING && tokens.size() > 3) {
                continue;
            }
            for (String phrase : intent.keywords()) {
                List<String> phraseTokens = VietText.tokenize(phrase);
                boolean matched = phraseTokens.stream()
                        .allMatch(phraseToken -> matchesToken(tokens, phraseToken));
                if (matched) {
                    return intent;
                }
            }
        }
        return null;
    }

    private boolean matchesToken(List<String> tokens, String phraseToken) {
        if (tokens.contains(phraseToken)) {
            return true;
        }
        return phraseToken.length() >= 5
                && tokens.stream().anyMatch(token -> VietText.fuzzyEquals(phraseToken, token));
    }

    private String replyForIntent(ChatIntent intent) {
        return switch (intent) {
            case SHIPPING_FEE -> "Phí giao hàng hiện tại là " + vnd(shopProperties.shippingFee())
                    + " cho mỗi đơn. Mức phí cụ thể sẽ được tính chính xác khi bạn đặt hàng.";
            case OUT_OF_STOCK -> outOfStockReply();
            case NEW_PRODUCTS -> latestProductsReply();
            case CATEGORY_LIST -> categoriesReply();
            case PAYMENT_METHODS -> "EcoMart hỗ trợ 2 hình thức thanh toán: thanh toán khi nhận hàng (COD) "
                    + "và thanh toán qua mã QR PayOS ngay khi đặt đơn.";
            case ORDER_HELP -> "Bạn mở mục Đơn hàng trong tài khoản để xem trạng thái hoặc hủy đơn chưa xác nhận. "
                    + "Nếu cần hỗ trợ gấp, gọi hotline 0900 000 000.";
            case ACCOUNT -> "Bạn đăng nhập hoặc đăng ký ở góc trên bên phải trang web. Tài khoản giúp lưu địa chỉ, "
                    + "theo dõi đơn hàng và thanh toán nhanh hơn.";
            case RESET_PASSWORD -> "Bạn mở trang đăng nhập và chọn quên mật khẩu để đặt lại mật khẩu "
                    + "qua email hoặc số điện thoại đã đăng ký.";
            case HELP -> "Mình là trợ lý ảo của EcoMart, có thể giúp bạn: phí giao hàng, tìm sản phẩm, danh mục, "
                    + "thanh toán, đơn hàng, tài khoản. Bạn cứ đặt câu hỏi tự nhiên nhé!";
            case THANKS -> "Không có gì ạ! Cần thêm gì cứ hỏi mình nhé.";
            case FAREWELL -> "Tạm biệt bạn! Chúc bạn có những bữa ăn ngon và lành. EcoMart luôn sẵn sàng phục vụ.";
            case GREETING -> "Chào bạn! Mình là trợ lý ảo của EcoMart. Bạn cần tìm món gì, hay hỏi về phí giao hàng, "
                    + "thanh toán, đơn hàng không?";
        };
    }

    private String outOfStockReply() {
        List<Product> products = productRepository.findByIsActiveTrueAndStockOrderByNameAsc(0);
        if (products.isEmpty()) {
            return "Hiện không có sản phẩm nào hết hàng, mọi mặt hàng bạn cần đều sẵn sàng.";
        }
        StringBuilder reply = new StringBuilder("Một số sản phẩm đang tạm hết hàng: ");
        for (int i = 0; i < Math.min(products.size(), 5); i++) {
            if (i > 0) {
                reply.append(", ");
            }
            reply.append(products.get(i).getName());
        }
        reply.append(". Hàng sẽ được bổ sung trong thời gian tới, bạn quay lại thường xuyên nhé!");
        return reply.toString();
    }

    private String latestProductsReply() {
        List<Product> products = productRepository.findTop8ByIsActiveTrueOrderByCreatedAtDesc();
        if (products.isEmpty()) {
            return "Hiện chưa có sản phẩm nào để gợi ý, bạn qua trang sản phẩm xem thêm nhé.";
        }
        StringBuilder reply = new StringBuilder("Một số sản phẩm mới tại EcoMart: ");
        for (int i = 0; i < Math.min(products.size(), 5); i++) {
            if (i > 0) {
                reply.append(" • ");
            }
            reply.append(formatProduct(products.get(i)));
        }
        return reply.toString();
    }

    private String categoriesReply() {
        List<Category> parents = categoryRepository.findByParentIsNullOrderByDisplayOrderAsc();
        if (parents.isEmpty()) {
            return "Danh mục sản phẩm đang được cập nhật, bạn xem trên trang chủ nhé.";
        }
        StringBuilder reply = new StringBuilder("EcoMart có các nhóm hàng chính: ");
        for (int i = 0; i < parents.size(); i++) {
            if (i > 0) {
                reply.append(", ");
            }
            reply.append(parents.get(i).getName());
        }
        reply.append(".");
        return reply.toString();
    }

    private Optional<String> bm25Reply(String query) {
        Bm25 current = ensureIndex().index;
        List<Bm25.ScoredDocument> hits = current.search(query, 1, bm25Threshold);
        if (hits.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(answers.get(hits.get(0).id()).text());
    }

    private IndexState ensureIndex() {
        Instant now = Instant.now();
        if (index == null || answers == null || lastProductRefresh == null
                || now.isAfter(lastProductRefresh.plusMillis(productRefreshMs))) {
            rebuildIndex();
        }
        return new IndexState(index, answers);
    }

    private record IndexState(Bm25 index, List<AnswerDoc> answers) {
    }

    private synchronized void rebuildIndex() {
        Instant now = Instant.now();
        if (index != null && answers != null && lastProductRefresh != null
                && !now.isAfter(lastProductRefresh.plusMillis(productRefreshMs))) {
            return;
        }
        List<Bm25.Document> documents = new ArrayList<>();
        List<AnswerDoc> built = new ArrayList<>();
        int id = 0;
        for (ChatKnowledge.ChatEntry entry : knowledge.entries()) {
            String text = String.join(" ", entry.keywords()) + " " + entry.answer();
            documents.add(new Bm25.Document(id, VietText.tokenize(VietText.normalize(text))));
            built.add(new AnswerDoc(entry.answer(), SOURCE_BM25));
            id++;
        }
        for (Product product : productRepository.search("", null, null, null, true, PageRequest.of(0, 100)).getContent()) {
            String text = product.getName() + " " + nullToEmpty(product.getOrigin())
                    + " " + product.getCategory().getName() + " " + nullToEmpty(product.getDescription());
            documents.add(new Bm25.Document(id, VietText.tokenize(VietText.normalize(text))));
            built.add(new AnswerDoc(formatProduct(product), SOURCE_BM25));
            id++;
        }
        this.index = new Bm25(documents);
        this.answers = List.copyOf(built);
        this.lastProductRefresh = now;
    }

    private Optional<String> geminiReply(List<ChatRequest.Message> messages) {
        List<GeminiClient.ChatTurn> turns = messages.stream()
                .filter(m -> m.content() != null && !m.content().isBlank())
                .map(m -> new GeminiClient.ChatTurn("user".equalsIgnoreCase(m.role()) ? "user" : "model", m.content()))
                .toList();
        if (turns.isEmpty()) {
            return Optional.empty();
        }
        List<GeminiClient.ChatTurn> trimmed = turns.size() > 8 ? turns.subList(turns.size() - 8, turns.size()) : turns;
        return geminiClient.generate(GEMINI_SYSTEM_PROMPT, trimmed, 512);
    }

    private String formatProduct(Product product) {
        String status = product.getStock() > 0 ? "còn " + product.getStock() : "tạm hết hàng";
        String origin = product.getOrigin() == null ? "" : " (xuất xứ " + product.getOrigin() + ")";
        return product.getName() + " – " + vnd(product.getPrice()) + " – " + status + origin;
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private String vnd(double amount) {
        String digits = String.format("%.0f", amount);
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < digits.length(); i++) {
            if (i > 0 && (digits.length() - i) % 3 == 0) {
                out.append('.');
            }
            out.append(digits.charAt(i));
        }
        return out + "₫";
    }
}