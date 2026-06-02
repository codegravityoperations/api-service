package com.codegravity.itconsultancy.service;

import com.codegravity.itconsultancy.dto.request.CandidateRegisterRequest;
import com.codegravity.itconsultancy.dto.request.CandidateProfileUpdateRequest;
import com.codegravity.itconsultancy.dto.response.CandidateProfileResponse;
import com.codegravity.itconsultancy.dto.response.RegistrationResponse;

public interface CandidateService {
    RegistrationResponse register(CandidateRegisterRequest request);

    CandidateProfileResponse updateProfile(String candidateId, CandidateProfileUpdateRequest request);
}
