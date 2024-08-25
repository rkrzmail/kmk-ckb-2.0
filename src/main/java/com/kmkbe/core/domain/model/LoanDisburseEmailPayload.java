package com.kmkbe.core.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class LoanDisburseEmailPayload {
    private String financingCode;
    private String companyName;
    private String phoneNumber;

    /**
     * date format with dd/MM/yyyy see DateSerializer
     */
    private String applicationDate;

    /**
     * Start Should be used BigDecimal, but with performance issue. used direct string
     */
    private String invoiceAmt;

    /**
     * Start Should be used BigDecimal, but with performance issue. used direct string
     */
    private String retention;

    /**
     * Start Should be used BigDecimal, but with performance issue. used direct string
     */
    private String financingAmt;

    /**
     * Start Should be used BigDecimal, but with performance issue. used direct string
     */
    private String totalFeeAmt;

    /**
     * Start Should be used BigDecimal, but with performance issue. used direct string
     */
    private String disburseAmt;

    private Long tenor;

    /**
     * date format with dd/MM/yyyy see DateSerializer
     */
    private String financingDueDate;

    private List<InvoicePayload> invoices;

    @Getter
    @Builder
    public static class InvoicePayload {
        private Long seq;
        private String description;
        private String bouwheerName;

        /**
         * date format with dd/MM/yyyy see DateSerializer
         */
        private String invoiceDate;

        /**
         * date format with dd/MM/yyyy see DateSerializer
         */
        private String invoiceDueDate;

        /**
         * Start Should be used BigDecimal, but with performance issue. used direct string
         */
        private String invoiceAmt;

        public static String toHtmlListBody(List<InvoicePayload> payloads) {
            if (payloads.isEmpty()) {
                return "";
            }

            final StringBuilder result = new StringBuilder();

            for (int i = 0; i < payloads.size(); i++) {
                final InvoicePayload payload = payloads.get(i);

                if (i % 2 == 0) {
                    result.append("<tr>").append("\n");
                } else {
                    result.append("<tr style=" + "\"background-color: #E7ECFF;\"" + ">").append("\n");
                }

                result.append("<td>").append(payload.getSeq()).append("</td>").append("\n");
                result.append("<td>").append(payload.getDescription()).append("</td>").append("\n");
                result.append("<td>").append(payload.getBouwheerName()).append("</td>").append("\n");
                result.append("<td>").append(payload.getInvoiceDate()).append("</td>").append("\n");
                result.append("<td>").append(payload.getInvoiceDueDate()).append("</td>").append("\n");
                result.append("<td>").append(payload.getInvoiceAmt()).append("</td>").append("\n");
                result.append("</tr>").append("\n");
            }
            return result.toString();
        }
    }
}
