package com.kmkbe.core.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;
import java.util.Set;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "agreement_file_signing", schema = "public")
public class AgreementFileSigning {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "agreement_file_id")
    private Long agreementFileId;

    @Column(name = "agreement_code", nullable = false)
    private String agreementCode;

    @Column(name = "file_type_code", nullable = false)
    private String fileTypeCode;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "stamp")
    private String stamp;

    @Column(name = "usr_crt")
    private String usrCrt;

    @ColumnDefault("now()")
    @Column(name = "dtm_crt")
    private LocalDateTime dtmCrt;

    @Size(max = 50)
    @Column(name = "usr_upd")
    private String usrUpd;

    @Column(name = "dtm_upd")
    private LocalDateTime dtmUpd;

    @Column(name = "document_id")
    private String documentId;

    @Column(name = "signer")
    private String signer;

    @Column(name = "identity_no")
    private String identityNo;

    @Column(name = "financing_hdr_code")
    private String financingHdrCode;

    // Getters and Setters
    public Long getAgreementFileId() { return agreementFileId; }
    public void setAgreementFileId(Long agreementFileId) { this.agreementFileId = agreementFileId; }

    public String getAgreementCode() { return agreementCode; }
    public void setAgreementCode(String agreementCode) { this.agreementCode = agreementCode; }

    public String getFileTypeCode() { return fileTypeCode; }
    public void setFileTypeCode(String fileTypeCode) { this.fileTypeCode = fileTypeCode; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String stamp() {return stamp;}
    public void setStamp(String stamp) {
        stamp = stamp;
    }

    public String getDocumentId() { return documentId; }
    public void setDocumentId(String documentId) { this.documentId = documentId; }

    public String getSigner() { return signer; }
    public void setSigner(String signer) { this.signer = signer; }

    public String getIdentityNo() { return identityNo; }
    public void setIdentityNo(String identityNo) { this.identityNo = identityNo; }

    public String getFinancingHdrCode() { return financingHdrCode; }
    public void setFinancingHdrCode(String financingHdrCode) { this.financingHdrCode = financingHdrCode; }
}
