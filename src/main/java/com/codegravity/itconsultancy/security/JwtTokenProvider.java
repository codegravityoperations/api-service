package com.codegravity.itconsultancy.security;

import com.codegravity.itconsultancy.constants.SecurityConstants;
import com.codegravity.itconsultancy.enums.Role;
import com.codegravity.itconsultancy.enums.UserType;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Slf4j
@Component
public class JwtTokenProvider {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.access-token-expiry}")
    private long accessTokenExpiry;

    private SecretKey secretKey;

    @PostConstruct
    public void init() {
        this.secretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    // ─── Generate Access Token ────────────────────────────────────
    public String generateAccessToken(String email, Role role, UserType userType, String businessId) {
        return Jwts.builder()
                .subject(email)
                .claim(SecurityConstants.CLAIM_ROLE, role.name())
                .claim(SecurityConstants.CLAIM_USER_TYPE, userType.name())
                .claim(SecurityConstants.CLAIM_BUSINESS_ID, businessId)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + accessTokenExpiry))
                .signWith(secretKey)
                .compact();
    }

    // ─── Extract Claims ───────────────────────────────────────────
    public String getEmailFromToken(String token) {
        return parseClaims(token).getSubject();
    }

    public UserType getUserTypeFromToken(String token) {
        String userType = parseClaims(token).get(SecurityConstants.CLAIM_USER_TYPE, String.class);
        return UserType.valueOf(userType);
    }

    public Role getRoleFromToken(String token) {
        String role = parseClaims(token).get(SecurityConstants.CLAIM_ROLE, String.class);
        return Role.valueOf(role);
    }

    // ─── Validate ─────────────────────────────────────────────────
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("JWT expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.warn("JWT unsupported: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.warn("JWT malformed: {}", e.getMessage());
        } catch (SecurityException e) {
            log.warn("JWT signature invalid: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("JWT claims empty: {}", e.getMessage());
        }
        return false;
    }

    // ─── Internal ─────────────────────────────────────────────────
    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}