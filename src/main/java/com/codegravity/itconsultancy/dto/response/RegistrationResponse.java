package com.codegravity.itconsultancy.dto.response;

import com.codegravity.itconsultancy.enums.EmailStatus;
import com.codegravity.itconsultancy.enums.UserType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class RegistrationResponse {

    private String message;
    private UserType userType;
    private String generatedId;
    private EmailStatus emailStatus;
}