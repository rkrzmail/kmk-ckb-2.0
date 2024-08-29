package com.kmkbe.modules.major_account.service;

import com.kmkbe.core.domain.dto.BranchDto;
import com.kmkbe.modules.user.entity.MstBranch;
import com.kmkbe.modules.user.repository.MstBranchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class MstBranchService {
    private final MstBranchRepository mstBranchRepository;

    public List<BranchDto> branchList(String branchName) {
        try {
            List<MstBranch> mstBranches = mstBranchRepository.findAllActive();
            List<BranchDto> result = new ArrayList<>();

            if (mstBranches != null && !mstBranches.isEmpty()) {
                result = mstBranches.stream()
                        .map((e) -> BranchDto.builder()
                                .branchCode(e.getBranchCode())
                                .branchName(e.getBranchName())
                                .build())
                        .toList();
            }

            return result;
        } catch (Exception e) {
            log.error("branchList: error {}", e.getMessage());
            throw e;
        }
    }
}
