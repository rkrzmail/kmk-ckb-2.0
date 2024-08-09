package com.kmkbe.modules.user.repository;

import com.kmkbe.modules.user.entity.MstAppRoleForm;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MstAppRoleFormRepository extends JpaRepository<MstAppRoleForm, UUID> {
}
