package com.kmkbe.modules.user.repository;

import com.kmkbe.modules.user.entity.MstApplicationRole;
import com.kmkbe.modules.user.entity.MstRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface MstApplicationRoleRepository extends JpaRepository<MstApplicationRole, UUID> {
    Integer countByRoleCode(MstRole role);

    Optional<MstApplicationRole> findByRoleCode(MstRole role);
}
