package com.kmkbe.modules.user.repository;

import com.kmkbe.modules.user.entity.MstEmployee;
import com.kmkbe.modules.user.entity.MstUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface MstUserRepository extends JpaRepository<MstUser, UUID> {
    Optional<MstUser> findByUserCode(UUID userCode);

    Optional<MstUser> findByUsername(String username);

    Optional<MstUser> findByEmployee(MstEmployee employee);
}
