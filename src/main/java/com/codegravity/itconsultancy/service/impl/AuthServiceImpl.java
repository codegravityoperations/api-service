package com.codegravity.itconsultancy.service.impl;

import com.codegravity.itconsultancy.constants.SecurityConstants;
import com.codegravity.itconsultancy.dto.request.LoginRequest;
import com.codegravity.itconsultancy.dto.request.RefreshTokenRequest;
import com.codegravity.itconsultancy.dto.response.AuthResponse;
import com.codegravity.itconsultancy.entity.Candidate;
import com.codegravity.itconsultancy.entity.Employee;
import com.codegravity.itconsultancy.entity.RefreshToken;
import com.codegravity.itconsultancy.enums.Role;
import com.codegravity.itconsultancy.enums.UserType;
import com.codegravity.itconsultancy.exception.ResourceNotFoundException;
import com.codegravity.itconsultancy.repository.CandidateRepository;
import com.codegravity.itconsultancy.repository.EmployeeRepository;
import com.codegravity.itconsultancy.security.JwtTokenProvider;
import com.codegravity.itconsultancy.service.AuthService;
import com.codegravity.itconsultancy.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final EmployeeRepository employeeRepository;
    private final CandidateRepository candidateRepository;

    @Override
    public AuthResponse login(LoginRequest request) {
        // Authenticate via Spring Security
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail() + "::" + request.getUserType().name(),
                        request.getPassword()
                )
        );

        return buildAuthResponse(request.getEmail(), request.getUserType());
    }

    @Override
    public AuthResponse refresh(RefreshTokenRequest request) {
        RefreshToken refreshToken = refreshTokenService.validateRefreshToken(request.getRefreshToken());
        return buildAuthResponse(refreshToken.getUserEmail(), refreshToken.getUserType());
    }

    @Override
    public void logout(RefreshTokenRequest request) {
        RefreshToken refreshToken = refreshTokenService.validateRefreshToken(request.getRefreshToken());
        refreshTokenService.revokeUserTokens(refreshToken.getUserEmail(), refreshToken.getUserType());
        log.info("User logged out: {}", refreshToken.getUserEmail());
    }

    // ─── Internal ─────────────────────────────────────────────────
    private AuthResponse buildAuthResponse(String email, UserType userType) {
        Role role;
        String businessId;

        if (userType == UserType.EMPLOYEE || userType == UserType.ADMIN) {
            Employee employee = employeeRepository.findByEmail(email)
                    .orElseThrow(() -> new ResourceNotFoundException("Employee not found: " + email));
            role = employee.getRoles().stream()
                    .map(r -> r.getName())
                    .findFirst()
                    .orElse(Role.ROLE_EMPLOYEE);
            businessId = employee.getEmployeeId();
        } else {
            Candidate candidate = candidateRepository.findByEmail(email)
                    .orElseThrow(() -> new ResourceNotFoundException("Candidate not found: " + email));
            role = candidate.getRoles().stream()
                    .map(r -> r.getName())
                    .findFirst()
                    .orElse(Role.ROLE_CANDIDATE);
            businessId = candidate.getCandidateId();
        }

        String accessToken = jwtTokenProvider.generateAccessToken(email, role, userType, businessId);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(email, userType);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .tokenType(SecurityConstants.TOKEN_PREFIX.trim())
                .email(email)
                .businessId(businessId)
                .userType(userType)
                .role(role.getSimpleName())
                .build();
    }
}