package com.kmkbe.modules.internal.repository;

import com.kmkbe.modules.internal.entity.MstAppRoleFormUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MstAppRoleFormUserRepository extends JpaRepository<MstAppRoleFormUser, UUID> {
}
