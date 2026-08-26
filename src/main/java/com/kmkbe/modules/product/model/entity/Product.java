package com.kmkbe.modules.product.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.kmkbe.modules.bouwheer.model.entity.Bouwheer;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;
import java.util.UUID;


@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "product")
public class Product {
  @Id
  @ColumnDefault("nextval('product_product_id_seq'::regclass)")
  @GeneratedValue(strategy = GenerationType.IDENTITY)  // Pastikan auto-generated
  @Column(name = "product_id", nullable = false)
  private Long productId;

  @Size(max = 3)
  // @NotNull
  @Column(name = "branch_code", nullable = false, length = 3)
  private String branchCode;

  @Size(max = 100)
  // @NotNull
  @Column(name = "product_name", nullable = false, length = 100)
  private String productName;

  @Size(max = 100)
  // @NotNull
  @Column(name = "product_code", nullable = false, length = 100)
  private String productCode;

  @JsonIgnore
  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "bouwheer_code")
  private Bouwheer bouwheer;

  // @NotNull
  @Column(name = "effective_date", nullable = false)
  private LocalDateTime effectiveDate;

  // @NotNull
  @Column(name = "ntf_from", nullable = false) //, precision = 17, scale = 2
  private Double ntfFrom;

  // @NotNull
  @Column(name = "ntf_to", nullable = false) // , precision = 17, scale = 2
  private Double ntfTo;

  // @NotNull
  @Column(name = "effective_rate", nullable = false) // , precision = 5, scale = 2
  private Double effectiveRate;

  // @NotNull
  @Column(name = "provision_rate", nullable = false) // , precision = 5, scale = 2
  private Double provisionRate;

  // @NotNull
  @Column(name = "survey_fee", nullable = false) // , precision = 17, scale = 2
  private Double surveyFee;

  // @NotNull
  @Column(name = "legal_fee", nullable = false) // , precision = 17, scale = 2
  private Double legalFee;

  // @NotNull
  @Column(name = "admin_limit_fee", nullable = false) // , precision = 17, scale = 2
  private Double adminLimitFee;

  // @NotNull
  @Column(name = "admin_rate", nullable = false) // , precision = 5, scale = 2
  private Double adminRate;

  // @NotNull
  @Column(name = "insurance_rate", nullable = false) // , precision = 5, scale = 2
  private Double insuranceRate;

  // @NotNull
  @Column(name = "others_fee", nullable = false) //, precision = 17, scale = 2
  private Double othersFee;

  // @NotNull
  @Column(name = "is_active", nullable = false)
  @Builder.Default
  private Boolean isActive = false;

  @Size(max = 50)
  // @NotNull
  @Column(name = "usr_crt", nullable = false, length = 50)
  private String usrCrt;

  // @NotNull
  @Column(name = "dtm_crt", nullable = false)
  private LocalDateTime dtmCrt;

  @Size(max = 50)
  @Column(name = "usr_upd", length = 50)
  private String usrUpd;

  @Column(name = "dtm_upd")
  private LocalDateTime dtmUpd;

}
