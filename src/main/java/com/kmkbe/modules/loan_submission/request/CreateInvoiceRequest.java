package com.kmkbe.modules.loan_submission.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class CreateInvoiceRequest {
    @JsonProperty(required = true)
    @NotNull(message = "Please provide a valid custCode")
    private UUID custCode;

    @JsonProperty(required = true)
    @NotNull(message = "Please provide a valid custCode")
    private UUID bouwheerCode;

    private String bouwheerInvNo;
    private String custInvNo;
    private String invoiceDescription;
    private Instant invoiceDate;
    private Instant invoiceDueDate;
    private Double invoiceAmt;
}
