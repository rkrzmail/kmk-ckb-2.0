package com.kmkbe.modules.user.repository;

import com.kmkbe.modules.user.entity.MstEmployee;
import com.kmkbe.modules.user.entity.MstUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface MstUserRepository extends JpaRepository<MstUser, UUID> {
    Optional<MstUser> findByUserCode(UUID userCode);

    Optional<MstUser> findByUsername(String username);

    Optional<MstUser> findByEmployee(MstEmployee employee);

    @Query("select b.branchCode from MstUser u join u.employee e join e.branch b where u.userCode = :userCode")
    Optional<String> findBranchCodeByUserCode(@Param("userCode") UUID userCode);
}
