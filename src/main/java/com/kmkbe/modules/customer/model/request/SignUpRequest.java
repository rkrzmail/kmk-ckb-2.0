package com.kmkbe.modules.customer.model.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.kmkbe.core.converter.ToLowerCaseDeserializer;
import com.kmkbe.core.utils.CommonFormattingUtils;
import com.kmkbe.core.domain.constant.CustomerModel;
import com.kmkbe.helpers.base.BaseRequest;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class SignUpRequest extends BaseRequest {
  @NotNull(message = "vendorCode cannot be null")
  @NotEmpty(message = "vendorCode cannot be empty")
  private String vendorCode;

  @NotNull(message = "Nama cannot be null")
  @NotEmpty(message = "Nama cannot be empty")
  private String name;

  @NotNull(message = "Tipe Debitur cannot be null")
  @NotEmpty(message = "Tipe Debitur cannot be empty")
  private String customerType;

  @NotNull(message = "KTP/NPWP cannot be null")
  @NotEmpty(message = "KTP/NPWP cannot be empty")
  private String customerIdNo;

  private String customerNo;

  @NotNull(message = "Email cannot be null")
  @NotEmpty(message = "Email cannot be empty")
  @Pattern(regexp = CommonFormattingUtils.REGEX_EMAIL, message = "Email is not valid")
  @JsonDeserialize(using = ToLowerCaseDeserializer.class)
  private String email;

  @NotNull(message = "No. Hp cannot be null")
  @NotEmpty(message = "No. Hp cannot be empty")
  private String mobilePhone;

  @NotNull(message = "ID Vendor cannot be null")
  @NotEmpty(message = "ID Vendor cannot be empty")
  private String vendorId;

  @NotNull(message = "Bouwheer cannot be null")
  @NotEmpty(message = "Bouwheer cannot be empty")
  private String bouwheerCode;

  @NotNull(message = "Pin cannot be null")
  @NotEmpty(message = "Pin cannot be empty")
  @Length(message = "Pin must be 6 digit", min = 6, max = 6)
  private String pin;

  @NotNull(message = "Setujui Syarat & Ketentuan cannot be null")
  private boolean isAgreeTc;


  @JsonIgnore
  private transient Company company;

  @JsonIgnore
  private transient Personal personal;

  @SuperBuilder
  @Getter
  @AllArgsConstructor
  @NoArgsConstructor
  public static class AddressDetail {
    private String rt;
    private String rw;
    private String kelurahan;
    private String kecamatan;
    private String city;
    private String province;
    private String zipCode;
    private String area;
    private String phone;
    private String ownershipStatus;
    private LocalDateTime staySince;
  }

  @SuperBuilder
  @Getter
  @NoArgsConstructor
  @AllArgsConstructor
  @ToString
  public static class Company extends AddressDetail {
    private String companyType;
    private String companyModel;
    private String identityType;
    private String identityNo;
    private LocalDateTime identityIssuedDate;
    private LocalDateTime identityExpiredDate;
    private String companyAddress;
    private String custIdNo;

  }

  @SuperBuilder
  @Getter
  @NoArgsConstructor
  @AllArgsConstructor
  @ToString
  public static class Personal extends AddressDetail {
    private String birthPlace;
    private LocalDateTime birthDate;
    private String gender;
    private String identityType;
    private String identityNo;
    private LocalDateTime expiredDate;
    private String motherMaidenName;
    private String maritalStatus;
    private CustomerModel customerModel;
    private String legalAddress;
  }
}
