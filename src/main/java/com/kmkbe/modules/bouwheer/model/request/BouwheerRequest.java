package com.kmkbe.modules.bouwheer.model.request;

import com.kmkbe.helpers.base.BaseRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BouwheerRequest extends BaseRequest {

  @NotNull(message = "Bouwheer Name Code is required")
  @NotBlank(message = "Bouwheer Name cannot be empty") // Changed message for clarity
  @Size(min = 1, max = 100, message = "Must be between 1 and 100 characters")
  private String bouwheerName;

  @NotNull(message = "Legal Address is required")
  @NotBlank(message = "Legal Address cannot be empty") // Changed message for clarity
  @Size(min = 3, max = 1000, message = "Must be between 3 and 1000 characters")
  private String legalAddress;

  @NotNull(message = "RT is required")
  @NotBlank(message = "RT cannot be empty") // Changed message for clarity
  @Size(min = 0, max = 100, message = "Must be between 0 and 100 characters")
  private String rt;

  @Size(max = 50)
  private String rw; // Assuming RW is short if provided

  @Size(max = 100)
  private String kelurahan;

  @Size(max = 100)
  private String kecamatan;

  @Pattern(regexp = "^[A-Za-z0-9\\s]+$", message = "City contains invalid characters")
  @Size(max = 150)
  private String city;

  @Size(max = 100)
  private String province;

  @Pattern(regexp = "^[A-Za-z0-9\\s-]+$", message = "Invalid zip code format")
  @Size(max = 20)
  private String zipcode;
  private String area;

  @Pattern(regexp = "^[\\d\\-\\s]+$", message = "Phone number must contain only digits, hyphens, or spaces")
  private String phone;
  private Boolean isSbu;

  @Size(max = 100)
  private String picName;

  @Pattern(regexp = "^[A-Za-z0-9+_.-]+@[A-Za-z.]{2,6}$", message = "Invalid email format")
  private String picEmail;

  @Pattern(regexp = "^[\\d\\-\\s]+$", message = "Mobile phone must contain only digits, hyphens, or spaces")
  private String picMobilePhone;

  private Boolean isWaActive;

  // Long types usually don't need validation unless range constraints are known
  private Long termOfPayment;
  private Long gracePeriod;

  private Boolean isActive;
}
