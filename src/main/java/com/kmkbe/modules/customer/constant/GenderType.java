package com.kmkbe.modules.customer.constant;

public enum GenderType {
    LAKI_LAKI,
    PEREMPUAN;

    @Override
    public String toString() {
        return switch (this) {
            case LAKI_LAKI -> "Laki - Laki";
            case PEREMPUAN -> "Perempuan";
        };
    }
}
