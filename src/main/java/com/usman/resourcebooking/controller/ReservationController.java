package com.usman.resourcebooking.controller;

import java.math.BigDecimal;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.usman.resourcebooking.dto.request.ReservationCreateRequest;
import com.usman.resourcebooking.dto.response.ApiResponse;
import com.usman.resourcebooking.dto.response.ReservationResponse;
import com.usman.resourcebooking.model.ReservationStatus;
import com.usman.resourcebooking.service.ReservationService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/reservations")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
public class ReservationController {

        private final ReservationService reservationService;

        @PostMapping
        public ResponseEntity<ApiResponse<ReservationResponse>> createReservation(
                        @Valid @RequestBody ReservationCreateRequest request,
                        Authentication authentication) {
                ReservationResponse reservation = reservationService.createReservation(request, authentication);
                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(ApiResponse.success("Reservation created successfully", reservation));
        }

        @GetMapping
        public ResponseEntity<ApiResponse<Page<ReservationResponse>>> getReservations(
                        @RequestParam(required = false) ReservationStatus status,
                        @RequestParam(required = false) BigDecimal minPrice,
                        @RequestParam(required = false) BigDecimal maxPrice,
                        @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
                        Authentication authentication) {
                Page<ReservationResponse> page = reservationService.getReservations(
                                status, minPrice, maxPrice, pageable, authentication);
                return ResponseEntity.ok(ApiResponse.success(page));
        }

        @GetMapping("/{id}")
        public ResponseEntity<ApiResponse<ReservationResponse>> getReservationById(
                        @PathVariable Long id,
                        Authentication authentication) {
                return ResponseEntity
                                .ok(ApiResponse.success(reservationService.getReservationById(id, authentication)));
        }

        @PutMapping("/{id}")
        public ResponseEntity<ApiResponse<ReservationResponse>> updateReservation(
                        @PathVariable Long id,
                        @Valid @RequestBody ReservationCreateRequest request,
                        Authentication authentication) {
                ReservationResponse reservation = reservationService.updateReservation(id, request, authentication);
                return ResponseEntity.ok(ApiResponse.success("Reservation updated successfully", reservation));
        }

        @PatchMapping("/{id}/status")
        public ResponseEntity<ApiResponse<ReservationResponse>> updateReservationStatus(
                        @PathVariable Long id,
                        @RequestParam ReservationStatus status,
                        Authentication authentication) {
                ReservationResponse reservation = reservationService.updateReservationStatus(id, status, authentication);
                return ResponseEntity.ok(ApiResponse.success("Reservation status updated successfully", reservation));
        }

        @DeleteMapping("/{id}")
        public ResponseEntity<ApiResponse<Void>> deleteReservation(
                        @PathVariable Long id,
                        Authentication authentication) {
                reservationService.deleteReservation(id, authentication);
                return ResponseEntity.ok(ApiResponse.success("Reservation deleted successfully"));
        }
}
