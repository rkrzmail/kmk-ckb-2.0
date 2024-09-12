package com.kmkbe.modules.loan_submission.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.kmkbe.core.converter.DateDeserializer;
import com.kmkbe.core.domain.model.PostedInvoicePayload;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class CalculateSimulationRequest {
    private String token;

    @NotEmpty(message = "Please select at least 1 invoice to get calculated invoice amount")
    private BigDecimal totalInvoiceAmount;

    @NotEmpty(message = "Please swipe the indicator to get calculated percentage")
    private Double disbursePercentage;


    @NotEmpty(message = "Invoices is required, Please select at least 1 invoice")
    private String bouwheerCode;

    @NotEmpty(message = "Invoices is required, Please select at least 1 invoice")
    private  String invoiceDueDate;
}
