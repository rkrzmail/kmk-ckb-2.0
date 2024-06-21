package com.kmkbe.modules.customer.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;


@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
@Entity
@Table(name = "customer", schema = "public")
public class Customer implements UserDetails {

    @Id
    @Column(nullable = false, updatable = false)
    @SequenceGenerator(
            name = "primary_sequence",
            sequenceName = "primary_sequence",
            allocationSize = 1
            //initialValue = 10000
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "primary_sequence"
    )
    private Long custId;

    @Column(nullable = false)
    private UUID custCode;

    @Column(length = 20)
    private String custNo;

    @Column(length = 500)
    private String custName;

    @Column(length = 50)
    private String custTypeCode;

    @Column(length = 4)
    private String custIdTypeCode;

    @Column(length = 20)
    private String custIdNo;

    @Column(length = 100)
    private String custEmail;

    @Column
    private Boolean isEmailValid;

    @Column(length = 20)
    private String custMobilePhone;

    @Column
    private Boolean isPhoneValid;

    @Column
    private Boolean isWaActive;

    @Column(length = 250)
    private String custPin;

    @Column
    private Boolean agreeTc;

    @Column
    private Boolean isActive;

    @Column(length = 50)
    private String usrCrt;

    @Column
    private OffsetDateTime dtmCrt;

    @Column(length = 50)
    private String usrUpd;

    @Column
    private OffsetDateTime dtmUpd;

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
