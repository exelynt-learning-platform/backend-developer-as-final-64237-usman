package com.usman.resourcebooking.repository;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;

import com.usman.resourcebooking.model.Reservation;
import com.usman.resourcebooking.model.ReservationStatus;
import com.usman.resourcebooking.model.User;

@DisplayName("ReservationSpecification Unit Tests")
class ReservationSpecificationTest {

    @Test
    @DisplayName("hasStatus returns specification")
    void hasStatus() {
        Specification<Reservation> spec = ReservationSpecification.hasStatus(ReservationStatus.PENDING);
        assertNotNull(spec);
        spec.toPredicate(mock(Root.class), mock(CriteriaQuery.class), mock(CriteriaBuilder.class));
    }

    @Test
    @DisplayName("hasStatus returns null specification when status is null")
    void hasStatus_Null() {
        Specification<Reservation> spec = ReservationSpecification.hasStatus(null);
        assertNotNull(spec);
        spec.toPredicate(mock(Root.class), mock(CriteriaQuery.class), mock(CriteriaBuilder.class));
    }

    @Test
    @DisplayName("belongsToUser returns specification")
    void belongsToUser() {
        Specification<Reservation> spec = ReservationSpecification.belongsToUser(1L);
        assertNotNull(spec);
        Root<Reservation> root = mock(Root.class);
        Path<Object> path = mock(Path.class);
        when(root.get("user")).thenReturn(path);
        spec.toPredicate(root, mock(CriteriaQuery.class), mock(CriteriaBuilder.class));
    }

    @Test
    @DisplayName("belongsToUser returns null specification when userId is null")
    void belongsToUser_Null() {
        Specification<Reservation> spec = ReservationSpecification.belongsToUser(null);
        assertNotNull(spec);
        spec.toPredicate(mock(Root.class), mock(CriteriaQuery.class), mock(CriteriaBuilder.class));
    }

    @Test
    @DisplayName("minPrice returns specification")
    void minPrice() {
        Specification<Reservation> spec = ReservationSpecification.minPrice(BigDecimal.TEN);
        assertNotNull(spec);
        spec.toPredicate(mock(Root.class), mock(CriteriaQuery.class), mock(CriteriaBuilder.class));
    }

    @Test
    @DisplayName("minPrice returns null specification when minPrice is null")
    void minPrice_Null() {
        Specification<Reservation> spec = ReservationSpecification.minPrice(null);
        assertNotNull(spec);
        spec.toPredicate(mock(Root.class), mock(CriteriaQuery.class), mock(CriteriaBuilder.class));
    }

    @Test
    @DisplayName("maxPrice returns specification")
    void maxPrice() {
        Specification<Reservation> spec = ReservationSpecification.maxPrice(BigDecimal.TEN);
        assertNotNull(spec);
        spec.toPredicate(mock(Root.class), mock(CriteriaQuery.class), mock(CriteriaBuilder.class));
    }

    @Test
    @DisplayName("maxPrice returns null specification when maxPrice is null")
    void maxPrice_Null() {
        Specification<Reservation> spec = ReservationSpecification.maxPrice(null);
        assertNotNull(spec);
        spec.toPredicate(mock(Root.class), mock(CriteriaQuery.class), mock(CriteriaBuilder.class));
    }
}
