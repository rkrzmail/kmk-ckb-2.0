package com.kmkbe.core.domain.dto;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProyeksiReportDto implements Serializable {
    private String debtorName;
    private String debtorStatus;
    private String bouwheerName;
    private String invoiceNo;
    private Double amountInvoice;
    private Double amountFinancing;
    private LocalDateTime invoiceDueDate;
    private LocalDateTime effectiveDate;
}
