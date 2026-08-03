package com.kmkbe.exception;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.http.HttpStatus;

@Setter
@Getter
@ToString
public class BaseException extends RuntimeException {

  protected final boolean isSuccess;
  protected final HttpStatus httpStatus;
  protected final Integer code;
  private final String message;

  public BaseException(HttpStatus httpStatus, Integer errorCode, String errorMessage,String title) {
    super(title);
    this.isSuccess = false;
    this.httpStatus = httpStatus;
    this.code = errorCode;
    this.message = errorMessage;
  }

  public BaseException(boolean isSuccess,HttpStatus httpStatus, Integer errorCode, String errorMessage,String title) {
    super(title);
    this.isSuccess = isSuccess;
    this.httpStatus = httpStatus;
    this.code = errorCode;
    this.message = errorMessage;
  }
}
