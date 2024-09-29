package com.kmkbe.core.domain.model;

import com.kmkbe.core.domain.entity.FinancingHdr;
import lombok.*;

public class MappedFinancingStatus {
    @Getter
    private String status;
    @Getter
    private String label;

    @Setter
    private FinancingHdr financingHdr;

    public MappedFinancingStatus(FinancingHdr financingHdr, Type type) {
        this.financingHdr = financingHdr;
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
            case Disbursement:
                mappedDisbursement();
                break;
            case Repayment:
                mappedRepayment();
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
            label = "Untracked";
            status = "UNTRACKED";
        }
    }

    private void mappedCustomer() {
        if (financingHdr.getFinancingStatus().equalsIgnoreCase("new")) {
            label = "New";
            status = "NEW";
        } else if (financingHdr.getFinancingStatus().equalsIgnoreCase("inprocess")) {
            if (
                    financingHdr.getFinancingStep().equalsIgnoreCase("ASSIGNMENT")
                            || financingHdr.getFinancingStep().equalsIgnoreCase("INPROCESS")
                            || financingHdr.getFinancingStep().equalsIgnoreCase("SIGNING")
                            || financingHdr.getFinancingStep().equalsIgnoreCase("SIGNED")
            ) {
                label = "In proses";
                status = "INPROCESS";
            }
        } else if (financingHdr.getFinancingStatus().equalsIgnoreCase("live")) {
            label = "Live";
            status = "LIVE";
        } else if (financingHdr.getFinancingStatus().equalsIgnoreCase("COMPLETED")) {
            label = "Completed";
            status = "COMPLETED";
        } else {
            label = "Untracked";
            status = "UNTRACKED";
        }
    }

    private void mappedMajorAccount() {
        if (financingHdr.getFinancingStatus().equalsIgnoreCase("new")) {
            label = "New";
            status = "NEW";
        } else if (financingHdr.getFinancingStatus().equalsIgnoreCase("inprocess")) {
            if (financingHdr.getFinancingStep().equalsIgnoreCase("ASSIGNMENT")) {
                label = "Assignment";
                status = "ASSIGNMENT";
            } else if (financingHdr.getFinancingStep().equalsIgnoreCase("INPROCESS")) {
                label = "In Process";
                status = "INPROCESS";
            } else if (
                    financingHdr.getFinancingStep().equalsIgnoreCase("SIGNING")
                            || financingHdr.getFinancingStep().equalsIgnoreCase("SIGNED")
            ) {
                label = "Signing";
                status = "SIGNING";
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
            label = "Untracked";
            status = "UNTRACKED";
        }
    }

    private void mappedDisbursement() {
        if (financingHdr.getFinancingStatus().equalsIgnoreCase("inprocess")) {
            if (financingHdr.getFinancingStep().equalsIgnoreCase("SIGNED")) {
                label = "New";
                status = "NEW";
            }
        } else if (financingHdr.getFinancingStatus().equalsIgnoreCase("live")) {
            if (financingHdr.getFinancingStep().equalsIgnoreCase("GOLIVE")) {
                label = "Disbursed";
                status = "DISBURSED";
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
            label = "Untracked";
            status = "UNTRACKED";
        }
    }

    private void mappedRepayment() {
        if (financingHdr.getFinancingStatus().equalsIgnoreCase("inprocess")) {
            if (financingHdr.getFinancingStep().equalsIgnoreCase("SIGNED")) {
                label = "Unpaid";
                status = "UNPAID";
            }
        } else if (financingHdr.getFinancingStatus().equalsIgnoreCase("live")) {
            if (financingHdr.getFinancingStep().equalsIgnoreCase("GOLIVE")) {
                label = "Unpaid";
                status = "UNPAID";
            } else if (financingHdr.getFinancingStep().equalsIgnoreCase("PAID")) {
                label = "Paid";
                status = "PAID";
            }
        } else if (financingHdr.getFinancingStatus().equalsIgnoreCase("COMPLETED")) {
            if (financingHdr.getFinancingStep().equalsIgnoreCase("REFUND")) {
                label = "Paid";
                status = "PAID";
            }
        } else {
            label = "Untracked";
            status = "UNTRACKED";
        }
    }

    public enum Type {
        Customer,
        MajorAccount,
        BranchAdmin,
        Disbursement,
        Repayment
    }
}
