package com.codegravity.itconsultancy.dto.response;

import com.codegravity.itconsultancy.enums.UserType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class AuthResponse {

    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private String email;
    private String businessId;
    private UserType userType;
    private String role;
}