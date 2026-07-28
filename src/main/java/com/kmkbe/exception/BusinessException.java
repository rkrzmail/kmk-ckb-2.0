package com.kmkbe.exception;

import org.springframework.http.HttpStatus;

public class BusinessException extends BaseException {

  public BusinessException(Integer errorCode, String errorDesc, String errorMessage) {
    super(HttpStatus.CONFLICT, errorCode, errorDesc, errorMessage);
  }

  public BusinessException(HttpStatus httpStatus, Integer errorDesc, String errorMessage) {
    super(httpStatus, errorDesc, errorMessage, "");
  }

  public BusinessException(HttpStatus httpStatus, Integer errorCode, String errorDesc, String errorMessage) {
    super(httpStatus, errorCode, errorDesc, errorMessage);
  }
}
