package com.ecomart.security;

import com.ecomart.AbstractPostgresIntegrationTest;
import com.ecomart.domain.entity.Category;
import com.ecomart.domain.enums.UserRole;
import com.ecomart.dto.request.CreateUserRequest;
import com.ecomart.dto.request.ProductRequest;
import com.ecomart.repository.CategoryRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicLong;

@SpringBootTest
@AutoConfigureMockMvc
class AdminAuthorizationIntegrationTest extends AbstractPostgresIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    CategoryRepository categoryRepository;

    private static final AtomicLong counter = new AtomicLong();

    private ProductRequest product() {
        String name = "Bơ " + counter.incrementAndGet();
        long categoryId = seedCategory(name);
        return new ProductRequest(name, "bo-" + counter.incrementAndGet(), null, 25000.0, 10, 0.5,
                "Việt Nam", categoryId, true, new ArrayList<>(), new ArrayList<>());
    }

    private long seedCategory(String baseName) {
        Category category = new Category();
        category.setName(baseName);
        category.setSlug(baseName.toLowerCase(Locale.ROOT).replace(' ', '-') + "-" + counter.incrementAndGet());
        category.setIcon("leaf");
        category.setDisplayOrder(99);
        category.setActive(true);
        return categoryRepository.save(category).getId();
    }

    private CreateUserRequest user() {
        return new CreateUserRequest("quanlynew" + counter.incrementAndGet(), "quanlynew" + counter.incrementAndGet() + "@ecomart.vn",
                "0900000099", "secret123", UserRole.CUSTOMER, null);
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void nonAdminCannotCreateProduct() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(product())))
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    void anonymousCannotCreateProduct() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(product())))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void nonAdminCannotCreateUser() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user())))
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    void anonymousCannotCreateUser() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user())))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void nonAdminCannotReadStatistics() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/admin/statistics"))
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    void anonymousCannotReadStatistics() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/admin/statistics"))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "STAFF")
    void staffCanCreateProduct() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(product())))
                .andExpect(MockMvcResultMatchers.status().isCreated());
    }

    @Test
    @WithMockUser(roles = "STAFF")
    void staffCanCreateCategory() throws Exception {
        String name = "Đồ uống xanh " + counter.incrementAndGet();
        mockMvc.perform(MockMvcRequestBuilders.post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"" + name + "\",\"active\":true}"))
                .andExpect(MockMvcResultMatchers.status().isCreated());
    }

    @Test
    @WithMockUser(roles = "STAFF")
    void staffCanReadStatistics() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/admin/statistics"))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithMockUser(roles = "STAFF")
    void staffCanReadDashboard() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/admin/dashboard"))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithMockUser(roles = "STAFF")
    void staffCannotCreateUser() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user())))
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "STAFF")
    void staffCannotReadActivityLogs() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/admin/activity-logs"))
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminCanReadActivityLogs() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/admin/activity-logs"))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }
}