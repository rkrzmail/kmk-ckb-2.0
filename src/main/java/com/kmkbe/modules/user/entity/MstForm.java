package com.kmkbe.modules.user.entity;

import com.kmkbe.core.utils.DateTimeUtils;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "mst_form", schema = "users")
public class MstForm {
    @Id
    @Size(max = 20)
    @Column(name = "form_code", nullable = false, length = 20)
    private String formCode;

    @NotNull
    @ColumnDefault("nextval('users.mst_form_form_id_seq'::regclass)")
    @Column(
            name = "form_id",
            columnDefinition = "serial",
            insertable = false,
            updatable = false
    )
    private Long formId;

    @Size(max = 100)
    @Column(name = "form_name", length = 100)
    private String formName;

    @Size(max = 500)
    @Column(name = "form_path", length = 500)
    private String formPath;

    @Size(max = 50)
    @Column(name = "form_icon", length = 50)
    private String formIcon;

    @Size(max = 20)
    @Column(name = "parent_code", length = 20)
    private String parentCode;

    @Column(name = "order_no")
    private Short orderNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "module_code")
    private MstModule moduleCode;

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
    private LocalDateTime dtmCrt = DateTimeUtils.now();

    @Size(max = 50)
    @Column(name = "usr_upd", length = 50)
    private String usrUpd;

    @Column(name = "dtm_upd")
    private LocalDateTime dtmUpd;

}
