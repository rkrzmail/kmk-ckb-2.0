package com.kmkbe.helpers.base;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * @author hyvercode
 * @date 6/26/26
 */

@Setter
@Getter
@ToString
public class BaseResponseBuilder<T> extends BaseResponse {

  protected boolean isSuccess;
  protected String code;
  protected String message;
  protected transient T data;

  @JsonSerialize(using = LocalDateTimeSerializer.class)
  @JsonDeserialize(using = LocalDateTimeDeserializer.class)
  protected LocalDateTime localDateTime;

  public BaseResponseBuilder() {}

  public BaseResponseBuilder(boolean isSuccess,String code, String message, T content, LocalDateTime localDateTime) {
    this.isSuccess=isSuccess;
    this.code = code;
    this.message = message;
    this.data= content;
    this.localDateTime = localDateTime;
  }

  public BaseResponseBuilder(boolean isSuccess,String code, String message, T content) {
    this.isSuccess=isSuccess;
    this.code = code;
    this.message= message;
    this.data= content;
    this.localDateTime = LocalDateTime.now();
  }

  public BaseResponseBuilder(boolean isSuccess,String code, String message) {
    this.isSuccess=isSuccess;
    this.code = code;
    this.message = message;
    this.localDateTime = LocalDateTime.now();
  }
}
