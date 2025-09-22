package com.tinaco.monitoragua.auth.dto;

public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private String message;

    public AuthResponse(String message) {
        this.message = message;
    }

    public AuthResponse(String accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public String getMessage() {
        return message;
    }
}