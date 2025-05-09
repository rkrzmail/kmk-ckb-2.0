package com.kmkbe.core.domain.entity;

import jakarta.persistence.*;
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
@Table(name = "visitor_log")
public class Visitor {

    @Id
    @ColumnDefault("nextval('product_product_id_seq'::regclass)")
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // Pastikan auto-generated
    @Column(name = "visitor_id", nullable = false)
    private Long visitorId;

    @Column(name = "vendor_code")
    private String vendorCode;

    @Size(max = 50)
    @Column(name = "debtor_name", length = 50)
    private String debtorName;

    @Size(max = 10)
    @Column(name = "debtor_status", length = 10)
    private String debtorStatus;

    @Size(max = 50)
    @Column(name = "bouwheer_name", length = 50)
    private String bouwheerName;

    @Column(name = "visit_date", nullable = false)
    private LocalDateTime visitDate;

}
