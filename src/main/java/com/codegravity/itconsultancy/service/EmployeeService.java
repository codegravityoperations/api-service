package com.codegravity.itconsultancy.service;

import com.codegravity.itconsultancy.dto.request.EmployeeRegisterRequest;
import com.codegravity.itconsultancy.dto.response.RegistrationResponse;

public interface EmployeeService {
    RegistrationResponse register(EmployeeRegisterRequest request);
}