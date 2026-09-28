package com.usman.resourcebooking.repository;

import com.usman.resourcebooking.model.Reservation;
import com.usman.resourcebooking.model.ReservationStatus;

import java.math.BigDecimal;

import org.springframework.data.jpa.domain.Specification;

/**
 * Reusable JPA Specification builders for dynamic Reservation queries.
 * Returns {@code cb.conjunction()} (always-true predicate) when the filter
 * argument is {@code null}, instead of returning {@code null} directly.
 */
public final class ReservationSpecification {

    private ReservationSpecification() {
    }

    public static Specification<Reservation> hasStatus(ReservationStatus status) {
        return (root, query, cb) -> status == null ? cb.conjunction() : cb.equal(root.get("status"), status);
    }

    public static Specification<Reservation> minPrice(BigDecimal min) {
        return (root, query, cb) -> min == null ? cb.conjunction() : cb.greaterThanOrEqualTo(root.get("price"), min);
    }

    public static Specification<Reservation> maxPrice(BigDecimal max) {
        return (root, query, cb) -> max == null ? cb.conjunction() : cb.lessThanOrEqualTo(root.get("price"), max);
    }

    public static Specification<Reservation> belongsToUser(Long userId) {
        return (root, query, cb) -> userId == null ? cb.conjunction() : cb.equal(root.get("user").get("id"), userId);
    }
}
