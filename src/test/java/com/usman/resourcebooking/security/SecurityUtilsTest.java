package com.usman.resourcebooking.security;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.usman.resourcebooking.exception.ForbiddenException;
import com.usman.resourcebooking.model.Reservation;
import com.usman.resourcebooking.model.User;

@DisplayName("SecurityUtils Unit Tests")
class SecurityUtilsTest {

    @Test
    @DisplayName("isAdmin returns true for ADMIN role")
    void isAdmin_ReturnsTrue_ForAdmin() {
        User user = User.builder().id(1L).username("admin").email("admin@test.com").password("pwd").role(com.usman.resourcebooking.model.Role.ADMIN).build();
        UserPrincipal principal = UserPrincipal.create(user);
        Authentication auth = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        assertTrue(SecurityUtils.isAdmin(auth));
    }

    @Test
    @DisplayName("isAdmin returns false for USER role")
    void isAdmin_ReturnsFalse_ForUser() {
        User user = User.builder().id(1L).username("user").email("user@test.com").password("pwd").role(com.usman.resourcebooking.model.Role.USER).build();
        UserPrincipal principal = UserPrincipal.create(user);
        Authentication auth = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        assertFalse(SecurityUtils.isAdmin(auth));
    }

    @Test
    @DisplayName("checkOwnership passes for owner")
    void checkOwnership_PassesForOwner() {
        User owner = User.builder().id(1L).username("user").email("user@test.com").password("pwd").role(com.usman.resourcebooking.model.Role.USER).build();
        Reservation reservation = Reservation.builder().user(owner).build();
        UserPrincipal principal = UserPrincipal.create(owner);
        Authentication auth = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        
        assertDoesNotThrow(() -> SecurityUtils.checkOwnership(reservation, auth));
    }

    @Test
    @DisplayName("checkOwnership passes for ADMIN even if not owner")
    void checkOwnership_PassesForAdmin() {
        User owner = User.builder().id(1L).build();
        Reservation reservation = Reservation.builder().user(owner).build();
        User admin = User.builder().id(2L).username("admin").email("admin@test.com").password("pwd").role(com.usman.resourcebooking.model.Role.ADMIN).build();
        UserPrincipal principal = UserPrincipal.create(admin);
        Authentication auth = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        
        assertDoesNotThrow(() -> SecurityUtils.checkOwnership(reservation, auth));
    }

    @Test
    @DisplayName("checkOwnership throws ForbiddenException for non-owner USER")
    void checkOwnership_ThrowsForbiddenException_ForNonOwner() {
        User owner = User.builder().id(1L).build();
        Reservation reservation = Reservation.builder().user(owner).build();
        User user2 = User.builder().id(2L).username("user2").email("user2@test.com").password("pwd").role(com.usman.resourcebooking.model.Role.USER).build();
        UserPrincipal principal = UserPrincipal.create(user2);
        Authentication auth = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        
        assertThrows(ForbiddenException.class, () -> SecurityUtils.checkOwnership(reservation, auth));
    }
}
