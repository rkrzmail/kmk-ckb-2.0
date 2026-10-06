package com.kmkbe.modules.master.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record FileTypeRequest(
    @NotBlank @Size(max = 20) String fileTypeCode,
    @NotBlank @Size(max = 100) String fileTypeName,
    @Size(max = 500) String fileTypeDesc,
    @NotBlank @Size(max = 50) String fileAllocation,
    @NotNull Boolean isMandatory,
    @NotNull @Min(1) Long maxSizeMb,
    UUID bouwheerCode
) {}
