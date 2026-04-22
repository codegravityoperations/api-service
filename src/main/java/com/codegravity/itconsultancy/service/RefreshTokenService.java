package com.codegravity.itconsultancy.service;

import com.codegravity.itconsultancy.entity.RefreshToken;
import com.codegravity.itconsultancy.enums.UserType;

public interface RefreshTokenService {
    RefreshToken createRefreshToken(String email, UserType userType);
    RefreshToken validateRefreshToken(String token);
    void revokeUserTokens(String email, UserType userType);
}