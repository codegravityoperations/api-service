package com.codegravity.itconsultancy.dto.response;

import com.codegravity.itconsultancy.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CandidateProfileResponse {

    private String candidateId;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String address;
    private String appliedRole;
    private String resumeUrl;
    private String notes;

    private String highestEducation;
    private String fieldOfStudy;
    private String workAuthorization;
    private String toolsTechnologies;
    private String accommodationNeeded;

    private UserStatus status;
    private boolean active;
    private LocalDateTime updatedAt;
}
