package com.kmkbe.core.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class InvoiceEmailPayload {
    private String invoiceNo;
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

    public static String toHtmlListBody(List<InvoiceEmailPayload> payloads) {
        if (payloads.isEmpty()) {
            return "";
        }

        final StringBuilder result = new StringBuilder();

        for (int i = 0; i < payloads.size(); i++) {
            final InvoiceEmailPayload payload = payloads.get(i);

            if (i % 2 == 0) {
                result.append("<tr>").append("\n");
            } else {
                result.append("<tr style=" + "\"background-color: #E7ECFF;\"" + ">").append("\n");
            }

            result.append("<td>").append(payload.getInvoiceNo()).append("</td>").append("\n");
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
