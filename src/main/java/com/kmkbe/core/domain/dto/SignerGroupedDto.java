package com.kmkbe.core.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SignerGroupedDto {
    private Long signerId;
    private String karyawanName;
    private String jabatan;
    private String identityNo;
    private String email;
}
