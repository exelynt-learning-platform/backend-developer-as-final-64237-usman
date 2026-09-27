package com.usman.resourcebooking.dto.response;

import lombok.Getter;

@Getter
public class AuthResponse {

    private final String accessToken;
    private final String tokenType = "Bearer";
    private final String role;

    public AuthResponse(String accessToken, String role) {
        this.accessToken = accessToken;
        this.role = role;
    }
}
