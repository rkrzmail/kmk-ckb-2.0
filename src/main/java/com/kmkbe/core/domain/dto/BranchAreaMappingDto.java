package com.kmkbe.core.domain.dto;

import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;

/**
 * DTO for {@link com.kmkbe.core.domain.entity.BranchAreaMapping}
 */
@Builder
@Getter
public class BranchAreaMappingDto implements Serializable {
    private final Long branchAreaMappingId;
    private final String area;
    private final String province;
    private final String city;
    private final String branch;
}
