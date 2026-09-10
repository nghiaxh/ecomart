package com.ecomart.security;

import com.ecomart.AbstractPostgresIntegrationTest;
import com.ecomart.domain.entity.Admin;
import com.ecomart.domain.entity.Category;
import com.ecomart.domain.entity.Product;
import com.ecomart.domain.entity.User;
import com.ecomart.domain.enums.UserRole;
import com.ecomart.repository.CategoryRepository;
import com.ecomart.repository.ProductRepository;
import com.ecomart.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CommerceFlowIntegrationTest extends AbstractPostgresIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    CategoryRepository categoryRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    private static final AtomicLong counter = new AtomicLong();

    private String register(String email) throws Exception {
        long n = counter.incrementAndGet();
        String body = """
                {"username":"mua%d","email":"%s","numberPhone":"0901234567","password":"secret123"}
                """.formatted(n, email);
        var result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("token").asText();
    }

    private long seedProduct(double price, int stock) {
        long n = counter.incrementAndGet();
        Category category = new Category();
        category.setName("Hàng mới " + n);
        category.setSlug("hang-moi-" + n);
        categoryRepository.save(category);

        Product product = new Product();
        product.setCategory(category);
        product.setName("Sản phẩm " + n);
        product.setSlug("san-pham-" + n);
        product.setPrice(price);
        product.setStock(stock);
        product.setActive(true);
        productRepository.save(product);
        return product.getId();
    }

    private long addAddress(String token) throws Exception {
        var result = mockMvc.perform(post("/api/addresses")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"label":"Nhà","street":"12 Lê Lợi","ward":"Bến Nghé","district":"Quận 1","city":"TP.HCM","receiverName":"Minh","receiverPhone":"0901234567","isDefault":true}
                                """))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }

    private long checkoutCod(String token, long addressId) throws Exception {
        var result = mockMvc.perform(post("/api/orders/checkout")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"addressId":%d,"paymentMethod":"COD"}
                                """.formatted(addressId)))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("orderId").asLong();
    }

    @Test
    void checkoutCodFullFlowUpdatesStockAndShowsInMyOrders() throws Exception {
        long productId = seedProduct(25000, 10);
        String email = "cod" + counter.incrementAndGet() + "@ecomart.test";
        String token = register(email);

        mockMvc.perform(post("/api/cart")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":%d,\"quantity\":2}".formatted(productId)))
                .andExpect(status().isOk());

        long addressId = addAddress(token);
        mockMvc.perform(post("/api/orders/checkout")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"addressId":%d,"paymentMethod":"COD"}
                                """.formatted(addressId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.message").value("Order placed successfully, pay on delivery"));

        mockMvc.perform(get("/api/orders/mine").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].status").value("PENDING"))
                .andExpect(jsonPath("$.content[0].subtotal").value(50000))
                .andExpect(jsonPath("$.content[0].shippingFee").value(20000))
                .andExpect(jsonPath("$.content[0].total").value(70000));

        Product reloaded = productRepository.findById(productId).orElseThrow();
        assertThat(reloaded.getStock()).isEqualTo(8);
    }

    @Test
    void foreignCustomerCannotAccessOrder() throws Exception {
        long productId = seedProduct(30000, 5);
        String emailA = "chu" + counter.incrementAndGet() + "@ecomart.test";
        String emailB = "khachl" + counter.incrementAndGet() + "@ecomart.test";
        String tokenA = register(emailA);
        String tokenB = register(emailB);

        mockMvc.perform(post("/api/cart")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":%d,\"quantity\":1}".formatted(productId)))
                .andExpect(status().isOk());
        long addressId = addAddress(tokenA);
        long orderId = checkoutCod(tokenA, addressId);

        mockMvc.perform(get("/api/orders/" + orderId).header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Access denied"));
    }

    @Test
    void hiddenReviewIsHiddenFromPublicList() throws Exception {
        long productId = seedProduct(15000, 3);
        String customerEmail = "review" + counter.incrementAndGet() + "@ecomart.test";
        String customerToken = register(customerEmail);

        var createResult = mockMvc.perform(post("/api/reviews")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":%d,\"rating\":4,\"content\":\"Sạch\"}".formatted(productId)))
                .andExpect(status().isCreated())
                .andReturn();
        long reviewId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(get("/api/reviews").param("productId", String.valueOf(productId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        Admin admin = new Admin();
        admin.setUsername("duyet" + counter.incrementAndGet());
        admin.setEmail("duyet" + counter.incrementAndGet() + "@ecomart.test");
        admin.setNumberPhone("0901112233");
        admin.setPasswordHash(passwordEncoder.encode("admin123"));
        admin.setRole(UserRole.ADMIN);
        admin.setActive(true);
        userRepository.save(admin);

        String loginBody = """
                {"identifier":"%s","password":"admin123"}
                """.formatted(admin.getEmail());
        var loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody))
                .andExpect(status().isOk())
                .andReturn();
        String adminToken = objectMapper.readTree(loginResult.getResponse().getContentAsString()).get("token").asText();

        mockMvc.perform(patch("/api/reviews/" + reviewId + "/toggle")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hidden").value(true));

        mockMvc.perform(get("/api/reviews").param("productId", String.valueOf(productId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        mockMvc.perform(get("/api/reviews")
                        .param("productId", String.valueOf(productId))
                        .param("includeHidden", "true")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }
}