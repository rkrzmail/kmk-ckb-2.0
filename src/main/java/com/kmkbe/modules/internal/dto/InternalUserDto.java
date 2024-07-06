package com.kmkbe.modules.internal.dto;


import lombok.*;

@Getter
@Setter
@Data
@Builder
public class InternalUserDto {
    private boolean userValid;
    private String sn;
    private String username;
    private String fullName;
    private String email;
    private String branchCode;
    private String branchName;
    private String businessUnitID;
    private String departmentCode;
    private String department;
    private String empPositionCode;
    private String empPosition;
}
