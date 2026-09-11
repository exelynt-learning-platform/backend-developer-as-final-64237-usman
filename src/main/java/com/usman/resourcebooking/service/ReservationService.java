package com.usman.resourcebooking.service;

import com.usman.resourcebooking.dto.request.ReservationCreateRequest;
import com.usman.resourcebooking.dto.response.ReservationResponse;
import com.usman.resourcebooking.model.ReservationStatus;
import java.math.BigDecimal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;

/**
 * Contract for reservation CRUD and query operations.
 */
public interface ReservationService {

    ReservationResponse createReservation(ReservationCreateRequest request, Authentication authentication);

    Page<ReservationResponse> getReservations(ReservationStatus status,
                                              BigDecimal minPrice,
                                              BigDecimal maxPrice,
                                              Pageable pageable,
                                              Authentication authentication);

    ReservationResponse getReservationById(Long id, Authentication authentication);

    ReservationResponse updateReservation(Long id, ReservationCreateRequest request, ReservationStatus status, Authentication authentication);

    void deleteReservation(Long id, Authentication authentication);
}
