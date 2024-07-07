package com.kmkbe.core.model;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Data
@NoArgsConstructor
public class CommonResult<T> {
    protected Boolean isSuccess;
    protected Integer code;
    protected String message;
    protected T data;

    public CommonResult<T> success(T data) {
        this.isSuccess = true;
        this.code = 200;
        this.message = "Success";
        this.data = data;
        return this;
    }

    public CommonResult<T> success(T data, String message) {
        this.isSuccess = true;
        this.code = 200;
        this.message = message;
        this.data = data;
        return this;
    }

    public CommonResult<T> fail(Integer code, String message) {
        return fail(code, message, null);
    }

    public CommonResult<T> fail(Integer code, String message, T data) {
        this.isSuccess = false;
        this.code = code;
        this.message = message;
        this.data = data;
        return this;
    }
}
