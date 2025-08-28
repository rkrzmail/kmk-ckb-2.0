package com.kmkbe.core.domain.model;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@Data
@NoArgsConstructor
public class CommonResult<T> {
    protected Boolean isSuccess;
    protected Integer code;
    protected Integer count;
    protected String message;
    protected T data;
    private Map<String, Object> extra = new HashMap<>();

    public CommonResult<T> success(T data) {
        this.isSuccess = true;
        this.code = 200;
        this.message = "Success";
        this.data = data;
        return this;
    }

    public void addProperty(String key, Object value) {
        this.extra.put(key, value);
    }

    public Map<String, Object> getExtra() {
        return this.extra;
    }

    public CommonResult<T> successWithCount(T data, Integer count ) {
        this.isSuccess = true;
        this.code = 200;
        this.message = "Success";
        this.count = count;
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
