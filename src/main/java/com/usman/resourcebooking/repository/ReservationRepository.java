package com.usman.resourcebooking.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.usman.resourcebooking.model.Reservation;
import com.usman.resourcebooking.model.ReservationStatus;

public interface ReservationRepository
                extends JpaRepository<Reservation, Long>,
                JpaSpecificationExecutor<Reservation> {

        boolean existsByResourceIdAndStatusInAndStartTimeLessThanAndEndTimeGreaterThan(
                        Long resourceId,
                        List<ReservationStatus> statuses,
                        LocalDateTime endTime,
                        LocalDateTime startTime);

        boolean existsByResourceIdAndIdNotAndStatusInAndStartTimeLessThanAndEndTimeGreaterThan(
                        Long resourceId,
                        Long id,
                        List<ReservationStatus> statuses,
                        LocalDateTime endTime,
                        LocalDateTime startTime);

        @Override
        @EntityGraph(attributePaths = { "resource", "user" })
        Optional<Reservation> findById(Long id);

        @EntityGraph(attributePaths = { "resource", "user" })
        Page<Reservation> findAll(Specification<Reservation> spec, Pageable pageable);
}