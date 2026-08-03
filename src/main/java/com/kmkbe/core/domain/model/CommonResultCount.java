package com.kmkbe.core.domain.model;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Data
@NoArgsConstructor
public class CommonResultCount<T> extends CommonResult<T>{
    protected Boolean isSuccess;
    protected int code;
    protected Integer count;
    protected String message;
    protected T data;

    public CommonResultCount<T> success(T data) {
        this.isSuccess = true;
        this.code = 200;
        this.message = "Success";
        this.data = data;
        return this;
    }

    public CommonResultCount<T> success(T data, String message, int count) {
        this.isSuccess = true;
        this.code = 200;
        this.message = message;
        this.data = data;
        return this;
    }

    public CommonResultCount<T> fail(Integer code, String message) {
        return fail(code, message, null);
    }

    public CommonResultCount<T> fail(Integer code, String message, T data) {
        this.isSuccess = false;
        this.code = code;
        this.message = message;
        this.data = data;
        return this;
    }
}
