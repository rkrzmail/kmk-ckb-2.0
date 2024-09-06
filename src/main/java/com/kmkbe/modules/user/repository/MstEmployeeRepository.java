package com.kmkbe.modules.user.repository;

import com.kmkbe.modules.user.entity.MstEmployee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MstEmployeeRepository extends JpaRepository<MstEmployee, String> {
    Optional<MstEmployee> findByEmail(String email);

    Optional<MstEmployee> findByEmployeeCode(String employeeCode);
}
