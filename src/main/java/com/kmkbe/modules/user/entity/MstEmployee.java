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
    @JoinColumn(
            name = "branch_code",
            referencedColumnName = "branch_code",
            nullable = false
    )
    private MstBranch branch;

    @Size(max = 50)
    @Column(name = "email", length = 50)
    private String email;

    @Size(max = 20)
    @Column(name = "phone", length = 20)
    private String phone;

    @Builder.Default
    @ColumnDefault("false")
    @Column(name = "is_active")
    private Boolean isActive = true;

    @Builder.Default
    @Size(max = 50)
    @Column(name = "usr_crt", length = 50)
    private String usrCrt = "system";

    @Builder.Default
    @Column(name = "dtm_crt")
    private Instant dtmCrt = Instant.now();

    @Size(max = 50)
    @Column(name = "usr_upd", length = 50)
    private String usrUpd;

    @Column(name = "dtm_upd")
    private Instant dtmUpd;


}
