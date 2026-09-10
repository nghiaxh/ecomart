package com.ecomart.security;

import com.ecomart.AbstractPostgresIntegrationTest;
import com.ecomart.domain.enums.UserRole;
import com.ecomart.dto.request.CreateUserRequest;
import com.ecomart.dto.request.ProductRequest;
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

@SpringBootTest
@AutoConfigureMockMvc
class AdminAuthorizationIntegrationTest extends AbstractPostgresIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    private ProductRequest product() {
        return new ProductRequest("Bơ", "bo", null, 25000.0, 10, 0.5, "Việt Nam", 1L, true,
                new ArrayList<>(), new ArrayList<>());
    }

    private CreateUserRequest user() {
        return new CreateUserRequest("quanlynew", "quanlynew@ecomart.vn", "0900000099",
                "secret123", UserRole.CUSTOMER, null);
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
}