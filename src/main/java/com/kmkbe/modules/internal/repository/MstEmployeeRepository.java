package com.kmkbe.modules.internal.repository;

import com.kmkbe.modules.internal.entity.MstEmployee;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MstEmployeeRepository extends JpaRepository<MstEmployee, String> {
}
