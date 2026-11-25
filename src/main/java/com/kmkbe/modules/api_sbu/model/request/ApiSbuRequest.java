package com.kmkbe.modules.api_sbu.model.request;


import com.kmkbe.helpers.base.BaseRequest;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

/**
 * Entity mapping tabel public.api_sbu
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiSbuRequest extends BaseRequest {


  @NotNull(message = "Bouwheer code cannot be null")
  @Column(name = "bouwheer_code", nullable = false)
  private String bouwheerCode;

  @Column(name = "expired_date")
  private LocalDate expiredDate;

  @Size(max = 64, message = "Session status cannot exceed 64 characters")
  @Column(name = "ses_status", length = 64)
  private String sesStatus;

  @Size(max = 64, message = "App path cannot exceed 64 characters")
  @Column(name = "app_path", length = 64)
  private String appPath;

  @Size(max = 64, message = "App name cannot exceed 64 characters")
  @Column(name = "app_name", length = 64)
  private String appName;

}
