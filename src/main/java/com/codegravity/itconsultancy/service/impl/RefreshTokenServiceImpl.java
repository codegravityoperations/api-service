package com.codegravity.itconsultancy.service.impl;

import com.codegravity.itconsultancy.entity.RefreshToken;
import com.codegravity.itconsultancy.enums.UserType;
import com.codegravity.itconsultancy.exception.TokenException;
import com.codegravity.itconsultancy.repository.RefreshTokenRepository;
import com.codegravity.itconsultancy.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${app.jwt.refresh-token-expiry}")
    private long refreshTokenExpiry;

    @Override
    @Transactional
    public RefreshToken createRefreshToken(String email, UserType userType) {
        // Revoke old tokens for this user first
        revokeUserTokens(email, userType);

        RefreshToken refreshToken = RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .userEmail(email)
                .userType(userType)
                .expiryDate(LocalDateTime.now().plusSeconds(refreshTokenExpiry / 1000))
                .revoked(false)
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    @Override
    public RefreshToken validateRefreshToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new TokenException("Refresh token not found"));

        if (refreshToken.isRevoked()) {
            throw new TokenException("Refresh token has been revoked");
        }

        if (refreshToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new TokenException("Refresh token has expired");
        }

        return refreshToken;
    }

    @Override
    @Transactional
    public void revokeUserTokens(String email, UserType userType) {
        refreshTokenRepository.revokeAllByEmailAndUserType(email, userType);
        log.debug("Revoked all refresh tokens for {} | type: {}", email, userType);
    }
}