package com.kmkbe.modules.loan_submission.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "bouwheer")
public class Bouwheer {
    @Id
    @Column(name = "bouwheer_code", nullable = false)
    private UUID bouwheerCode;

    @NotNull
    @ColumnDefault("nextval('bouwheer_bouwheer_id_seq'::regclass)")
    @Column(name = "bouwheer_id", nullable = false)
    private Long bouwheerId;

    @Size(max = 100)
    @NotNull
    @Column(name = "bouwheer_name", nullable = false, length = 100)
    private String bouwheerName;

    @Size(max = 1000)
    @NotNull
    @Column(name = "legal_address", nullable = false, length = 1000)
    private String legalAddress;

    @Size(max = 5)
    @NotNull
    @Column(name = "rt", nullable = false, length = 5)
    private String rt;

    @Size(max = 5)
    @NotNull
    @Column(name = "rw", nullable = false, length = 5)
    private String rw;

    @Size(max = 50)
    @NotNull
    @Column(name = "kelurahan", nullable = false, length = 50)
    private String kelurahan;

    @Size(max = 50)
    @NotNull
    @Column(name = "kecamatan", nullable = false, length = 50)
    private String kecamatan;

    @Size(max = 50)
    @NotNull
    @Column(name = "city", nullable = false, length = 50)
    private String city;

    @Size(max = 50)
    @NotNull
    @Column(name = "province", nullable = false, length = 50)
    private String province;

    @Size(max = 10)
    @NotNull
    @Column(name = "zipcode", nullable = false, length = 10)
    private String zipcode;

    @Size(max = 5)
    @Column(name = "area", length = 5)
    private String area;

    @Size(max = 20)
    @NotNull
    @Column(name = "phone", nullable = false, length = 20)
    private String phone;

    @NotNull
    @Column(name = "is_sbu", nullable = false)
    private Boolean isSbu = false;

    @Size(max = 50)
    @NotNull
    @Column(name = "pic_name", nullable = false, length = 50)
    private String picName;

    @Size(max = 50)
    @NotNull
    @Column(name = "pic_email", nullable = false, length = 50)
    private String picEmail;

    @Size(max = 20)
    @NotNull
    @Column(name = "pic_mobile_phone", nullable = false, length = 20)
    private String picMobilePhone;

    @NotNull
    @Column(name = "is_wa_active", nullable = false)
    private Boolean isWaActive = false;

    @NotNull
    @Column(name = "term_of_payment", nullable = false)
    private Long termOfPayment;

    @NotNull
    @Column(name = "grace_period", nullable = false)
    private Long gracePeriod;

    @Size(max = 16)
    @NotNull
    @Column(name = "aes_key", nullable = false, length = 16)
    private String aesKey;

    @NotNull
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = false;

    @Column(name = "secret_key")
    private String secretKey;

    @Column(name = "api_key")
    private String apiKey;

    @Size(max = 50)
    @NotNull
    @Column(name = "usr_crt", nullable = false, length = 50)
    private String usrCrt;

    @NotNull
    @Column(name = "dtm_crt", nullable = false)
    private Instant dtmCrt;

    @Size(max = 50)
    @Column(name = "usr_upd", length = 50)
    private String usrUpd;

    @Column(name = "dtm_upd")
    private Instant dtmUpd;

    @OneToMany(mappedBy = "bouwheerCode")
    private Set<FinancingHdr> financingHdrs = new LinkedHashSet<>();

    @OneToMany(mappedBy = "bouwheerCode")
    private Set<Invoice> invoices = new LinkedHashSet<>();

}
