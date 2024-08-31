package com.kmkbe.core.domain.model;

import com.kmkbe.core.domain.entity.FinancingHdr;
import lombok.Getter;
import lombok.Setter;

public class MappedFinancingStatus {
    @Getter
    private String status;
    @Getter
    private String label;

    @Setter
    private FinancingHdr financingHdr;

    @Setter
    private Type type;

    public MappedFinancingStatus(FinancingHdr financingHdr, Type type) {
        this.financingHdr = financingHdr;
        this.type = type;

        switch (type) {
            case Customer:
                mappedCustomer();
                break;
            case BranchAdmin:
                mappedBranchAdmin();
                break;
            case MajorAccount:
                mappedMajorAccount();
                break;
        }
    }

    private void mappedBranchAdmin() {
        if (financingHdr.getFinancingStatus().equalsIgnoreCase("inprocess")) {
            if (financingHdr.getFinancingStep().equalsIgnoreCase("ASSIGNMENT")) {
                label = "Baru";
                status = "NEW";
            } else if (financingHdr.getFinancingStep().equalsIgnoreCase("INPROCESS")) {
                label = "Preparation";
                status = "PREPARATION";
            } else if (financingHdr.getFinancingStep().equalsIgnoreCase("SIGNING")) {
                label = "Signing";
                status = "SIGNING";
            } else if (financingHdr.getFinancingStep().equalsIgnoreCase("SIGNED")) {
                label = "Signed";
                status = "SIGNED";
            }
        } else if (financingHdr.getFinancingStatus().equalsIgnoreCase("live")) {
            if (financingHdr.getFinancingStep().equalsIgnoreCase("GOLIVE")) {
                label = "Live";
                status = "LIVE";
            } else if (financingHdr.getFinancingStep().equalsIgnoreCase("PAID")) {
                label = "Paid";
                status = "PAID";
            }
        } else if (financingHdr.getFinancingStatus().equalsIgnoreCase("COMPLETED")) {
            if (financingHdr.getFinancingStep().equalsIgnoreCase("REFUND")) {
                label = "Completed";
                status = "COMPLETED";
            }
        } else {
            label = "";
            status = "";
        }
    }

    private void mappedCustomer() {

    }

    private void mappedMajorAccount() {

    }

    public enum Type {
        Customer,
        MajorAccount,
        BranchAdmin
    }
}
