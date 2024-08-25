package com.kmkbe.modules.loan_submission.request;

import com.kmkbe.core.domain.model.PostedInvoicePayload;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Data
public class CreateSimulationRequest {
    @NotNull(message = "Product id is required")
    private Long productId;

    @NotNull(message = "Disburse percentage is required")
    private Double disbursePercentage;

    @NotNull(message = "Invoices is required, Please select at least 1 invoice")
    @NotEmpty(message = "Invoices is required, Please select at least 1 invoice")
    private List<PostedInvoicePayload> invoices;
}
