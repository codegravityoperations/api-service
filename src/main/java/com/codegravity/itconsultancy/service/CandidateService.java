package com.codegravity.itconsultancy.service;

import com.codegravity.itconsultancy.dto.request.CandidateProfileUpdateRequest;
import com.codegravity.itconsultancy.dto.request.CandidateRegisterRequest;
import com.codegravity.itconsultancy.dto.response.CandidateListResponse;
import com.codegravity.itconsultancy.dto.response.CandidateProfileResponse;
import com.codegravity.itconsultancy.dto.response.RegistrationResponse;
import com.codegravity.itconsultancy.enums.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;

public interface CandidateService {
    RegistrationResponse register(CandidateRegisterRequest request);

    Page<CandidateListResponse> searchCandidates(
            String candidateId,
            String firstName,
            String lastName,
            String email,
            String phoneNumber,
            UserStatus status,
            int page,
            int size
    );

    CandidateProfileResponse updateProfile(String candidateId,
                                           CandidateProfileUpdateRequest request,
                                           Authentication authentication);

    void deleteCandidate(String candidateId, Authentication authentication);
}
