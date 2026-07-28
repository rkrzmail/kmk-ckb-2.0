package com.kmkbe.feign.model.request;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Data
@Getter
@Setter
public class PostLoginRequest implements Serializable {
  private String username;
  private String password;
  private String clientSecret;

  public PostLoginRequest(String username, String password, String clientSecret) {
    this.username = username;
    this.password = password;
    this.clientSecret = clientSecret;
  }
}
