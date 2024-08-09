package com.kmkbe.modules.internal.repository;

import com.kmkbe.modules.internal.entity.MstAppRoleForm;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MstAppRoleFormRepository extends JpaRepository<MstAppRoleForm, UUID> {
}
