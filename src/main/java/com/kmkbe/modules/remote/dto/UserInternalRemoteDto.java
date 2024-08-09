package com.kmkbe.modules.remote.dto;


import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInternalRemoteDto {
    private Boolean userValid;
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
