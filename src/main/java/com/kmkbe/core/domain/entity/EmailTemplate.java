package com.kmkbe.core.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
@Entity
@Table(name = "email_template", schema = "public")
public class EmailTemplate {

    @Column(
            nullable = false,
            columnDefinition = "serial",
            insertable = false,
            updatable = false
    )
    private Long emailTemplateId;

    @Id
    @Column(nullable = false, length = 50)
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
    private Date dtmCrt;

    @Column(length = 50)
    private String usrUpd;

    @Column
    private Date dtmUpd;

}
