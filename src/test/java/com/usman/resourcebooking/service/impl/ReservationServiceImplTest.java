package com.usman.resourcebooking.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import com.usman.resourcebooking.dto.request.ReservationCreateRequest;
import com.usman.resourcebooking.dto.response.ReservationResponse;
import com.usman.resourcebooking.exception.BadRequestException;
import com.usman.resourcebooking.exception.ConflictException;
import com.usman.resourcebooking.exception.ForbiddenException;
import com.usman.resourcebooking.model.Reservation;
import com.usman.resourcebooking.model.ReservationStatus;
import com.usman.resourcebooking.model.Resource;
import com.usman.resourcebooking.model.Role;
import com.usman.resourcebooking.model.User;
import com.usman.resourcebooking.repository.ReservationRepository;
import com.usman.resourcebooking.repository.ResourceRepository;
import com.usman.resourcebooking.repository.UserRepository;
import com.usman.resourcebooking.security.UserPrincipal;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReservationServiceImpl Unit Tests")
class ReservationServiceImplTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private ResourceRepository resourceRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ReservationServiceImpl reservationService;

    private User user;
    private User admin;
    private Resource resource;
    private Reservation reservation;
    private Authentication userAuth;
    private Authentication adminAuth;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).username("user").role(Role.USER).build();
        admin = User.builder().id(2L).username("admin").role(Role.ADMIN).build();

        UserPrincipal userPrincipal = UserPrincipal.create(user);
        userAuth = new UsernamePasswordAuthenticationToken(userPrincipal, null, userPrincipal.getAuthorities());

        UserPrincipal adminPrincipal = UserPrincipal.create(admin);
        adminAuth = new UsernamePasswordAuthenticationToken(adminPrincipal, null, adminPrincipal.getAuthorities());

        resource = Resource.builder().id(100L).name("Test Resource").available(true).build();

        reservation = Reservation.builder()
                .id(10L)
                .user(user)
                .resource(resource)
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(2))
                .price(BigDecimal.valueOf(100))
                .status(ReservationStatus.PENDING)
                .build();
    }

    @Test
    @DisplayName("Create Reservation - Success")
    void createReservation_Success() {
        ReservationCreateRequest req = new ReservationCreateRequest();
        req.setResourceId(100L);
        req.setStartTime(LocalDateTime.now().plusDays(1));
        req.setEndTime(LocalDateTime.now().plusDays(2));
        req.setPrice(BigDecimal.valueOf(100));

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(resourceRepository.findByIdWithPessimisticWriteLock(100L)).thenReturn(Optional.of(resource));
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(invocation -> {
            Reservation r = invocation.getArgument(0);
            r.setId(10L);
            return r;
        });

        ReservationResponse res = reservationService.createReservation(req, userAuth);
        assertNotNull(res);
        assertEquals(10L, res.getId());
        assertEquals(1L, res.getUserId());
    }

    @Test
    @DisplayName("Create Reservation - Fails when Resource Unavailable")
    void createReservation_ResourceUnavailable() {
        resource.setAvailable(false);
        ReservationCreateRequest req = new ReservationCreateRequest();
        req.setResourceId(100L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(resourceRepository.findByIdWithPessimisticWriteLock(100L)).thenReturn(Optional.of(resource));

        assertThrows(ConflictException.class, () -> reservationService.createReservation(req, userAuth));
    }

    @Test
    @DisplayName("Create Reservation - Fails when Dates Invalid")
    void createReservation_DatesInvalid() {
        ReservationCreateRequest req = new ReservationCreateRequest();
        req.setResourceId(100L);
        req.setStartTime(LocalDateTime.now().plusDays(2));
        req.setEndTime(LocalDateTime.now().plusDays(1)); // end before start

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(resourceRepository.findByIdWithPessimisticWriteLock(100L)).thenReturn(Optional.of(resource));

        assertThrows(BadRequestException.class, () -> reservationService.createReservation(req, userAuth));
    }

    @Test
    @DisplayName("Create Reservation - Fails when Overlapping")
    void createReservation_Overlapping_Conflict() {
        ReservationCreateRequest req = new ReservationCreateRequest();
        req.setResourceId(100L);
        req.setStartTime(LocalDateTime.now().plusDays(1));
        req.setEndTime(LocalDateTime.now().plusDays(2));
        req.setPrice(BigDecimal.valueOf(100));

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(resourceRepository.findByIdWithPessimisticWriteLock(100L)).thenReturn(Optional.of(resource));
        when(reservationRepository.existsByResourceIdAndStatusInAndStartTimeLessThanAndEndTimeGreaterThan(
                any(Long.class), any(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(true);

        assertThrows(ConflictException.class, () -> reservationService.createReservation(req, userAuth));
    }

    @Test
    @DisplayName("Get Reservation - USER can access own")
    void getReservation_UserOwn_Success() {
        when(reservationRepository.findById(10L)).thenReturn(Optional.of(reservation));
        ReservationResponse res = reservationService.getReservationById(10L, userAuth);
        assertNotNull(res);
        assertEquals(10L, res.getId());
    }

    @Test
    @DisplayName("Get Reservation - USER blocked from accessing others")
    void getReservation_UserOther_Forbidden() {
        User otherUser = User.builder().id(3L).username("other").role(Role.USER).build();
        UserPrincipal otherPrincipal = UserPrincipal.create(otherUser);
        Authentication otherAuth = new UsernamePasswordAuthenticationToken(otherPrincipal, null,
                otherPrincipal.getAuthorities());

        when(reservationRepository.findById(10L)).thenReturn(Optional.of(reservation));
        assertThrows(ForbiddenException.class, () -> reservationService.getReservationById(10L, otherAuth));
    }

    @Test
    @DisplayName("Get Reservation - ADMIN can access any")
    void getReservation_AdminAccessAny_Success() {
        when(reservationRepository.findById(10L)).thenReturn(Optional.of(reservation));
        ReservationResponse res = reservationService.getReservationById(10L, adminAuth);
        assertNotNull(res);
    }

    @Test
    @DisplayName("Update Reservation - USER can update own")
    void updateReservation_UserOwn_Success() {
        ReservationCreateRequest req = new ReservationCreateRequest();
        req.setResourceId(100L);
        req.setStartTime(LocalDateTime.now().plusDays(3));
        req.setEndTime(LocalDateTime.now().plusDays(4));
        req.setPrice(BigDecimal.valueOf(120));

        when(reservationRepository.findById(10L)).thenReturn(Optional.of(reservation));
        when(reservationRepository.save(any(Reservation.class))).thenReturn(reservation);

        ReservationResponse res = reservationService.updateReservation(10L, req, ReservationStatus.CANCELLED, userAuth);
        assertNotNull(res);
        assertEquals(ReservationStatus.CANCELLED, reservation.getStatus());
        assertEquals(BigDecimal.valueOf(120), reservation.getPrice());
    }

    @Test
    @DisplayName("Update Reservation - USER invalid status transition")
    void updateReservation_UserInvalidStatusTransition_Forbidden() {
        ReservationCreateRequest req = new ReservationCreateRequest();
        req.setResourceId(100L);
        req.setStartTime(LocalDateTime.now().plusDays(3));
        req.setEndTime(LocalDateTime.now().plusDays(4));
        req.setPrice(BigDecimal.valueOf(120));

        reservation.setStatus(ReservationStatus.CONFIRMED);
        when(reservationRepository.findById(10L)).thenReturn(Optional.of(reservation));

        assertThrows(ForbiddenException.class,
                () -> reservationService.updateReservation(10L, req, ReservationStatus.PENDING, userAuth));
    }

    @Test
    @DisplayName("Update Reservation - USER blocked from updating others")
    void updateReservation_UserOther_Forbidden() {
        User otherUser = User.builder().id(3L).username("other").role(Role.USER).build();
        UserPrincipal otherPrincipal = UserPrincipal.create(otherUser);
        Authentication otherAuth = new UsernamePasswordAuthenticationToken(otherPrincipal, null,
                otherPrincipal.getAuthorities());

        ReservationCreateRequest req = new ReservationCreateRequest();
        req.setResourceId(100L);

        when(reservationRepository.findById(10L)).thenReturn(Optional.of(reservation));
        assertThrows(ForbiddenException.class, () -> reservationService.updateReservation(10L, req, null, otherAuth));
    }

    @Test
    @DisplayName("Delete Reservation - USER can delete own")
    void deleteReservation_UserOwn_Success() {
        when(reservationRepository.findById(10L)).thenReturn(Optional.of(reservation));
        reservationService.deleteReservation(10L, userAuth);
        verify(reservationRepository, times(1)).delete(reservation);
    }

    @Test
    @DisplayName("Delete Reservation - USER blocked from deleting others")
    void deleteReservation_UserOther_Forbidden() {
        User otherUser = User.builder().id(3L).username("other").role(Role.USER).build();
        UserPrincipal otherPrincipal = UserPrincipal.create(otherUser);
        Authentication otherAuth = new UsernamePasswordAuthenticationToken(otherPrincipal, null,
                otherPrincipal.getAuthorities());

        when(reservationRepository.findById(10L)).thenReturn(Optional.of(reservation));
        assertThrows(ForbiddenException.class, () -> reservationService.deleteReservation(10L, otherAuth));
        verify(reservationRepository, never()).delete(any(Reservation.class));
    }

    @Test
    @DisplayName("Get Reservations - ADMIN sees all (specs test proxy)")
    void getReservations_Admin_GetsAll() {
        Page<Reservation> page = new PageImpl<>(List.of(reservation));
        when(reservationRepository.findAll(any(Specification.class), any(PageRequest.class))).thenReturn(page);

        Page<ReservationResponse> res = reservationService.getReservations(null, null, null, PageRequest.of(0, 10),
                adminAuth);
        assertEquals(1, res.getTotalElements());
    }
}
