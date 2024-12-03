package com.kmkbe.modules.user.entity;

import com.kmkbe.core.utils.DateTimeUtils;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "mst_user", schema = "users")
public class MstUser implements UserDetails {
    @Builder.Default
    @Id
    @Column(name = "user_code", nullable = false)
    private UUID userCode = UUID.randomUUID();

    @ColumnDefault("nextval('users.mst_user_user_id_seq'::regclass)")
    @Column(
            name = "user_id",
            columnDefinition = "serial",
            insertable = false,
            updatable = false
    )
    private Long userId;

    @Size(max = 50)
    @NotNull
    @Column(name = "username", nullable = false, length = 50)
    private String username;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "employee_code",
            referencedColumnName = "employee_code",
            nullable = false
    )
    private MstEmployee employee;

    @NotNull
    @ColumnDefault("false")
    @Column(name = "is_user_ad", nullable = false)
    private Boolean isUserAd = false;

    @NotNull
    @ColumnDefault("false")
    @Column(name = "is_user_nonad", nullable = false)
    private Boolean isUserNonad = false;

    @Size(max = 250)
    @Column(name = "password", length = 250)
    private String password;

    @Builder.Default
    @ColumnDefault("false")
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Builder.Default
    @Size(max = 50)
    @Column(name = "usr_crt", nullable = false, length = 50)
    private String usrCrt = "system";

    @Builder.Default
    @Column(name = "dtm_crt", nullable = false)
    private LocalDateTime dtmCrt = DateTimeUtils.now();

    @Size(max = 50)
    @Column(name = "usr_upd", length = 50)
    private String usrUpd;

    @Column(name = "dtm_upd")
    private LocalDateTime dtmUpd;

    @OneToMany(
            cascade = CascadeType.ALL,
            mappedBy = "user",
            fetch = FetchType.EAGER
    )
    private Set<MstAppRoleFormUser> appRoleFormsUser;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
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
