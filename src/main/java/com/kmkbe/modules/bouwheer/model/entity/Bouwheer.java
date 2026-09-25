package com.kmkbe.modules.bouwheer.model.entity;

import com.kmkbe.core.domain.entity.FinancingHdr;
import com.kmkbe.core.domain.entity.Invoice;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "bouwheer")
public class Bouwheer {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "bouwheer_code", nullable = false)
  private UUID bouwheerCode;

  @Column(
    name = "bouwheer_id",
    columnDefinition = "serial",
    nullable = false,
    insertable = false,
    updatable = false
  )
  private Integer bouwheerId;

  @Size(max = 100)

  @Column(name = "bouwheer_name", nullable = false, length = 100)
  private String bouwheerName;

  @Size(max = 1000)

  @Column(name = "legal_address", nullable = false, length = 1000)
  private String legalAddress;

  @Size(max = 5)

  @Column(name = "rt", nullable = false, length = 5)
  private String rt;

  @Size(max = 5)

  @Column(name = "rw", nullable = false, length = 5)
  private String rw;

  @Size(max = 50)

  @Column(name = "kelurahan", nullable = false, length = 50)
  private String kelurahan;

  @Size(max = 50)

  @Column(name = "kecamatan", nullable = false, length = 50)
  private String kecamatan;

  @Size(max = 50)

  @Column(name = "city", nullable = false, length = 50)
  private String city;

  @Size(max = 50)

  @Column(name = "province", nullable = false, length = 50)
  private String province;

  @Size(max = 10)

  @Column(name = "zipcode", nullable = false, length = 10)
  private String zipcode;

  @Size(max = 5)
  @Column(name = "area", length = 5)
  private String area;

  @Size(max = 20)

  @Column(name = "phone", nullable = false, length = 20)
  private String phone;


  @Column(name = "is_sbu", nullable = false)
  private Boolean isSbu = false;

  @Size(max = 50)

  @Column(name = "pic_name", nullable = false, length = 50)
  private String picName;

  @Size(max = 50)

  @Column(name = "pic_email", nullable = false, length = 50)
  private String picEmail;

  @Size(max = 20)

  @Column(name = "pic_mobile_phone", nullable = false, length = 20)
  private String picMobilePhone;


  @Column(name = "is_wa_active", nullable = false)
  private Boolean isWaActive = false;


  @Column(name = "term_of_payment", nullable = false)
  private Long termOfPayment;


  @Column(name = "grace_period", nullable = false)
  private Long gracePeriod;

  @Size(max = 16)

  @Column(name = "aes_key", nullable = false, length = 16)
  private String aesKey;


  @Column(name = "is_active", nullable = false)
  @Builder.Default
  private Boolean isActive = false;

  @Column(name = "secret_key")
  private String secretKey;

  @Column(name = "api_key")
  private String apiKey;

  @Column(name = "min_retention")
  private Float minRetention;

  @Size(max = 50)

  @Column(name = "usr_crt", nullable = false, length = 50)
  private String usrCrt;


  @Column(name = "dtm_crt", nullable = false)
  private LocalDateTime dtmCrt;

  @Size(max = 50)
  @Column(name = "usr_upd", length = 50)
  private String usrUpd;

  @Column(name = "dtm_upd")
  private LocalDateTime dtmUpd;

  @OneToMany(mappedBy = "bouwheer")
  private Set<FinancingHdr> financingHdrs = new LinkedHashSet<>();

  @OneToMany(mappedBy = "bouwheer")
  private Set<Invoice> invoices = new LinkedHashSet<>();

}
