package com.ecomart.service;

import com.ecomart.domain.entity.Product;
import com.ecomart.repository.ProductRepository;
import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Simple in-process assistant: intent matching by keyword (Vietnamese, diacritic-insensitive)
 * plus lightweight RAG over the product catalog (name / category / description scoring).
 */
@Component
public class ChatBot {

    private static final NumberFormat VND = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
    private static final int MAX_RESULTS = 5;

    private static final Set<String> STOPWORDS = Set.of(
            "cua", "cho", "co", "la", "the", "va", "ve", "hay", "sao", "nao", "bao",
            "lam", "de", "gi", "ai", "khi", "thi", "mot", "nhieu", "duoc", "sang",
            "ngay", "ban", "may", "giup", "them", "ezhc", "data", "khong"
    );

    private record Rule(List<String> keywords, String reply, boolean includeProducts) {}

    private static final List<Rule> RULES = List.of(
            new Rule(List.of("xin chao", "chao ban", "chao"), "Xin chào! Mình là EcoBot, trợ lý của EcoMart. "
                    + "Bạn cần hỗ trợ về sản phẩm, giá cả, giao hàng hay thanh toán? Cứ hỏi mình nhé!", false),
            new Rule(List.of("gia bao nhieu", "bao nhieu tien", "gia re", "gia so", "khuyen mai", "giam gia",
                    "uu dai", "giam"), "Giá từng sản phẩm được niêm yết ngay trên trang bán hàng. "
                    + "Gửi mình tên món bạn quan tâm (ví dụ “cà rốt”) để mình gợi ý giá nhé! "
                    + "Ưu đãi mới luôn được cập nhật ở banner và mục “Sản phẩm mới”.", true),
            new Rule(List.of("giao hang", "giao nhanh", "van chuyen", "phi ship", "shipping", "nhan hang",
                    "bao lau", "may ngay", "may gio", "trong ngay"), "EcoMart giao hàng tận nơi toàn quốc. "
                    + "Nội thành nhận trong ngày hoặc 1–2 ngày; ngoại tỉnh 2–4 ngày. "
                    + "Phí giao tùy địa chỉ và hiển thị rõ khi bạn tiến hành thanh toán.", false),
            new Rule(List.of("doi tra", "hoan tra", "tra hang", "hoan tien", "bao hanh"),
                    "Bạn có thể đổi/trả trong 24h kể từ khi nhận hàng nếu sản phẩm lỗi hoặc không đúng đơn. "
                    + "Liên hệ hotline 0900 000 000 để được xử lý nhanh nhất.", false),
            new Rule(List.of("thanh toan", "chuyen khoan", "payos", "ma qr", "quet ma", "qr", "tien mat", "cod"),
                    "EcoMart hỗ trợ thanh toán khi nhận hàng (COD) hoặc quét mã QR qua PayOS ngay khi đặt hàng. "
                    + "Bạn chỉ cần chọn phương thức ở bước thanh toán nhé!", false),
            new Rule(List.of("hotline", "lien he", "email", "so dien thoai", "phan hoi"),
                    "Bạn gọi hotline 0900 000 000 (8h–20h mỗi ngày) hoặc gửi email hi@ecomart.vn. "
                    + "Đội ngũ EcoMart luôn sẵn sàng hỗ trợ bạn!", false),
            new Rule(List.of("dang ky", "dang nhap", "tai khoan", "mat khau", "quen mat khau", "dang xuat"),
                    "Bạn có thể đăng ký / đăng nhập nhanh ở góc phải phía trên trang chủ. "
                    + "Nếu quên mật khẩu, gọi hotline 0900 000 000 để được cấp lại nhé!", false)
    );

    private static final String DEFAULT_REPLY = "Mình chưa hiểu rõ câu hỏi của bạn. "
            + "Bạn có thể thử hỏi về sản phẩm (ví dụ “có cà rốt không?”), giá cả, giao hàng, thanh toán, "
            + "hoặc gọi hotline 0900 000 000 để được hỗ trợ trực tiếp nhé!";

    private final ProductRepository productRepository;

    public ChatBot(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public String answer(String question) {
        String normalized = normalize(question);
        if (normalized.isEmpty()) {
            return DEFAULT_REPLY;
        }

        for (Rule rule : RULES) {
            if (matchesAny(rule.keywords(), normalized)) {
                if (rule.includeProducts()) {
                    List<Product> hits = retrieve(normalized);
                    if (!hits.isEmpty()) {
                        return rule.reply() + "\n\n" + formatProducts(hits);
                    }
                }
                return rule.reply();
            }
        }

        List<Product> hits = retrieve(normalized);
        return hits.isEmpty() ? DEFAULT_REPLY : formatProducts(hits);
    }

    private boolean matchesAny(List<String> keywords, String normalized) {
        Set<String> tokens = Set.of(normalized.split(" "));
        for (String keyword : keywords) {
            if (!keyword.contains(" ")) {
                if (tokens.contains(keyword)) {
                    return true;
                }
            } else if (normalized.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private List<Product> retrieve(String normalized) {
        Set<String> tokens = new HashSet<>();
        for (String token : normalized.split(" ")) {
            if (token.length() >= 3 && !STOPWORDS.contains(token)) {
                tokens.add(token);
            }
        }

        List<ScoredProduct> scored = new ArrayList<>();
        for (Product product : productRepository.findAllByIsActiveTrue()) {
            String name = normalize(product.getName());
            String category = normalize(product.getCategory() == null ? "" : product.getCategory().getName());
            String description = normalize(product.getDescription());

            int score = 0;
            if (!normalized.isEmpty() && name.contains(normalized)) {
                score += 3;
            }
            if (!normalized.isEmpty() && category.contains(normalized)) {
                score += 3;
            }
            for (String token : tokens) {
                if (name.contains(token)) {
                    score += 2;
                } else if (category.contains(token)) {
                    score += 2;
                } else if (description.contains(token)) {
                    score += 1;
                }
            }
            if (score > 0) {
                scored.add(new ScoredProduct(product, score));
            }
        }

        return scored.stream()
                .sorted((a, b) -> Integer.compare(b.score(), a.score()))
                .limit(MAX_RESULTS)
                .map(ScoredProduct::product)
                .toList();
    }

    private String formatProducts(List<Product> products) {
        StringBuilder sb = new StringBuilder("Mình tìm thấy một số sản phẩm phù hợp:");
        for (Product product : products) {
            String category = product.getCategory() == null ? "" : " (" + product.getCategory().getName() + ")";
            sb.append("\n• ").append(product.getName()).append(category).append(" — ").append(formatPrice(product.getPrice()));
        }
        sb.append("\nBạn xem thêm chi tiết tại trang Sản phẩm nhé!");
        return sb.toString();
    }

    private String formatPrice(double price) {
        return VND.format(price) + " đ";
    }

    static String normalize(String text) {
        if (text == null) {
            return "";
        }
        String lowered = text.toLowerCase(Locale.ROOT);
        String decomposed = Normalizer.normalize(lowered, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replaceAll("[^a-z0-9 ]+", " ")
                .replaceAll("\\s+", " ")
                .trim();
        return decomposed;
    }

    private record ScoredProduct(Product product, int score) {}
}