package com.usman.resourcebooking.repository;

import com.usman.resourcebooking.model.Reservation;
import com.usman.resourcebooking.model.ReservationStatus;
import java.math.BigDecimal;
import org.springframework.data.jpa.domain.Specification;

public final class ReservationSpecification {

    private ReservationSpecification() {
    }

    public static Specification<Reservation> hasStatus(ReservationStatus status) {
        return (root, query, cb) -> status == null ? null : cb.equal(root.get("status"), status);
    }

    public static Specification<Reservation> minPrice(BigDecimal min) {
        return (root, query, cb) -> min == null ? null : cb.greaterThanOrEqualTo(root.get("price"), min);
    }

    public static Specification<Reservation> maxPrice(BigDecimal max) {
        return (root, query, cb) -> max == null ? null : cb.lessThanOrEqualTo(root.get("price"), max);
    }

    public static Specification<Reservation> belongsToUser(Long userId) {
        return (root, query, cb) -> userId == null ? null : cb.equal(root.get("user").get("id"), userId);
    }
}
