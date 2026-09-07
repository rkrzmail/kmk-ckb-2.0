package com.kmkbe.modules.user.repository;

import com.kmkbe.modules.user.entity.MstEmployee;
import feign.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface MstEmployeeRepository extends JpaRepository<MstEmployee, String> {
    Optional<MstEmployee> findByEmail(String email);

  @Query(value = "SELECT me.* FROM users.mst_role mr " +
    "JOIN users.mst_application_role mar ON mr.role_code = mar.role_code " +
    "JOIN users.mst_app_role_form marf ON marf.application_role_code = mar.application_role_code " +
    "JOIN users.mst_app_role_form_user marfu ON marf.app_role_form_code = marfu.app_role_form_code " +
    "JOIN users.mst_user mu ON marfu.user_code = mu.user_code " +
    "JOIN users.mst_employee me ON mu.employee_code = me.employee_code " +
    "WHERE mr.role_code = :roleCode",
    nativeQuery = true)
  List<MstEmployee> findListEmployeesByRoleCode(@Param("roleCode") String roleCode);

}
