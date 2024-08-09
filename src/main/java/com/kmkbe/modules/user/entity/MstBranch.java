package com.kmkbe.modules.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "mst_branch", schema = "users")
public class MstBranch {
    @Id
    @Size(max = 20)
    @Column(name = "branch_code", nullable = false, length = 20)
    private String branchCode;

    @NotNull
    @ColumnDefault("nextval('users.mst_branch_branch_id_seq'::regclass)")
    @Column(
            name = "branch_id",
            columnDefinition = "serial",
            insertable = false,
            updatable = false
    )
    private Long branchId;

    @Size(max = 50)
    @Column(name = "branch_name", length = 50)
    private String branchName;

    @Size(max = 50)
    @Column(name = "branch_initial", length = 50)
    private String branchInitial;

    @Size(max = 50)
    @Column(name = "business_unit", length = 50)
    private String businessUnit;

    @Size(max = 20)
    @Column(name = "cg_id", length = 20)
    private String cgId;

    @Size(max = 1000)
    @Column(name = "address", length = 1000)
    private String address;

    @Size(max = 5)
    @Column(name = "rt", length = 5)
    private String rt;

    @Size(max = 5)
    @Column(name = "rw", length = 5)
    private String rw;

    @Size(max = 50)
    @Column(name = "kelurahan", length = 50)
    private String kelurahan;

    @Size(max = 50)
    @Column(name = "kecamatan", length = 50)
    private String kecamatan;

    @Size(max = 50)
    @Column(name = "city", length = 50)
    private String city;

    @Size(max = 50)
    @Column(name = "province", length = 50)
    private String province;

    @Size(max = 10)
    @Column(name = "zipcode", length = 10)
    private String zipcode;

    @Size(max = 5)
    @Column(name = "area", length = 5)
    private String area;

    @Size(max = 20)
    @Column(name = "phone", length = 20)
    private String phone;

    @NotNull
    @ColumnDefault("false")
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = false;

    @Size(max = 50)
    @NotNull
    @Column(name = "usr_crt", nullable = false, length = 50)
    private String usrCrt;

    @NotNull
    @Column(name = "dtm_crt", nullable = false)
    private Instant dtmCrt = Instant.now();

    @Size(max = 50)
    @Column(name = "usr_upd", length = 50)
    private String usrUpd;

    @Column(name = "dtm_upd")
    private Instant dtmUpd;

}
