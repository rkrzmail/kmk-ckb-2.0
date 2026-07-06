package com.kmkbe.modules.loan_submission.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class CalculateCreateSimulationRequest extends CreateSimulationRequest {
    private String token;

     private BigDecimal totalInvoiceAmount;

     private Double disbursePercentage;


      private String bouwheerCode;

     private  String invoiceDueDate;

    private  Double interest;


    private  String vendorCode;
}
