package com.usman.resourcebooking.service.impl;

import com.usman.resourcebooking.dto.request.LoginRequest;
import com.usman.resourcebooking.dto.request.RegisterRequest;
import com.usman.resourcebooking.dto.response.AuthResponse;
import com.usman.resourcebooking.exception.ConflictException;
import com.usman.resourcebooking.model.Role;
import com.usman.resourcebooking.model.User;
import com.usman.resourcebooking.repository.UserRepository;
import com.usman.resourcebooking.security.JwtTokenProvider;
import com.usman.resourcebooking.security.UserPrincipal;
import com.usman.resourcebooking.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    @Transactional
    public String register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ConflictException(
                    "Username '" + request.getUsername() + "' is already taken");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException(
                    "Email '" + request.getEmail() + "' is already registered");
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .build();

        userRepository.save(user);
        return "Registration successful. You can now log in.";
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = jwtTokenProvider.generateToken(authentication);
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        String role = principal.getAuthorities().iterator().next().getAuthority();

        return new AuthResponse(jwt, role);
    }
}
