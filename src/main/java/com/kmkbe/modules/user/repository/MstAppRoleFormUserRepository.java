package com.kmkbe.modules.user.repository;

import com.kmkbe.modules.user.entity.MstAppRoleFormUser;
import com.kmkbe.modules.user.entity.MstUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MstAppRoleFormUserRepository extends JpaRepository<MstAppRoleFormUser, UUID> {
    List<MstAppRoleFormUser> findAllByUser(MstUser mstUser);

    Optional<MstAppRoleFormUser> findTopByUserOrderByAppRoleFormUserId(MstUser mstUser);

    @Query(value = """
        SELECT DISTINCT employee.email
        FROM users.mst_app_role_form_user permission
        JOIN users.mst_user app_user ON app_user.user_code = permission.user_code
        JOIN users.mst_employee employee ON employee.employee_code = app_user.employee_code
        JOIN users.mst_app_role_form role_form
          ON role_form.app_role_form_code = permission.app_role_form_code
        JOIN users.mst_application_role app_role
          ON app_role.application_role_code = role_form.application_role_code
        WHERE employee.branch_code = :branchCode
          AND app_role.role_code IN ('brnch_admin', 'brnch_adm')
          AND permission.is_active = true
          AND app_user.is_active = true
          AND employee.is_active = true
          AND employee.email IS NOT NULL
          AND TRIM(employee.email) <> ''
        ORDER BY employee.email
        """, nativeQuery = true)
    List<String> findActiveBranchAdminEmails(@Param("branchCode") String branchCode);
}
