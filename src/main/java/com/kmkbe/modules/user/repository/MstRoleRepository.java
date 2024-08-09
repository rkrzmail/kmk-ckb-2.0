package com.kmkbe.modules.user.repository;

import com.kmkbe.modules.user.entity.MstRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MstRoleRepository extends JpaRepository<MstRole, String> {
    Optional<MstRole> findByRoleCode(String code);

    List<MstRole> findAllByIsActive(Boolean isActive);

    default List<MstRole> findAllByIsActive() {
        return findAllByIsActive(true);
    }
}
