package com.kmkbe.modules.customer.entity;

import com.kmkbe.modules.common.entity.LoginLog;
import com.kmkbe.modules.loan_submission.entity.FinancingHdr;
import com.kmkbe.modules.loan_submission.entity.Invoice;
import com.kmkbe.modules.loan_submission.entity.LegalFile;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.util.*;


@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
@Entity
@Builder
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

    @Column
    private Boolean isActive = false;

    @Column(length = 50)
    private String usrCrt = "SYSTEM";

    @Column
    private Instant dtmCrt;

    @Column(length = 50)
    private String usrUpd;

    @Column
    private Instant dtmUpd;

    @OneToOne(mappedBy = "custCode")
    private CustomerPersonal personal;

    @OneToOne(mappedBy = "custCode")
    private CustomerCompany company;

    @OneToMany(mappedBy = "custCode")
    private Set<LoginLog> loginLogs;

    @OneToMany(mappedBy = "custCode")
    private Set<ChangePasswordLog> changePasswordLogs;

    @OneToMany(mappedBy = "custCode")
    private Set<Invoice> invoices = new LinkedHashSet<>();

    @OneToMany(mappedBy = "custCode")
    private Set<LegalFile> legalFiles = new LinkedHashSet<>();

    @OneToMany(mappedBy = "custCode")
    private Set<FinancingHdr> financingHdrs = new LinkedHashSet<>();

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
}
