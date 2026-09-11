package com.usman.resourcebooking.service.impl;

import com.usman.resourcebooking.dto.request.ReservationCreateRequest;
import com.usman.resourcebooking.dto.response.ReservationResponse;
import com.usman.resourcebooking.exception.BadRequestException;
import com.usman.resourcebooking.exception.ConflictException;
import com.usman.resourcebooking.exception.ResourceNotFoundException;
import com.usman.resourcebooking.model.Reservation;
import com.usman.resourcebooking.model.ReservationStatus;
import com.usman.resourcebooking.model.Resource;
import com.usman.resourcebooking.model.User;
import com.usman.resourcebooking.repository.ReservationRepository;
import com.usman.resourcebooking.repository.ReservationSpecification;
import com.usman.resourcebooking.repository.ResourceRepository;
import com.usman.resourcebooking.repository.UserRepository;
import com.usman.resourcebooking.security.UserPrincipal;
import com.usman.resourcebooking.service.ReservationService;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReservationServiceImpl implements ReservationService {

        private final ReservationRepository reservationRepository;
        private final ResourceRepository resourceRepository;
        private final UserRepository userRepository;

        @Override
        @Transactional
        public ReservationResponse createReservation(ReservationCreateRequest request,
                        Authentication authentication) {
                UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();

                User user = userRepository.findById(principal.getId())
                                .orElseThrow(() -> new ResourceNotFoundException("User", "id", principal.getId()));

                Resource resource = resourceRepository.findById(request.getResourceId())
                                .orElseThrow(() -> new ResourceNotFoundException("Resource", "id",
                                                request.getResourceId()));

                if (!resource.isAvailable()) {
                        throw new ConflictException(
                                        "Resource '" + resource.getName() + "' is not currently available for booking");
                }

                if (!request.getEndTime().isAfter(request.getStartTime())) {
                        throw new BadRequestException("End time must be strictly after start time");
                }

                boolean isOverlapping = reservationRepository.existsByResourceIdAndStatusInAndStartTimeLessThanAndEndTimeGreaterThan(
                        request.getResourceId(),
                        java.util.List.of(ReservationStatus.PENDING, ReservationStatus.CONFIRMED),
                        request.getEndTime(),
                        request.getStartTime()
                );

                if (isOverlapping) {
                        throw new ConflictException("The resource is already booked during this time slot.");
                }

                Reservation reservation = Reservation.builder()
                                .startTime(request.getStartTime())
                                .endTime(request.getEndTime())
                                .price(request.getPrice())
                                .status(ReservationStatus.PENDING)
                                .user(user)
                                .resource(resource)
                                .build();

                return mapToResponse(reservationRepository.save(reservation));
        }

        @Override
        @Transactional(readOnly = true)
        public Page<ReservationResponse> getReservations(ReservationStatus status,
                        BigDecimal minPrice,
                        BigDecimal maxPrice,
                        Pageable pageable,
                        Authentication authentication) {
                UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();

                boolean isAdmin = authentication.getAuthorities().stream()
                                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

                Specification<Reservation> spec = ReservationSpecification.hasStatus(status)
                                .and(ReservationSpecification.minPrice(minPrice))
                                .and(ReservationSpecification.maxPrice(maxPrice));

                if (!isAdmin) {
                        spec = spec.and(ReservationSpecification.belongsToUser(principal.getId()));
                }
                return reservationRepository.findAll(spec, pageable)
                                .map(this::mapToResponse);
        }
        @Override
        @Transactional(readOnly = true)
        public ReservationResponse getReservationById(Long id, Authentication authentication) {
                Reservation reservation = reservationRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("Reservation", "id", id));

                checkOwnership(reservation, authentication);

                return mapToResponse(reservation);
        }

        @Override
        @Transactional
        public ReservationResponse updateReservation(Long id, ReservationCreateRequest request, ReservationStatus status, Authentication authentication) {
                Reservation reservation = reservationRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("Reservation", "id", id));

                checkOwnership(reservation, authentication);

                if (!reservation.getResource().getId().equals(request.getResourceId())) {
                        Resource resource = resourceRepository.findById(request.getResourceId())
                                        .orElseThrow(() -> new ResourceNotFoundException("Resource", "id", request.getResourceId()));
                        if (!resource.isAvailable()) {
                                throw new ConflictException("Resource '" + resource.getName() + "' is not currently available for booking");
                        }
                        reservation.setResource(resource);
                }

                if (!request.getEndTime().isAfter(request.getStartTime())) {
                        throw new BadRequestException("End time must be strictly after start time");
                }

                boolean isOverlapping = reservationRepository.existsByResourceIdAndIdNotAndStatusInAndStartTimeLessThanAndEndTimeGreaterThan(
                        request.getResourceId(),
                        id,
                        java.util.List.of(ReservationStatus.PENDING, ReservationStatus.CONFIRMED),
                        request.getEndTime(),
                        request.getStartTime()
                );

                if (isOverlapping) {
                        throw new ConflictException("The resource is already booked during this time slot.");
                }

                reservation.setStartTime(request.getStartTime());
                reservation.setEndTime(request.getEndTime());
                reservation.setPrice(request.getPrice());
                
                if (status != null) {
                        boolean isAdmin = authentication.getAuthorities().stream()
                                        .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
                        if (!isAdmin && status == ReservationStatus.CONFIRMED) {
                                throw new com.usman.resourcebooking.exception.ForbiddenException("Only administrators can confirm reservations.");
                        }
                        reservation.setStatus(status);
                }

                return mapToResponse(reservationRepository.save(reservation));
        }

        @Override
        @Transactional
        public void deleteReservation(Long id, Authentication authentication) {
                Reservation reservation = reservationRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("Reservation", "id", id));

                checkOwnership(reservation, authentication);

                reservationRepository.delete(reservation);
        }

        private void checkOwnership(Reservation reservation, Authentication authentication) {
                UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
                boolean isAdmin = authentication.getAuthorities().stream()
                                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

                if (!isAdmin && !reservation.getUser().getId().equals(principal.getId())) {
                        throw new com.usman.resourcebooking.exception.ForbiddenException("You do not have permission to access this reservation");
                }
        }

        private ReservationResponse mapToResponse(Reservation r) {
                return ReservationResponse.builder()
                                .id(r.getId())
                                .startTime(r.getStartTime())
                                .endTime(r.getEndTime())
                                .price(r.getPrice())
                                .status(r.getStatus())
                                .createdAt(r.getCreatedAt())
                                .resourceId(r.getResource().getId())
                                .resourceName(r.getResource().getName())
                                .resourceType(r.getResource().getType())
                                .userId(r.getUser().getId())
                                .username(r.getUser().getUsername())
                                .build();
        }
}
