package com.usman.resourcebooking.repository;

import com.usman.resourcebooking.model.Reservation;
import com.usman.resourcebooking.model.ReservationStatus;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

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
}