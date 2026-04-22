package com.codegravity.itconsultancy.controller;

import com.codegravity.itconsultancy.constants.ApiConstants;
import com.codegravity.itconsultancy.dto.request.EmployeeRegisterRequest;
import com.codegravity.itconsultancy.dto.response.ApiResponse;
import com.codegravity.itconsultancy.dto.response.RegistrationResponse;
import com.codegravity.itconsultancy.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<RegistrationResponse>> register(
            @Valid @RequestBody EmployeeRegisterRequest request) {

        RegistrationResponse response = employeeService.register(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Employee registered successfully", response));
    }
}