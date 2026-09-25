package com.kmkbe.core.domain.entity;

import com.kmkbe.core.utils.DateTimeUtils;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "financing_dtl")
public class FinancingDtl {
  @Id
  @Column(name = "financing_dtl_code", nullable = false)
  private UUID financingDtlCode;

  @Column(
    name = "financing_dtl_id",
    columnDefinition = "serial",
    insertable = false,
    updatable = false
  )
  private Long financingDtlId;

  @NotNull
  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(
    name = "financing_hdr_code",
    referencedColumnName = "financing_hdr_code",
    nullable = false,
    updatable = false
  )
  private FinancingHdr financingHdr;

  @NotNull
  @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  @JoinColumn(
    name = "invoice_code",
    referencedColumnName = "invoice_code",
    nullable = false
  )
  private Invoice invoice;

  @Size(max = 50)
  @NotNull
  @Column(name = "bouwheer_inv_no", nullable = false, length = 50)
  private String bouwheerInvNo;

  @Column(name = "paid_to_cust_date")
  private LocalDateTime paidToCustDate;

  @Column(name = "bouwheer_paid_date")
  private LocalDateTime bouwheerPaidDate;

  @Size(max = 50)
  @NotNull
  @Column(name = "usr_crt", nullable = false, length = 50)
  private String usrCrt;

  @NotNull
  @Column(name = "dtm_crt", nullable = false)
  private LocalDateTime dtmCrt = DateTimeUtils.now();
  ;

  @Size(max = 50)
  @Column(name = "usr_upd", length = 50)
  private String usrUpd;

  @Column(name = "dtm_upd")
  private LocalDateTime dtmUpd;

}
