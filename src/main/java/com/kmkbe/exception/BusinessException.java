package com.kmkbe.exception;

import org.springframework.http.HttpStatus;

public class BusinessException extends BaseException {

  public BusinessException(Integer errorCode, String errorDesc, String errorMessage) {
    super(false,HttpStatus.CONFLICT, errorCode, errorDesc, errorMessage);
  }

  public BusinessException(HttpStatus httpStatus, Integer errorDesc, String errorMessage) {
    super(false,httpStatus, errorDesc, errorMessage, "");
  }

}
