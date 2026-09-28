package com.usman.resourcebooking.security;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@DisplayName("JwtTokenProvider Unit Tests")
class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret", "bXlTdXBlclNlY3VyZUpXVFNlY3JldEtleUZvclJlc291cmNlQm9va2luZ1N5c3RlbQ==");
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpirationMs", 86400000L);
    }

    @Test
    @DisplayName("validateSecret throws exception for null or blank secret")
    void validateSecret_ThrowsException_ForNullOrBlank() {
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret", null);
        assertThrows(IllegalArgumentException.class, () -> jwtTokenProvider.validateSecret());
        
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret", "   ");
        assertThrows(IllegalArgumentException.class, () -> jwtTokenProvider.validateSecret());
    }

    @Test
    @DisplayName("validateSecret throws exception for short secret")
    void validateSecret_ThrowsException_ForShortSecret() {
        // Base64 encoding of a string shorter than 32 bytes
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret", "c2hvcnRTZWNyZXQ=");
        assertThrows(IllegalArgumentException.class, () -> jwtTokenProvider.validateSecret());
    }

    @Test
    @DisplayName("validateSecret passes for valid secret")
    void validateSecret_PassesForValidSecret() {
        assertDoesNotThrow(() -> jwtTokenProvider.validateSecret());
    }

    @Test
    @DisplayName("generateToken and parseAndValidateClaims work correctly")
    void generateToken_AndParse_WorkCorrectly() {
        com.usman.resourcebooking.model.User user = com.usman.resourcebooking.model.User.builder().id(1L).username("testuser").email("user@test.com").password("pwd").role(com.usman.resourcebooking.model.Role.USER).build();
        UserPrincipal principal = UserPrincipal.create(user);
        Authentication auth = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        
        String token = jwtTokenProvider.generateToken(auth);
        assertNotNull(token);

        Claims claims = jwtTokenProvider.parseAndValidateClaims(token);
        assertNotNull(claims);
        assertEquals("testuser", claims.getSubject());
        
        Long userId = jwtTokenProvider.getUserIdFromClaims(claims);
        assertEquals(1L, userId);
    }

    @Test
    @DisplayName("parseAndValidateClaims returns null for invalid signature")
    void parseAndValidateClaims_ReturnsNull_ForInvalidSignature() {
        com.usman.resourcebooking.model.User user = com.usman.resourcebooking.model.User.builder().id(1L).username("testuser").email("user@test.com").password("pwd").role(com.usman.resourcebooking.model.Role.USER).build();
        UserPrincipal principal = UserPrincipal.create(user);
        Authentication auth = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        
        String token = jwtTokenProvider.generateToken(auth);
        
        // Tamper with the token
        String tamperedToken = token.substring(0, token.length() - 5) + "aaaaa";
        
        Claims claims = jwtTokenProvider.parseAndValidateClaims(tamperedToken);
        assertNull(claims);
    }
}
