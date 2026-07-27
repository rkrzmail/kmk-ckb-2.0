package com.kmkbe.exception;

import org.springframework.http.HttpStatus;

public class BusinessException extends BaseException {

  public BusinessException(String errorCode, String errorDesc, String errorMessage) {
    super(HttpStatus.CONFLICT, errorCode, errorDesc, errorMessage);
  }

  public BusinessException(HttpStatus httpStatus, String errorDesc, String errorMessage) {
    super(httpStatus, errorDesc, errorMessage, "");
  }

  public BusinessException(HttpStatus httpStatus, String errorCode, String errorDesc, String errorMessage) {
    super(httpStatus, errorCode, errorDesc, errorMessage);
  }
}
