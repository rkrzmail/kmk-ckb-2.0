package com.kmkbe.feign.model.response;

import com.kmkbe.helpers.base.BaseResponse;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class LoginResponse extends BaseResponse {
  private boolean success;
  private int statusCode;
  private AuthData data; // Maps the "data" object wrapper
  private Object error;
  private String timestamp;

  @Data
  @Getter
  @Setter
  public static class AuthData {
    private String token;
    private String tokenType;
    private long expiresIn;
  }
}
