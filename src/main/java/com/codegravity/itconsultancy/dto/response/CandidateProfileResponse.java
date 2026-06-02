package com.codegravity.itconsultancy.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class CandidateProfileResponse {

    private String candidateId;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String address;
    private String appliedRole;
    private String resumeUrl;
    private LocalDateTime updatedAt;
}
