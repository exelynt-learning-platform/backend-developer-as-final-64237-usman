package com.usman.resourcebooking.service;

import com.usman.resourcebooking.dto.request.LoginRequest;
import com.usman.resourcebooking.dto.request.RegisterRequest;
import com.usman.resourcebooking.dto.response.AuthResponse;

// Contract for authentication operations (registration and login).
public interface AuthService {

    String register(RegisterRequest request);

    AuthResponse login(LoginRequest request);
}
