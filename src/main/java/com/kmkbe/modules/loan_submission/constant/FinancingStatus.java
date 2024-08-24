package com.kmkbe.modules.loan_submission.constant;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

public enum FinancingStatus {
    NEW("NEW"),
    IN_PROCESS("INPROCESS"),
    ASSIGNMENT("ASSIGNMENT"),
    SIGNING("SIGNING"),
    SIGNED("SIGNED"),
    LIVE("LIVE"),
    GO_LIVE("GOLIVE"),
    PAID("PAID"),
    COMPLETED("COMPLETED"),
    REFUND("REFUND");

    @JsonValue
    @Getter
    private final String value;

    FinancingStatus(String value) {
        this.value = value;
    }
}
