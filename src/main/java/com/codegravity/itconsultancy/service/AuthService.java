package com.codegravity.itconsultancy.service;

import com.codegravity.itconsultancy.dto.request.LoginRequest;
import com.codegravity.itconsultancy.dto.request.RefreshTokenRequest;
import com.codegravity.itconsultancy.dto.response.AuthResponse;

public interface AuthService {
    AuthResponse login(LoginRequest request);
    AuthResponse refresh(RefreshTokenRequest request);
    void logout(RefreshTokenRequest request);
}