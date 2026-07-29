package com.kmkbe.feign.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfinsR3GetCustomerPersonalInfoDto {

  @JsonProperty("CustPersonalId")
  private Long custPersonalId;   // From "CustPersonalId" (Primary Key)
  @JsonProperty("CustId")
  private Long custId;           // Direct link to the main customer ID

  // --- Name Fields ---
  @JsonProperty("CustFullName")
  private String fullName;      // Combined full name
  @JsonProperty("CustPrefixName")
  private String prefixName;     // Prefix name (Mr., Ms.)
  @JsonProperty("CustSuffixName")
  private String suffixName;     // Suffix name

  // --- Personal Status & Demographics ---
  @JsonProperty("NickName")
  private String nickname;       // Nickname
  @JsonProperty("BirthPlace")
  private String birthPlace;
  @JsonProperty("BirthDt")// Location of birth
  private LocalDateTime birthDt;  // Date of Birth (Crucial to use proper Java time type)

  // Codes/Statuses (Using Strings is safest for codes as they might contain letters)
  @JsonProperty("MotherMaidenName")
  private String motherMaidenName; // Maiden name of the mother
  @JsonProperty("MrGenderCode")
  private String genderCode;     // e.g., "FEMALE", "MALE"
  @JsonProperty("MrReligionCode")
  private String religionCode;   // e.g., "ISLAM", "CHRISTIAN"
  @JsonProperty("MrEducationCode")
  private String educationCode;  // e.g., "D3", "S1"
  @JsonProperty("MrNationalityCode")
  private String nationalityCode;// e.g., "LOCAL", "FOREIGN"
  @JsonProperty("WnaCountryCode")
  private String wnaCountryCode; // World nationality country code
  @JsonProperty("MrMaritalStatCode")
  private String maritalStatCode; // e.g., "MARRIED"

  // --- Contact Details ---
  @JsonProperty("MobilePhnNo1")
  private String mobilePhnNo1;   // Primary phone number
  @JsonProperty("MobilePhnNo2")
  private String mobilePhnNo2;   // Secondary phone number
  @JsonProperty("Email1")
  private String email1;         // Primary email
  @JsonProperty("Email2")
  private String email2;
  @JsonProperty("Email3")
  private String email3;

  // --- Family & Dependents ---
  @JsonProperty("FamilyCardNo")
  private String familyCardNo; // National ID card number
  @JsonProperty("NoOfDependents")
  private Integer noOfDependents;// Number of dependents (Using Integer/Long)
  @JsonProperty("NoOfResidence")
  private String noOfResidence; // Is this a count or address? Use String if unclear.

  // Status Flags
  @JsonProperty("IsRestInPeace")
  private Boolean isRestInPeace; // Flag for deceased status

  // --- Codes (Titles and other static codes) ---
  @JsonProperty("MrSalutationCode")
  private String salutationCode; // e.g., "MR", "MRS"
}
