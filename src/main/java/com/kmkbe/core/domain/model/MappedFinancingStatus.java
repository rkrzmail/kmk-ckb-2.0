package com.kmkbe.core.domain.model;

import com.kmkbe.core.domain.constant.FinancingStatus;
import com.kmkbe.core.domain.entity.FinancingHdr;
import com.kmkbe.helpers.constant.StatusConstants;
import lombok.*;

public class MappedFinancingStatus {
  @Getter
  private String status;
  @Getter
  private String label;

  @Setter
  private FinancingHdr financingHdr;

  public MappedFinancingStatus(FinancingHdr financingHdr, Type type) {
    label = "";
    status = "";

    this.financingHdr = financingHdr;
    if (StatusConstants.FINANCING_STATUS_PAID.equalsIgnoreCase(financingHdr.getFinancingStatus())
      && StatusConstants.FINANCING_STEP_PAID.equalsIgnoreCase(financingHdr.getFinancingStep())) {
      label = "Paid";
      status = String.valueOf(FinancingStatus.PAID);
      return;
    }
    if (StatusConstants.FINANCING_STATUS_PAID.equalsIgnoreCase(financingHdr.getFinancingStatus())
      && StatusConstants.FINANCING_STEP_SIGNED.equalsIgnoreCase(financingHdr.getFinancingStep())) {
      label = "Signed";
      status = String.valueOf(FinancingStatus.SIGNED);
      return;
    }
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
      case AccountOfficer:
        mappedAccountOfficer();
        break;
    }
  }

  private void mappedAccountOfficer() {
    if (financingHdr.getFinancingStatus().equalsIgnoreCase("inprocess")) {
      if (financingHdr.getFinancingStep().equalsIgnoreCase(StatusConstants.FINANCING_STEP_ASSIGNMENT)) {
        label = "Baru";
        status = String.valueOf(FinancingStatus.NEW);
      } else if (financingHdr.getFinancingStep().equalsIgnoreCase(StatusConstants.FINANCING_STEP_INPROCESS)) {
        label = "Preparation";
        status = String.valueOf(FinancingStatus.PREPARATION);
      } else if (financingHdr.getFinancingStep().equalsIgnoreCase(StatusConstants.FINANCING_STEP_SIGNING)) {
        label = "Signing";
        status = String.valueOf(FinancingStatus.SIGNING);
      } else if (financingHdr.getFinancingStep().equalsIgnoreCase(StatusConstants.FINANCING_STEP_SIGNED)) {
        label = "Signed";
        status = String.valueOf(FinancingStatus.SIGNED);
      }
    } else if (financingHdr.getFinancingStatus().equalsIgnoreCase("live")) {
      if (financingHdr.getFinancingStep().equalsIgnoreCase(StatusConstants.FINANCING_STEP_GOLIVE)) {
        label = "Live";
        status = String.valueOf(FinancingStatus.LIVE);
      } else if (financingHdr.getFinancingStep().equalsIgnoreCase(StatusConstants.FINANCING_STEP_PAID)) {
        label = "Paid";
        status = String.valueOf(FinancingStatus.PAID);
      }
    } else {
      label = "Untracked";
      status = "UNTRACKED";
    }
  }

  private void mappedBranchAdmin() {
    if (financingHdr.getFinancingStatus().equalsIgnoreCase("inprocess")) {
      if (financingHdr.getFinancingStep().equalsIgnoreCase(StatusConstants.FINANCING_STEP_ASSIGNMENT)) {
        label = "Baru";
        status = String.valueOf(FinancingStatus.NEW);
      } else if (financingHdr.getFinancingStep().equalsIgnoreCase(StatusConstants.FINANCING_STEP_INPROCESS)) {
        label = "Preparation";
        status = String.valueOf(FinancingStatus.PREPARATION);
      } else if (financingHdr.getFinancingStep().equalsIgnoreCase(StatusConstants.FINANCING_STEP_SIGNING)) {
        label = "Signing";
        status = String.valueOf(FinancingStatus.SIGNING);
      } else if (financingHdr.getFinancingStep().equalsIgnoreCase(StatusConstants.FINANCING_STEP_SIGNED)) {
        label = "Signed";
        status = String.valueOf(FinancingStatus.SIGNED);
      }
    } else if (financingHdr.getFinancingStatus().equalsIgnoreCase("live")) {
      if (financingHdr.getFinancingStep().equalsIgnoreCase(StatusConstants.FINANCING_STEP_GOLIVE)) {
        label = "Live";
        status = String.valueOf(FinancingStatus.LIVE);
      } else if (financingHdr.getFinancingStep().equalsIgnoreCase(StatusConstants.FINANCING_STEP_PAID)) {
        label = "Paid";
        status = String.valueOf(FinancingStatus.PAID);
      }
    } else {
      label = "Untracked";
      status = "UNTRACKED";
    }
  }

  private void mappedCustomer() {
    if (financingHdr.getFinancingStatus().equalsIgnoreCase("new")) {
      label = "New";
      status = String.valueOf(FinancingStatus.NEW);
    } else if (financingHdr.getFinancingStatus().equalsIgnoreCase("inprocess")) {
      if (
        financingHdr.getFinancingStep().equalsIgnoreCase(StatusConstants.FINANCING_STEP_ASSIGNMENT)
          || financingHdr.getFinancingStep().equalsIgnoreCase(StatusConstants.FINANCING_STEP_INPROCESS)
          || financingHdr.getFinancingStep().equalsIgnoreCase(StatusConstants.FINANCING_STEP_SIGNING)
          || financingHdr.getFinancingStep().equalsIgnoreCase(StatusConstants.FINANCING_STEP_SIGNED)
      ) {
        label = "In proses";
        status = String.valueOf(FinancingStatus.IN_PROCESS);
      }
    } else if (financingHdr.getFinancingStatus().equalsIgnoreCase("live")) {
      label = "Live";
      status = String.valueOf(FinancingStatus.LIVE);
    } else {
      label = "Untracked";
      status = String.valueOf(FinancingStatus.UNTRACKED);
    }
  }

  private void mappedMajorAccount() {
    if (financingHdr.getFinancingStatus().equalsIgnoreCase("new")) {
      label = "New";
      status = String.valueOf(FinancingStatus.NEW);
    } else if (financingHdr.getFinancingStatus().equalsIgnoreCase("inprocess")) {
      if (financingHdr.getFinancingStep().equalsIgnoreCase(StatusConstants.FINANCING_STEP_ASSIGNMENT)) {
        label = "Assignment";
        status = String.valueOf(FinancingStatus.ASSIGNMENT);
      } else if (financingHdr.getFinancingStep().equalsIgnoreCase(StatusConstants.FINANCING_STEP_INPROCESS)) {
        label = "In Process";
        status = String.valueOf(FinancingStatus.IN_PROCESS);
      } else if (
        financingHdr.getFinancingStep().equalsIgnoreCase(StatusConstants.FINANCING_STEP_SIGNING)
          || financingHdr.getFinancingStep().equalsIgnoreCase(StatusConstants.FINANCING_STEP_SIGNED)
      ) {
        label = "Signing";
        status = String.valueOf(FinancingStatus.SIGNING);
      }
    } else if (financingHdr.getFinancingStatus().equalsIgnoreCase("live")) {
      if (financingHdr.getFinancingStep().equalsIgnoreCase(StatusConstants.FINANCING_STEP_GOLIVE)) {
        label = "Live";
        status = String.valueOf(FinancingStatus.LIVE);
      } else if (financingHdr.getFinancingStep().equalsIgnoreCase(StatusConstants.FINANCING_STEP_PAID)) {
        label = "Paid";
        status = String.valueOf(FinancingStatus.PAID);
      }
    } else {
      label = "Untracked";
      status = String.valueOf(FinancingStatus.UNTRACKED);
    }
  }

  private void mappedDisbursement() {
    if (financingHdr.getFinancingStatus().equalsIgnoreCase("inprocess")) {
      if (financingHdr.getFinancingStep().equalsIgnoreCase(StatusConstants.FINANCING_STEP_SIGNED)) {
        label = "New";
        status = String.valueOf(FinancingStatus.NEW);
      }
    } else if (financingHdr.getFinancingStatus().equalsIgnoreCase("live")) {
      if (financingHdr.getFinancingStep().equalsIgnoreCase(StatusConstants.FINANCING_STEP_GOLIVE)) {
        label = "Disbursed";
        status = "DISBURSED";
      } else if (financingHdr.getFinancingStep().equalsIgnoreCase(StatusConstants.FINANCING_STEP_PAID)) {
        label = "Paid";
        status = String.valueOf(FinancingStatus.PAID);
      }
    } else {
      label = "Untracked";
      status = String.valueOf(FinancingStatus.UNTRACKED);
    }
  }

  private void mappedRepayment() {
    if (financingHdr.getFinancingStatus().equalsIgnoreCase("inprocess")) {
      if (financingHdr.getFinancingStep().equalsIgnoreCase(StatusConstants.FINANCING_STEP_SIGNED)) {
        label = "Unpaid";
        status = String.valueOf(FinancingStatus.UNPAID);
      }
    } else if (financingHdr.getFinancingStatus().equalsIgnoreCase("live")) {
      if (financingHdr.getFinancingStep().equalsIgnoreCase(StatusConstants.FINANCING_STEP_GOLIVE)) {
        label = "Unpaid";
        status = String.valueOf(FinancingStatus.UNPAID);
      } else if (financingHdr.getFinancingStep().equalsIgnoreCase(StatusConstants.FINANCING_STEP_PAID)) {
        label = "Paid";
        status = String.valueOf(FinancingStatus.PAID);
      }
    } else {
      label = "Untracked";
      status = String.valueOf(FinancingStatus.UNTRACKED);
    }
  }

  public enum Type {
    Customer,
    MajorAccount,
    BranchAdmin,
    Disbursement,
    Repayment,
    AccountOfficer
  }
}
