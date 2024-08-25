package com.kmkbe.core.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "branch_area_mapping", schema = "public")
public class BranchAreaMapping {
    @Id
    @Size(max = 3)
    @Column(name = "branch_code", nullable = false, length = 3)
    private String branchCode;

    @Column(
            name = "branch_area_mapping_id",
            columnDefinition = "serial",
            nullable = false,
            insertable = false,
            updatable = false
    )
    private Long branchAreaMappingId;

    @Size(max = 50)
    @NotNull(message = "area null")
    @Column(name = "area", nullable = false, length = 50)
    private String area;

    @Size(max = 50)
    @NotNull(message = "province null")
    @Column(name = "province", nullable = false, length = 50)
    private String province;

    @Size(max = 50)
    @NotNull(message = "city null")
    @Column(name = "city", nullable = false, length = 50)
    private String city;

    @NotNull(message = "is active null")
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = false;

    @Size(max = 50)
    @NotNull(message = "usr crt null")
    @Column(name = "usr_crt", nullable = false, length = 50)
    private String usrCrt;

    @NotNull
    @ColumnDefault("now()")
    @Column(name = "dtm_crt", nullable = false)
    private Instant dtmCrt;

    @Size(max = 50)
    @Column(name = "usr_upd", length = 50)
    private String usrUpd;

    @Column(name = "dtm_upd")
    private Instant dtmUpd;
}
