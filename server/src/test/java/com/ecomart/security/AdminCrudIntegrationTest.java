package com.ecomart.security;

import com.ecomart.AbstractPostgresIntegrationTest;
import com.ecomart.domain.entity.Admin;
import com.ecomart.domain.enums.UserRole;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AdminCrudIntegrationTest extends AbstractPostgresIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    private static final AtomicLong counter = new AtomicLong();

    private String adminToken() throws Exception {
        Admin admin = new Admin();
        admin.setUsername("quanly" + counter.incrementAndGet());
        admin.setEmail("quanly" + counter.incrementAndGet() + "@ecomart.test");
        admin.setNumberPhone("0911110000");
        admin.setPasswordHash(passwordEncoder.encode("admin123"));
        admin.setRole(UserRole.ADMIN);
        admin.setActive(true);
        userRepository.save(admin);

        var result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"identifier":"%s","password":"admin123"}
                                """.formatted(admin.getEmail())))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("token").asText();
    }

    private long createCategory(String token) throws Exception {
        long n = counter.incrementAndGet();
        var result = mockMvc.perform(post("/api/categories")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Danh mục %d","slug":"danh-muc-%d","active":true}
                                """.formatted(n, n)))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }

    @Test
    void productCrudRoundTrip() throws Exception {
        String token = adminToken();
        long categoryId = createCategory(token);
        long n = counter.get();

        var createResult = mockMvc.perform(post("/api/products")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Bột giặt %d","slug":"bot-giat-%d","price":120000,"stock":5,"categoryId":%d,"active":true}
                                """.formatted(n, n, categoryId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.active").value(true))
                .andReturn();
        long productId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asLong();
        String slug = "bot-giat-" + n;

        mockMvc.perform(get("/api/products/" + productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Bột giặt " + n));

        mockMvc.perform(get("/api/products/slug/" + slug))
                .andExpect(status().isOk());

        mockMvc.perform(patch("/api/products/" + productId + "/toggle")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));

        mockMvc.perform(delete("/api/products/" + productId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Product deleted"));

        mockMvc.perform(get("/api/products/" + productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    void categoryWithProductsCannotBeDeleted() throws Exception {
        String token = adminToken();
        long categoryId = createCategory(token);
        long n = counter.get();

        mockMvc.perform(post("/api/products")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Sản phẩm %d","slug":"sp-%d","price":50000,"stock":2,"categoryId":%d,"active":true}
                                """.formatted(n, n, categoryId)))
                .andExpect(status().isCreated());

        mockMvc.perform(delete("/api/categories/" + categoryId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Cannot delete a category that contains products"));
    }
}