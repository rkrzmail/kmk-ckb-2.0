package com.kmkbe.core.domain.constant;

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
