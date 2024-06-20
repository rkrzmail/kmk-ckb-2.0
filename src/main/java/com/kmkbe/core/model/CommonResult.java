package com.kmkbe.core.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommonResult {
    private Boolean isSuccess;
    private Integer code;
    private String message;
}
