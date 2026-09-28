package com.usman.resourcebooking.service.impl;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.usman.resourcebooking.dto.request.ReservationCreateRequest;
import com.usman.resourcebooking.dto.response.ReservationResponse;
import com.usman.resourcebooking.exception.BadRequestException;
import com.usman.resourcebooking.exception.ConflictException;
import com.usman.resourcebooking.exception.ForbiddenException;
import com.usman.resourcebooking.exception.ResourceNotFoundException;
import com.usman.resourcebooking.model.Reservation;
import com.usman.resourcebooking.model.ReservationStatus;
import com.usman.resourcebooking.model.Resource;
import com.usman.resourcebooking.model.User;
import com.usman.resourcebooking.repository.ReservationRepository;
import com.usman.resourcebooking.repository.ReservationSpecification;
import com.usman.resourcebooking.repository.ResourceRepository;
import com.usman.resourcebooking.repository.UserRepository;
import com.usman.resourcebooking.security.SecurityUtils;
import com.usman.resourcebooking.security.UserPrincipal;
import com.usman.resourcebooking.service.ReservationService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReservationServiceImpl implements ReservationService {

        private static final List<ReservationStatus> ACTIVE_STATUSES = List
                        .of(ReservationStatus.PENDING, ReservationStatus.CONFIRMED);

        private final ReservationRepository reservationRepository;
        private final ResourceRepository resourceRepository;
        private final UserRepository userRepository;

        @Override
        @Transactional
        public ReservationResponse createReservation(ReservationCreateRequest request,
                        Authentication authentication) {
                UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();

                validateTimeOrdering(request);

                User user = userRepository.findById(principal.getId())
                                .orElseThrow(() -> new ResourceNotFoundException("User", "id", principal.getId()));

                Resource resource = resourceRepository.findByIdWithPessimisticWriteLock(request.getResourceId())
                                .orElseThrow(() -> new ResourceNotFoundException("Resource", "id",
                                                request.getResourceId()));

                validateResourceAvailability(resource);

                boolean isOverlapping = reservationRepository
                                .existsByResourceIdAndStatusInAndStartTimeLessThanAndEndTimeGreaterThan(
                                                request.getResourceId(),
                                                ACTIVE_STATUSES,
                                                request.getEndTime(),
                                                request.getStartTime());

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

                Specification<Reservation> spec = ReservationSpecification.hasStatus(status)
                                .and(ReservationSpecification.minPrice(minPrice))
                                .and(ReservationSpecification.maxPrice(maxPrice));

                if (!SecurityUtils.isAdmin(authentication)) {
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

                SecurityUtils.checkOwnership(reservation, authentication);

                return mapToResponse(reservation);
        }

        @Override
        @Transactional
        public ReservationResponse updateReservation(Long id, ReservationCreateRequest request,
                        Authentication authentication) {
                Reservation reservation = reservationRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("Reservation", "id", id));

                SecurityUtils.checkOwnership(reservation, authentication);

                validateTimeOrdering(request);

                if (!reservation.getResource().getId().equals(request.getResourceId())) {
                        Resource resource = resourceRepository.findByIdWithPessimisticWriteLock(request.getResourceId())
                                        .orElseThrow(() -> new ResourceNotFoundException("Resource", "id",
                                                        request.getResourceId()));
                        validateResourceAvailability(resource);
                        reservation.setResource(resource);
                }

                boolean isOverlapping = reservationRepository
                                .existsByResourceIdAndIdNotAndStatusInAndStartTimeLessThanAndEndTimeGreaterThan(
                                                request.getResourceId(),
                                                id,
                                                ACTIVE_STATUSES,
                                                request.getEndTime(),
                                                request.getStartTime());

                if (isOverlapping) {
                        throw new ConflictException("The resource is already booked during this time slot.");
                }

                reservation.setStartTime(request.getStartTime());
                reservation.setEndTime(request.getEndTime());
                reservation.setPrice(request.getPrice());

                return mapToResponse(reservationRepository.save(reservation));
        }

        @Override
        @Transactional
        public ReservationResponse updateReservationStatus(Long id, ReservationStatus status, Authentication authentication) {
                Reservation reservation = reservationRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("Reservation", "id", id));

                SecurityUtils.checkOwnership(reservation, authentication);

                if (status != null && status != reservation.getStatus()) {
                        if (!SecurityUtils.isAdmin(authentication)) {
                                if (reservation.getStatus() != ReservationStatus.PENDING
                                                || status != ReservationStatus.CANCELLED) {
                                        throw new ForbiddenException(
                                                        "Users can only cancel pending reservations. Other status transitions require admin privileges.");
                                }
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

                SecurityUtils.checkOwnership(reservation, authentication);

                reservationRepository.delete(reservation);
        }

        private void validateTimeOrdering(ReservationCreateRequest request) {
                if (!request.getEndTime().isAfter(request.getStartTime())) {
                        throw new BadRequestException("End time must be strictly after start time");
                }
        }

        private void validateResourceAvailability(Resource resource) {
                if (!resource.isAvailable()) {
                        throw new ConflictException("Resource '" + resource.getName()
                                        + "' is not currently available for booking");
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
