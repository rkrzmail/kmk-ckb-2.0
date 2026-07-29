package com.kmkbe.feign.model.dto;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PostLoginDto{
  private boolean success;
  private int statusCode;
  private transient AuthData data; // Maps the "data" object wrapper
  private transient Object error;
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
