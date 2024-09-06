package com.kmkbe.core.domain.repository;

import com.kmkbe.core.domain.entity.BranchAreaMapping;
import com.kmkbe.modules.user.entity.MstBranch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface BranchAreaMappingRepository extends JpaRepository<BranchAreaMapping, String>, JpaSpecificationExecutor<BranchAreaMapping> {
    List<BranchAreaMapping> findAllByMstBranch(MstBranch branch);
}
