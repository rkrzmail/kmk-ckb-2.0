package com.kmkbe.exception;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.http.HttpStatus;

@Setter
@Getter
@ToString
public class BaseException extends RuntimeException {

  protected final HttpStatus httpStatus;
  protected final Integer code;
  private final String message;

  public BaseException(HttpStatus httpStatus, Integer errorCode, String title, String errorMessage) {
    super(title);
    this.httpStatus = httpStatus;
    this.code = errorCode;
    this.message = errorMessage;
  }
}
