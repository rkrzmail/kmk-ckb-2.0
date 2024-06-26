package com.kmkbe.modules.common.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
@Entity
@Table(name = "email_template", schema = "public")
public class EmailTemplate {

    @Id
    @Column(nullable = false, updatable = false)
    @SequenceGenerator(
            name = "primary_sequence",
            sequenceName = "primary_sequence",
            allocationSize = 1
            //initialValue = 10000
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "primary_sequence"
    )
    private Long emailTemplateId;

    @Column(nullable = false, length = 20)
    private String emailTemplateCode;

    @Column(length = 1000)
    private String subjectMail;

    @Column(length = 8000)
    private String bodyMail;

    @Column(length = 1000)
    private String mailTo;

    @Column(length = 1000)
    private String mailCc;

    @Column(length = 1000)
    private String mailBcc;

    @Column(nullable = false)
    private Boolean isActive;

    @Column(nullable = false, length = 50)
    private String usrCrt;

    @Column(nullable = false)
    private OffsetDateTime dtmCrt;

    @Column(length = 50)
    private String usrUpd;

    @Column
    private OffsetDateTime dtmUpd;

}
