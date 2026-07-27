package com.kmkbe.helpers.base;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author hyvercode
 * @date 6/26/26
 */

@Setter
@Getter
@ToString
public class BaseResponseBuilder<T> extends BaseResponse {

  @JsonProperty("isSuccess")
  protected boolean isSuccess;
  protected Integer code;
  protected String message;
  protected transient T data;

  public BaseResponseBuilder() {}

  public BaseResponseBuilder(boolean isSuccess,Integer code, String message, T content) {
    this.isSuccess=isSuccess;
    this.code = code;
    this.message= message;
    this.data= content;
  }

  public BaseResponseBuilder(boolean isSuccess,Integer code, String message) {
    this.isSuccess=isSuccess;
    this.code = code;
    this.message = message;
  }
}
