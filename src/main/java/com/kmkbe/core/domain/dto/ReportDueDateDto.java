package com.kmkbe.core.domain.dto;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@Getter
@Setter
@Builder
@NoArgsConstructor
public class ReportDueDateDto implements Serializable {
    private String debtorName;
    private String npwp;
    private String bouwheerName;
    private String employeeName;
    private String branch;
    private String agreementNo;
    private LocalDateTime goliveDate;
    private long utilizationSeqNo;
    private double utilizationAmount;
    private double osAr;
    private double effectiveRate;
    private double retentionAmount;
    private double lcAmount;
    private LocalDateTime invoiceDueDate;
    private LocalDateTime settlementDate;
    private String financingStatus;

    public ReportDueDateDto( String debtorName,
                             String npwp,
                             String bouwheerName,
                             String employeeName,
                             String branch,
                             String agreementNo,
                             LocalDateTime goliveDate,
                             long utilizationSeqNo,
                             double utilizationAmount,
                             double osAr,
                             double effectiveRate,
                             double retentionAmount,
                             double lcAmount,
                             LocalDateTime invoiceDueDate,
                             LocalDateTime settlementDate,
                             String financingStatus)
    {
        this.debtorName = debtorName;
        this.npwp = npwp;
        this.bouwheerName = bouwheerName;
        this.employeeName = employeeName;
        this.branch = branch;
        this.agreementNo = agreementNo;
        this.goliveDate = goliveDate;
        this.utilizationSeqNo = utilizationSeqNo;
        this.utilizationAmount = utilizationAmount;
        this.osAr = osAr;
        this.effectiveRate = effectiveRate;
        this.retentionAmount = retentionAmount;
        this.lcAmount = lcAmount;
        this.invoiceDueDate = invoiceDueDate;
        this.settlementDate = settlementDate;
        this.financingStatus = financingStatus;
    }

}
