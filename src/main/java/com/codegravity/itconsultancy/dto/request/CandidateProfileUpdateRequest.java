package com.codegravity.itconsultancy.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CandidateProfileUpdateRequest {

    @NotBlank(message = "Phone is required")
    @Pattern(regexp = "^[+]?[0-9]{7,15}$", message = "Invalid phone number")
    private String phone;

    @Size(max = 255, message = "Address must not exceed 255 characters")
    private String address;

    @Size(max = 500, message = "Resume URL must not exceed 500 characters")
    private String resumeUrl;

    @NotNull(message = "Highest education is required")
    @Size(max = 150, message = "Highest education must not exceed 150 characters")
    private String highestEducation;

    @Size(max = 150, message = "Field of study must not exceed 150 characters")
    private String fieldOfStudy;

    @NotNull(message = "Work authorization is required")
    @Size(max = 100, message = "Work authorization must not exceed 100 characters")
    private String workAuthorization;

    @NotNull(message = "Tools and technologies are required")
    @Size(max = 1000, message = "Tools and technologies must not exceed 1000 characters")
    private String toolsTechnologies;

    @Size(max = 255, message = "Accommodation needed must not exceed 255 characters")
    private String accommodationNeeded;
}
