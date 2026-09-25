package com.kmkbe.core.domain.dto;

import com.kmkbe.helpers.base.BaseResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginDto extends BaseResponse{
  String token;
  String refreshToken;
  Long expiresIn;
}
