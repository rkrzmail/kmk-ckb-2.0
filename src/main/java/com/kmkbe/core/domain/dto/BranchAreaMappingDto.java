package com.kmkbe.core.domain.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
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
