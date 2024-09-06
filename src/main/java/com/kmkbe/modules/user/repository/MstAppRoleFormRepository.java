package com.kmkbe.modules.user.repository;

import com.kmkbe.modules.user.entity.MstAppRoleForm;
import com.kmkbe.modules.user.entity.MstAppRoleFormUser;
import com.kmkbe.modules.user.entity.MstApplicationRole;
import com.kmkbe.modules.user.entity.MstForm;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MstAppRoleFormRepository extends JpaRepository<MstAppRoleForm, UUID> {
    List<MstAppRoleForm> findAllByApplicationRoleAndForm(
            MstApplicationRole applicationRole,
            MstForm form
    );
}
