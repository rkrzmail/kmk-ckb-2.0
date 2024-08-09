package com.kmkbe.modules.user.dto;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.Builder;
import lombok.Getter;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@Getter
@Builder
public class UserRoleFormDto {
    private String parentCode;
    private String name;
    private String path;
    private String icon;
}
