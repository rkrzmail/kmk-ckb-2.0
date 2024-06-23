package com.kmkbe.core.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExceptionResult extends CommonResult{
    private Exception details;
}
