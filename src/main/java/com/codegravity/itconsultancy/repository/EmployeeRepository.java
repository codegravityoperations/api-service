package com.codegravity.itconsultancy.repository;

import com.codegravity.itconsultancy.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    Optional<Employee> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<Employee> findByEmployeeId(String employeeId);

    long countByEmployeeIdStartingWith(String prefix);
}