package com.codegravity.itconsultancy.controller;

import com.codegravity.itconsultancy.dto.request.CandidateProfileUpdateRequest;
import com.codegravity.itconsultancy.dto.request.CandidateRegisterRequest;
import com.codegravity.itconsultancy.dto.response.ApiResponse;
import com.codegravity.itconsultancy.dto.response.CandidateListResponse;
import com.codegravity.itconsultancy.dto.response.CandidateProfileResponse;
import com.codegravity.itconsultancy.dto.response.RegistrationResponse;
import com.codegravity.itconsultancy.enums.UserStatus;
import com.codegravity.itconsultancy.service.CandidateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Candidates", description = "Candidate registration and profile management")
@RestController
@RequestMapping("/api/candidates")
@RequiredArgsConstructor
public class CandidateController {

    private final CandidateService candidateService;

    @Operation(summary = "Register a new candidate")
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<RegistrationResponse>> register(
            @Valid @RequestBody CandidateRegisterRequest request) {

        RegistrationResponse response = candidateService.register(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Candidate registered successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<CandidateListResponse>>> searchCandidates(
            @RequestParam(required = false) String candidateId,
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String phoneNumber,
            @RequestParam(required = false) UserStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<CandidateListResponse> result = candidateService.searchCandidates(
                candidateId, firstName, lastName, email, phoneNumber, status, page, size);
        return ResponseEntity.ok(ApiResponse.success("Candidates retrieved successfully", result));
    }

    @Operation(
            summary = "Delete a candidate (soft delete)",
            description = "Marks the candidate as DELETED. Only EMPLOYEE and ADMIN roles may call this endpoint.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Candidate deleted"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Candidate not found")
    })
    @DeleteMapping("/{candidateId}")
    public ResponseEntity<ApiResponse<Void>> deleteCandidate(
            @Parameter(description = "Candidate business ID (e.g. CND_2026_00001)")
            @PathVariable String candidateId,
            Authentication authentication) {

        candidateService.deleteCandidate(candidateId, authentication);
        return ResponseEntity.ok(ApiResponse.success("Candidate deleted successfully", null));
    }

    @Operation(
            summary = "Update candidate profile",
            description = "Updates education, work authorisation, tools, and accommodation fields. " +
                    "A CANDIDATE may only update their own profile; an ADMIN may update any profile.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Profile updated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Candidate not found")
    })
    @PutMapping("/{id}/profile")
    public ResponseEntity<ApiResponse<CandidateProfileResponse>> updateProfile(
            @Parameter(description = "Candidate business ID (e.g. CND_2026_00001)")
            @PathVariable String id,
            @Valid @RequestBody CandidateProfileUpdateRequest request,
            Authentication authentication) {

        CandidateProfileResponse response = candidateService.updateProfile(id, request, authentication);
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", response));
    }
}
