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

  @Size(min = 0, max = 3, message = "RT Must be between 0 and 3 characters")
  private String rt;

  @Size(min = 0, max = 3, message = "RW Must be between 0 and 3 characters")
  private String rw; // Assuming RW is short if provided

  @Size(min = 0, max = 100, message = "Kelurahan Must be between 0 and 100 characters")
  private String kelurahan;

  @Size(min = 0, max = 100, message = "Kecamatan Must be between 0 and 100 characters")
  private String kecamatan;

  @Size(min = 0, max = 150, message = "Kota Must be between 0 and 150 characters")
  @Size(max = 150)
  private String city;

  @Size(min = 0, max = 100, message = "Province Must be between 0 and 100 characters")
  private String province;

  @Pattern(regexp = "^[A-Za-z0-9\\s-]+$", message = "Invalid zip code format")
  @Size(max = 20)
  private String zipcode;

  @Size(min = 0, max = 5, message = "Area Must be between 0 and 5 characters")
  private String area;

  @Pattern(regexp = "^[\\d\\-\\s]+$", message = "Phone number must contain only digits, hyphens, or spaces")
  private String phone;

  private Boolean isSbu;

  @NotNull(message = "PIC Name is required")
  @NotBlank(message = "PIC Name cannot be empty")
  @Size(min = 3, max = 100, message = "Must be between 3 and 100 characters")
  private String picName;

  @Pattern(
    regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$",
    message = "Invalid email format"
  )
  private String picEmail;

  @Pattern(regexp = "^[\\d\\-\\s]+$", message = "Mobile phone must contain only digits, hyphens, or spaces")
  private String picMobilePhone;

  private Boolean isWaActive;

  // Long types usually don't need validation unless range constraints are known
  private Long termOfPayment;
  private Long gracePeriod;

  private Boolean isActive;
}
