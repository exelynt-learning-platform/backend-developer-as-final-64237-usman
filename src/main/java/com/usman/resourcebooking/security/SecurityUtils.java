package com.usman.resourcebooking.security;

import org.springframework.security.core.Authentication;
import com.usman.resourcebooking.exception.ForbiddenException;
import com.usman.resourcebooking.model.Reservation;

public class SecurityUtils {

    public static final String ROLE_ADMIN = "ROLE_ADMIN";

    private SecurityUtils() {
        // Private constructor to prevent instantiation
    }

    public static void checkOwnership(Reservation reservation, Authentication authentication) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();

        if (!isAdmin(authentication) && !reservation.getUser().getId().equals(principal.getId())) {
            throw new ForbiddenException("You do not have permission to access this reservation");
        }
    }

    public static boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(ROLE_ADMIN));
    }
}
