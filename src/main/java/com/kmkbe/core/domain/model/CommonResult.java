package com.kmkbe.core.domain.model;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommonResult<T> {
  private boolean isSuccess;
  private int code;
  private String message;
  private T data;

  public CommonResult<T> fail(int code, String message) {
    this.isSuccess = false;
    this.code = code;
    this.message = message;
    return this;
  }

  public CommonResult<T> fail(int code, String message, T payload) {
    this.isSuccess = false;
    this.code = code;
    this.message = message;
    this.data = payload;
    return this;
  }

  public CommonResult<T> success(T payload) {
    this.isSuccess = true;
    this.code = 200;
    this.message = "Success";
    this.data = payload;
    return this;
  }

}
