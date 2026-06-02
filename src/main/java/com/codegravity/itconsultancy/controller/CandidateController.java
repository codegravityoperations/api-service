package com.codegravity.itconsultancy.controller;

import com.codegravity.itconsultancy.constants.ApiConstants;
import com.codegravity.itconsultancy.dto.request.CandidateProfileUpdateRequest;
import com.codegravity.itconsultancy.dto.request.CandidateRegisterRequest;
import com.codegravity.itconsultancy.dto.response.ApiResponse;
import com.codegravity.itconsultancy.dto.response.CandidateProfileResponse;
import com.codegravity.itconsultancy.dto.response.RegistrationResponse;
import com.codegravity.itconsultancy.service.CandidateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/candidates")
@RequiredArgsConstructor
public class CandidateController {

    private final CandidateService candidateService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<RegistrationResponse>> register(
            @Valid @RequestBody CandidateRegisterRequest request) {

        RegistrationResponse response = candidateService.register(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Candidate registered successfully", response));
    }

    @PutMapping("/{candidateId}/profile")
    public ResponseEntity<ApiResponse<CandidateProfileResponse>> updateProfile(
            @PathVariable String candidateId,
            @Valid @RequestBody CandidateProfileUpdateRequest request) {

        CandidateProfileResponse response = candidateService.updateProfile(candidateId, request);
        return ResponseEntity.ok(ApiResponse.success("Candidate profile updated successfully", response));
    }
}
