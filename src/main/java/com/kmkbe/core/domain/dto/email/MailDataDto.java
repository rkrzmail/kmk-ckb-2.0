package com.kmkbe.core.domain.dto.email;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
@Builder
public class MailDataDto {
    public String employeeCode;
    public String employeeName;
    public String email;
    public String branchCode;
}
