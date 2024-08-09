package com.kmkbe.modules.user.dto;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@Getter
@Builder
public class UserDto {
    private String employeeName;
    private String roleCode;
    private Boolean isActive;
    private List<UserRoleFormDto> permissions;
}
