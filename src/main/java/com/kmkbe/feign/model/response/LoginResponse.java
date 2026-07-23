package com.kmkbe.feign.model.response;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Data
@Getter
@Setter
public class LoginResponse implements Serializable {
  private boolean success;
  private int statusCode;
  private AuthData data; // Maps the "data" object wrapper
  private Object error;
  private String timestamp;

  // Nested Class matching your real payload keys
  public static class AuthData {
    private String token;
    private String tokenType;
    private long expiresIn;

    // Getters and Setters
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public String getTokenType() { return tokenType; }
    public void setTokenType(String tokenType) { this.tokenType = tokenType; }
    public long getExpiresIn() { return expiresIn; }
    public void setExpiresIn(long expiresIn) { this.expiresIn = expiresIn; }
  }
}
