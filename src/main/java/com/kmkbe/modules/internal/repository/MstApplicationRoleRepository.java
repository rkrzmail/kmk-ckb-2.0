package com.kmkbe.modules.internal.repository;

import com.kmkbe.modules.internal.entity.MstApplicationRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MstApplicationRoleRepository extends JpaRepository<MstApplicationRole, UUID> {
}
