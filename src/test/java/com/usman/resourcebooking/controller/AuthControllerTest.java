package com.usman.resourcebooking.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.usman.resourcebooking.dto.request.LoginRequest;
import com.usman.resourcebooking.dto.request.RegisterRequest;
import com.usman.resourcebooking.dto.response.AuthResponse;
import com.usman.resourcebooking.service.AuthService;

/**
 * Integration tests for {@link AuthController}.
 * Uses full Spring Boot context with real security filter chain.
 * Only the service layer is mocked.
 */
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("AuthController Integration Tests")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    // ── POST /auth/register ─────────────────────────────────────────────

    @Nested
    @DisplayName("POST /auth/register")
    class RegisterTests {

        @Test
        @DisplayName("201 CREATED — valid registration")
        void register_ValidPayload_Returns201() throws Exception {
            RegisterRequest request = new RegisterRequest();
            request.setUsername("newuser");
            request.setEmail("new@example.com");
            request.setPassword("password123");

            given(authService.register(any(RegisterRequest.class)))
                    .willReturn("Registration successful. You can now log in.");

            mockMvc.perform(post("/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Registration successful. You can now log in."));
        }

        @Test
        @DisplayName("400 BAD REQUEST — missing username")
        void register_MissingUsername_Returns400() throws Exception {
            RegisterRequest request = new RegisterRequest();
            request.setEmail("new@example.com");
            request.setPassword("password123");

            mockMvc.perform(post("/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("400 BAD REQUEST — invalid email format")
        void register_InvalidEmail_Returns400() throws Exception {
            RegisterRequest request = new RegisterRequest();
            request.setUsername("newuser");
            request.setEmail("not-an-email");
            request.setPassword("password123");

            mockMvc.perform(post("/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("400 BAD REQUEST — password too short")
        void register_ShortPassword_Returns400() throws Exception {
            RegisterRequest request = new RegisterRequest();
            request.setUsername("newuser");
            request.setEmail("new@example.com");
            request.setPassword("12");

            mockMvc.perform(post("/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    // ── POST /auth/login ────────────────────────────────────────────────

    @Nested
    @DisplayName("POST /auth/login")
    class LoginTests {

        @Test
        @DisplayName("200 OK — valid credentials return JWT + role")
        void login_ValidCredentials_Returns200WithToken() throws Exception {
            LoginRequest request = new LoginRequest();
            request.setUsername("admin");
            request.setPassword("adminpassword");

            AuthResponse response = new AuthResponse("mock.jwt.token", "ROLE_ADMIN");

            given(authService.login(any(LoginRequest.class))).willReturn(response);

            mockMvc.perform(post("/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.accessToken").value("mock.jwt.token"))
                    .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                    .andExpect(jsonPath("$.data.role").value("ROLE_ADMIN"));
        }

        @Test
        @DisplayName("400 BAD REQUEST — blank username")
        void login_BlankUsername_Returns400() throws Exception {
            LoginRequest request = new LoginRequest();
            request.setPassword("password");

            mockMvc.perform(post("/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("400 BAD REQUEST — blank password")
        void login_BlankPassword_Returns400() throws Exception {
            LoginRequest request = new LoginRequest();
            request.setUsername("admin");

            mockMvc.perform(post("/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    // ── Auth-exempt endpoint accessibility ───────────────────────────────

    @Nested
    @DisplayName("Security — public endpoints")
    class SecurityTests {

        @Test
        @DisplayName("/auth/** endpoints are accessible without JWT")
        void authEndpoints_NoToken_NotBlocked() throws Exception {
            LoginRequest request = new LoginRequest();
            request.setUsername("user");
            request.setPassword("pass");

            given(authService.login(any(LoginRequest.class)))
                    .willReturn(new AuthResponse("token", "ROLE_USER"));

            // If JWT were required this would be 401; we expect 200
            mockMvc.perform(post("/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());
        }
    }
}
