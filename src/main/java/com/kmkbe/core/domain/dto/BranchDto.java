package com.kmkbe.core.domain.dto;

import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;

@Getter
@Builder
public class BranchDto implements Serializable {
    private final String branchCode;
    private final String branchName;
}
