package com.kmkbe.modules.customer.model.entity;

import com.kmkbe.core.domain.entity.ChangePasswordLog;
import com.kmkbe.core.domain.entity.CustomerCompany;
import com.kmkbe.core.domain.entity.CustomerPersonal;
import com.kmkbe.core.domain.entity.LoginLog;
import com.kmkbe.helpers.utils.UuidConverter;
import com.kmkbe.modules.bouwheer.model.entity.Bouwheer;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "customer", schema = "public")
public class Customer implements UserDetails {
  @Column(
    nullable = false,
    columnDefinition = "serial",
    insertable = false,
    updatable = false
  )
  private Long custId;

  @Id
  @Column(name = "cust_code", nullable = false)
  private UUID custCode;

  @Column(length = 20)
  private String custNo = null;

  @Column(length = 500)
  private String custName;

  @Column(length = 50)
  private String custTypeCode = "";

  @Column(length = 4)
  private String custIdTypeCode = "";

  @Column(length = 20)
  private String custIdNo = "";

  @Column(length = 100)
  private String custEmail;

  @Column
  private Boolean isEmailValid = false;

  @Column(length = 20)
  private String custMobilePhone = "";

  @Column
  private Boolean isPhoneValid = false;

  @Column
  private Boolean isWaActive = false;

  @Column(length = 250)
  private String custPin;

  @Column
  private Boolean agreeTc = false;

  @Column(name = "agree_legal_share")
  private Boolean agreeLegalShare = false;

  @Column(name = "cust_external_code")
  private String custExternalCode;

  @Column
  private boolean isActive = false;

  @Column(name = "existing_cust")
  private String existingCust;

  @Column(length = 50)
  private String usrCrt = "SYSTEM";

  @Column
  private LocalDateTime dtmCrt;

  @Column(length = 50)
  private String usrUpd;

  @Column
  private LocalDateTime dtmUpd;

  @Column(name = "vendor_id", length = 60)
  private String vendorId;

  @Column(name = "approval_status", length = 10)
  private String approvalStatus;

  @Column(name = "approval_note", length = 100)
  private String approvalNote;

  @Column(name = "approval_by", length = 60)
  private String approvalBy;

  @Column
  private LocalDateTime approvalAt;

  @Column(name = "npwp", length = 20)
  private String npwp;

  @OneToOne(
    mappedBy = "customer",
    fetch = FetchType.EAGER
  )
  private CustomerPersonal personal;

  @OneToOne(
    mappedBy = "customer",
    fetch = FetchType.EAGER
  )
  private CustomerCompany company;

  @OneToMany(mappedBy = "custCode")
  private Set<LoginLog> loginLogs;

  @OneToMany(mappedBy = "custCode")
  private Set<ChangePasswordLog> changePasswordLogs;

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return List.of();
  }

  @Override
  public String getPassword() {
    return custPin;
  }

  @Override
  public String getUsername() {
    return custEmail;
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return true;
  }

  public CustomerCompany getCustomerCompany() {
    return this.company;
  }

  @Transient
  private Boolean forceLogout;

  public Boolean getForceLogout() {
    return forceLogout;
  }

  public void setForceLogout(Boolean forceLogout) {
    this.forceLogout = forceLogout;
  }

  @Column(name = "bouwheer", length = 36)
  private String bouwheer;

//  @OneToOne(cascade = CascadeType.ALL)
//  @JoinColumn(name = "bouwheer", referencedColumnName = "bouwheer_code", insertable = false, updatable = false)
//  private Bouwheer bouwheerDetail;



}
