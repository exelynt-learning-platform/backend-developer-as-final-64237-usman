package com.usman.resourcebooking.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.usman.resourcebooking.dto.request.LoginRequest;
import com.usman.resourcebooking.dto.request.RegisterRequest;
import com.usman.resourcebooking.dto.response.AuthResponse;
import com.usman.resourcebooking.exception.ConflictException;
import com.usman.resourcebooking.model.Role;
import com.usman.resourcebooking.model.User;
import com.usman.resourcebooking.repository.UserRepository;
import com.usman.resourcebooking.security.JwtTokenProvider;
import com.usman.resourcebooking.security.UserPrincipal;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthServiceImpl Unit Tests")
class AuthServiceImplTest {

    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    @DisplayName("Register - Success")
    void register_Success() {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("newuser");
        req.setEmail("new@example.com");
        req.setPassword("password");

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("encoded_password");

        String response = authService.register(req);

        assertEquals("Registration successful. You can now log in.", response);
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Register - Username Taken")
    void register_UsernameTaken() {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("takenuser");

        when(userRepository.existsByUsername("takenuser")).thenReturn(true);

        assertThrows(ConflictException.class, () -> authService.register(req));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Register - Email Taken")
    void register_EmailTaken() {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("newuser");
        req.setEmail("taken@example.com");

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("taken@example.com")).thenReturn(true);

        assertThrows(ConflictException.class, () -> authService.register(req));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Login - Success")
    void login_Success() {
        LoginRequest req = new LoginRequest();
        req.setUsername("user");
        req.setPassword("pass");

        User user = User.builder().id(1L).username("user").role(Role.USER).build();
        UserPrincipal principal = UserPrincipal.create(user);
        Authentication auth = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);
        when(jwtTokenProvider.generateToken(auth)).thenReturn("mock.jwt.token");

        AuthResponse res = authService.login(req);

        assertEquals("mock.jwt.token", res.getAccessToken());
        assertEquals("ROLE_USER", res.getRole());
    }
}
