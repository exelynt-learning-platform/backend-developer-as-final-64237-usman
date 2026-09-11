package com.usman.resourcebooking.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.usman.resourcebooking.dto.request.ReservationCreateRequest;
import com.usman.resourcebooking.dto.response.ReservationResponse;
import com.usman.resourcebooking.model.ReservationStatus;
import com.usman.resourcebooking.model.Role;
import com.usman.resourcebooking.model.User;
import com.usman.resourcebooking.security.UserPrincipal;
import com.usman.resourcebooking.service.ReservationService;

/**
 * Integration tests for {@link ReservationController}.
 * Uses full Spring Boot context with real security filter chain.
 * Only the service layer is mocked.
 */
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("ReservationController Integration Tests")
class ReservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ReservationService reservationService;

    // ── Helpers ──────────────────────────────────────────────────────────

    private UsernamePasswordAuthenticationToken userAuth() {
        User user = User.builder().id(1L).username("user1").email("u@x.com").password("p").role(Role.USER).build();
        UserPrincipal principal = UserPrincipal.create(user);
        return new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
    }

    private UsernamePasswordAuthenticationToken adminAuth() {
        User admin = User.builder().id(2L).username("admin").email("a@x.com").password("p").role(Role.ADMIN).build();
        UserPrincipal principal = UserPrincipal.create(admin);
        return new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
    }

    private ReservationCreateRequest validRequest() {
        ReservationCreateRequest req = new ReservationCreateRequest();
        req.setResourceId(1L);
        req.setStartTime(LocalDateTime.of(2026, 10, 1, 9, 0));
        req.setEndTime(LocalDateTime.of(2026, 10, 1, 10, 0));
        req.setPrice(BigDecimal.valueOf(50.00));
        return req;
    }

    private ReservationResponse sampleResponse() {
        return ReservationResponse.builder()
                .id(1L)
                .startTime(LocalDateTime.of(2026, 10, 1, 9, 0))
                .endTime(LocalDateTime.of(2026, 10, 1, 10, 0))
                .price(BigDecimal.valueOf(50.00))
                .status(ReservationStatus.PENDING)
                .resourceId(1L)
                .resourceName("Room A")
                .resourceType("ROOM")
                .userId(1L)
                .username("user1")
                .build();
    }

    // ── POST /reservations ──────────────────────────────────────────────

    @Nested
    @DisplayName("POST /reservations")
    class CreateReservationTests {

        @Test
        @DisplayName("201 CREATED — authenticated USER can create reservation")
        void create_AuthenticatedUser_Returns201() throws Exception {
            given(reservationService.createReservation(any(), any()))
                    .willReturn(sampleResponse());

            mockMvc.perform(post("/reservations")
                    .with(authentication(userAuth()))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validRequest())))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(1))
                    .andExpect(jsonPath("$.data.status").value("PENDING"))
                    .andExpect(jsonPath("$.data.username").value("user1"));
        }

        @Test
        @DisplayName("201 CREATED — authenticated ADMIN can also create reservation")
        void create_AuthenticatedAdmin_Returns201() throws Exception {
            given(reservationService.createReservation(any(), any()))
                    .willReturn(sampleResponse());

            mockMvc.perform(post("/reservations")
                    .with(authentication(adminAuth()))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validRequest())))
                    .andExpect(status().isCreated());
        }

        @Test
        @DisplayName("401 UNAUTHORIZED — no JWT token")
        void create_NoAuth_Returns401() throws Exception {
            mockMvc.perform(post("/reservations")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validRequest())))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("400 BAD REQUEST — missing resourceId")
        void create_MissingResourceId_Returns400() throws Exception {
            ReservationCreateRequest req = validRequest();
            req.setResourceId(null);

            mockMvc.perform(post("/reservations")
                    .with(authentication(userAuth()))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("400 BAD REQUEST — missing start time")
        void create_MissingStartTime_Returns400() throws Exception {
            ReservationCreateRequest req = validRequest();
            req.setStartTime(null);

            mockMvc.perform(post("/reservations")
                    .with(authentication(userAuth()))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("400 BAD REQUEST — negative price")
        void create_NegativePrice_Returns400() throws Exception {
            ReservationCreateRequest req = validRequest();
            req.setPrice(BigDecimal.valueOf(-10));

            mockMvc.perform(post("/reservations")
                    .with(authentication(userAuth()))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest());
        }
    }

    // ── GET /reservations ───────────────────────────────────────────────

    @Nested
    @DisplayName("GET /reservations")
    class GetReservationsTests {

        @Test
        @DisplayName("200 OK — USER sees own reservations")
        void get_AuthenticatedUser_Returns200() throws Exception {
            Page<ReservationResponse> page = new PageImpl<>(
                    Collections.singletonList(sampleResponse()),
                    PageRequest.of(0, 10), 1);

            given(reservationService.getReservations(any(), any(), any(), any(), any()))
                    .willReturn(page);

            mockMvc.perform(get("/reservations")
                    .with(authentication(userAuth())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content").isArray());
        }

        @Test
        @DisplayName("200 OK — ADMIN sees all reservations")
        void get_AuthenticatedAdmin_Returns200() throws Exception {
            Page<ReservationResponse> page = new PageImpl<>(
                    Collections.singletonList(sampleResponse()),
                    PageRequest.of(0, 10), 1);

            given(reservationService.getReservations(any(), any(), any(), any(), any()))
                    .willReturn(page);

            mockMvc.perform(get("/reservations")
                    .with(authentication(adminAuth())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("401 UNAUTHORIZED — no JWT token")
        void get_NoAuth_Returns401() throws Exception {
            mockMvc.perform(get("/reservations"))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ── GET /reservations/{id} ──────────────────────────────────────────

    @Nested
    @DisplayName("GET /reservations/{id}")
    class GetReservationByIdTests {

        @Test
        @DisplayName("200 OK — USER can view their reservation")
        void getById_Returns200() throws Exception {
            given(reservationService.getReservationById(eq(1L), any()))
                    .willReturn(sampleResponse());

            mockMvc.perform(get("/reservations/1")
                    .with(authentication(userAuth())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id").value(1));
        }
    }

    // ── PUT /reservations/{id} ──────────────────────────────────────────

    @Nested
    @DisplayName("PUT /reservations/{id}")
    class UpdateReservationTests {

        @Test
        @DisplayName("200 OK — USER can update their reservation")
        void update_Returns200() throws Exception {
            given(reservationService.updateReservation(eq(1L), any(), any(), any()))
                    .willReturn(sampleResponse());

            mockMvc.perform(put("/reservations/1")
                    .with(authentication(userAuth()))
                    .param("status", "CANCELLED")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validRequest())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id").value(1));
        }
    }

    // ── DELETE /reservations/{id} ───────────────────────────────────────

    @Nested
    @DisplayName("DELETE /reservations/{id}")
    class DeleteReservationTests {

        @Test
        @DisplayName("200 OK — USER can delete their reservation")
        void delete_Returns200() throws Exception {
            mockMvc.perform(delete("/reservations/1")
                    .with(authentication(userAuth())))
                    .andExpect(status().isOk());
        }
    }
}
