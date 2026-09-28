package com.usman.resourcebooking.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import com.usman.resourcebooking.model.Reservation;
import com.usman.resourcebooking.model.ReservationStatus;
import com.usman.resourcebooking.model.Resource;
import com.usman.resourcebooking.model.Role;
import com.usman.resourcebooking.model.User;

@DataJpaTest
@DisplayName("ReservationRepository Integration Tests")
class ReservationRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ReservationRepository reservationRepository;

    private User user;
    private Resource resource;
    private Reservation reservation;

    @BeforeEach
    void setUp() {
        user = User.builder().username("testuser").email("test@example.com").password("pwd").role(Role.USER).build();
        entityManager.persist(user);

        resource = Resource.builder().name("Test Resource").type("Room").available(true).build();
        entityManager.persist(resource);

        reservation = Reservation.builder()
                .user(user)
                .resource(resource)
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(2))
                .price(BigDecimal.valueOf(100.00))
                .status(ReservationStatus.PENDING)
                .build();
        entityManager.persist(reservation);
        entityManager.flush();
        entityManager.clear(); // Clear context to force fetching from DB
    }

    @Test
    @DisplayName("findById uses @EntityGraph to eagerly fetch user and resource")
    void findById_WithEntityGraph() {
        Optional<Reservation> foundOpt = reservationRepository.findById(reservation.getId());
        assertTrue(foundOpt.isPresent());
        Reservation found = foundOpt.get();
        
        // They should be loaded without additional queries due to @EntityGraph
        assertNotNull(found.getUser().getUsername());
        assertNotNull(found.getResource().getName());
        assertEquals("testuser", found.getUser().getUsername());
        assertEquals("Test Resource", found.getResource().getName());
    }

    @Test
    @DisplayName("findAll uses @EntityGraph to eagerly fetch user and resource")
    void findAll_WithEntityGraph() {
        Page<Reservation> page = reservationRepository.findAll(ReservationSpecification.hasStatus(ReservationStatus.PENDING), PageRequest.of(0, 10));
        assertEquals(1, page.getTotalElements());
        Reservation found = page.getContent().get(0);
        
        // They should be loaded without additional queries due to @EntityGraph
        assertNotNull(found.getUser().getUsername());
        assertNotNull(found.getResource().getName());
        assertEquals("testuser", found.getUser().getUsername());
        assertEquals("Test Resource", found.getResource().getName());
    }
}
