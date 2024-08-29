package com.kmkbe.modules.user.repository;

import com.kmkbe.modules.user.entity.MstBranch;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MstBranchRepository extends JpaRepository<MstBranch, String> {
    Optional<MstBranch> findByBranchCode(String code);

    List<MstBranch> findAllByIsActive(boolean isActive);

    default List<MstBranch> findAllActive() {
        return findAllByIsActive(true);
    }
}
