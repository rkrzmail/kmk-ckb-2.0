package com.kmkbe.core.domain.entity;

import com.kmkbe.modules.user.entity.MstBranch;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;



@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "branch_area_mapping", schema = "public")
public class BranchAreaMapping {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "branch_area_mapping_id_gen")
    @SequenceGenerator(name = "branch_area_mapping_id_gen", sequenceName = "branch_area_mapping_branch_area_mapping_id_seq", allocationSize = 1)
    @Column(name = "branch_area_mapping_id", nullable = false)
    private Long branchAreaMappingId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "branch_code",
            referencedColumnName = "branch_code",
            nullable = false
    )
    private MstBranch mstBranch;

    @Size(max = 50)
    @NotNull
    @Column(name = "area", nullable = false, length = 50)
    private String area;

    @Size(max = 50)
    @NotNull
    @Column(name = "province", nullable = false, length = 50)
    private String province;

    @Size(max = 50)
    @NotNull
    @Column(name = "city", nullable = false, length = 50)
    private String city;

    @NotNull
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = false;

    @Size(max = 50)
    @NotNull
    @Column(name = "usr_crt", nullable = false, length = 50)
    private String usrCrt;

    @NotNull
    @ColumnDefault("now()")
    @Column(name = "dtm_crt", nullable = false)
    private LocalDateTime dtmCrt;

    @Size(max = 50)
    @Column(name = "usr_upd", length = 50)
    private String usrUpd;

    @Column(name = "dtm_upd")
    private LocalDateTime dtmUpd;

}
