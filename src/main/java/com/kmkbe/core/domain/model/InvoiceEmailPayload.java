package com.kmkbe.core.domain.model;

import lombok.Builder;
import lombok.Getter;
import org.apache.commons.lang3.StringEscapeUtils;

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

    public static String esc(String s){
        return StringEscapeUtils.escapeEcmaScript(s);
    }
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
                result.append("<tr style=\"background-color: #E7ECFF;\">").append("\n");
            }

            result.append("<td>").append(esc(payload.getInvoiceNo())).append("</td>").append("\n");
            result.append("<td>").append(esc(payload.getDescription())).append("</td>").append("\n");
            result.append("<td>").append(esc(payload.getBouwheerName())).append("</td>").append("\n");
            result.append("<td>").append(esc(payload.getInvoiceDate())).append("</td>").append("\n");
            result.append("<td>").append(esc(payload.getInvoiceDueDate())).append("</td>").append("\n");
            result.append("<td>").append(esc(payload.getInvoiceAmt())).append("</td>").append("\n");
            result.append("</tr>").append("\n");
        }
        return result.toString();
    }
}
