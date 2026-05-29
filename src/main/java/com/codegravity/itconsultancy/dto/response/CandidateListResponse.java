package com.codegravity.itconsultancy.dto.response;

import com.codegravity.itconsultancy.enums.UserStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class CandidateListResponse {
    private String candidateId;
    private String fullName;
    private String email;
    private String phoneNumber;
    private UserStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
