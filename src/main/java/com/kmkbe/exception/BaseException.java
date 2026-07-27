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
  protected final String errorCode;
  private final String errorMessage;

  public BaseException(HttpStatus httpStatus, String errorCode, String title, String errorMessage) {
    super(title);
    this.httpStatus = httpStatus;
    this.errorCode = errorCode;
    this.errorMessage = errorMessage;
  }
}
