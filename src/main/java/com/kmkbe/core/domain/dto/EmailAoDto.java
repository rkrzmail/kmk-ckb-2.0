package com.kmkbe.core.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmailAoDto {
    private String employeeCode;
    private String branchCode;
    private String type;
}
