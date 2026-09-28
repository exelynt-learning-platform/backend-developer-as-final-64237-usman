package com.usman.resourcebooking.security;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import io.jsonwebtoken.Claims;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtAuthenticationFilter Unit Tests")
class JwtAuthenticationFilterTest {

    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private CustomUserDetailsService customUserDetailsService;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("doFilterInternal sets authentication for valid token")
    void doFilterInternal_ValidToken_SetsAuthentication() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);

        when(request.getHeader("Authorization")).thenReturn("Bearer valid_token");
        Claims claims = mock(Claims.class);
        when(tokenProvider.parseAndValidateClaims("valid_token")).thenReturn(claims);
        when(tokenProvider.getUserIdFromClaims(claims)).thenReturn(1L);

        UserDetails userDetails = mock(UserDetails.class);
        when(customUserDetailsService.loadUserById(1L)).thenReturn(userDetails);
        
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assert (SecurityContextHolder.getContext().getAuthentication() != null);
    }

    @Test
    @DisplayName("doFilterInternal ignores if userId is null")
    void doFilterInternal_NullUserId_IgnoresToken() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);

        when(request.getHeader("Authorization")).thenReturn("Bearer valid_token");
        Claims claims = mock(Claims.class);
        when(tokenProvider.parseAndValidateClaims("valid_token")).thenReturn(claims);
        when(tokenProvider.getUserIdFromClaims(claims)).thenReturn(null); // Null userId
        
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(customUserDetailsService, never()).loadUserById(any());
        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    @DisplayName("doFilterInternal clears context on exception")
    void doFilterInternal_Exception_ClearsContext() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);

        when(request.getHeader("Authorization")).thenReturn("Bearer valid_token");
        when(tokenProvider.parseAndValidateClaims("valid_token")).thenThrow(new RuntimeException("Test exception"));
        
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
}
