package com.kmkbe.modules.user.entity;

import jakarta.persistence.*;
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
@Table(name = "mst_employee", schema = "users")
public class MstEmployee {
    @Id
    @Size(max = 10)
    @Column(name = "employee_code", nullable = false, length = 10)
    private String employeeCode;

    @NotNull
    @ColumnDefault("nextval('users.mst_employee_employee_id_seq'::regclass)")
    @Column(
            name = "employee_id",
            columnDefinition = "serial",
            insertable = false,
            updatable = false
    )
    private Long employeeId;

    @Size(max = 50)
    @Column(name = "employee_name", length = 50)
    private String employeeName;

    @Size(max = 20)
    @Column(name = "report_to_code", length = 20)
    private String reportToCode;

    @Size(max = 20)
    @Column(name = "employee_type", length = 20)
    private String employeeType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_code")
    private MstBranch branchCode;

    @Size(max = 50)
    @Column(name = "email", length = 50)
    private String email;

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
    private Instant dtmCrt;

    @Size(max = 50)
    @Column(name = "usr_upd", length = 50)
    private String usrUpd;

    @Column(name = "dtm_upd")
    private Instant dtmUpd;

}
