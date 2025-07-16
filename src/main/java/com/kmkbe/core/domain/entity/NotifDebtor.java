package com.kmkbe.core.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "notif_debtor")  // Nama tabel sesuai dengan yang diinginkan
public class NotifDebtor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notif_id", nullable = false)
    private Long notifId;

    @Column(name = "notification", nullable = false)  // Pastikan kolom ini tidak null
    private String notification;

    @Column(name = "description", nullable = false)  // Pastikan kolom ini tidak null
    private String description;

    @Column(name = "financing_hdr_code" )
    private String financingHdrCode;

    @Column(name = "usr_crt", nullable = false)
    private String usrCrt;

    @Column(name = "dtm_crt", nullable = false)
    private LocalDateTime dtmCrt;


}
