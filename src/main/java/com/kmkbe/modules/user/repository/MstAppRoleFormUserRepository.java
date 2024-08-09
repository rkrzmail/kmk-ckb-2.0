package com.kmkbe.modules.user.repository;

import com.kmkbe.modules.user.entity.MstAppRoleFormUser;
import com.kmkbe.modules.user.entity.MstUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface MstAppRoleFormUserRepository extends JpaRepository<MstAppRoleFormUser, UUID> {
    Optional<MstAppRoleFormUser> findByUserCode(MstUser mstUser);

    Optional<MstAppRoleFormUser> findTopByUserCodeOrderByAppRoleFormUserId(MstUser mstUser);
}
