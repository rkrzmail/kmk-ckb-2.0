package com.kmkbe.core.domain.model;

import com.kmkbe.core.domain.entity.EmailTemplate;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class BouwheerPaymentEmailPayload {
    private String bouwheerName;
    private String vendorName;
    private String vendorCode;
    private String accountNo;
    private String bankAccount;
    private String bankName;
    private String bankKey;
    private String tglPengajuan;
    private List<InvoiceEmailPayload> invoices;

    public String bodyMail(EmailTemplate template) {
        return template.getBodyMail()
                .replace("{bouwheerName}", bouwheerName)
                .replace("{vendorName}", vendorName)
                .replace("{vendorCode}", vendorCode)
                .replace("{accountNo}", accountNo)
                .replace("{bankAccount}", bankAccount)
                .replace("{bankName}", bankName)
                .replace("{bankKey}", bankKey)
                .replace("{tglPengajuan}", tglPengajuan);
    }
}
