package com.kmkbe.core.domain.constant;

public enum ProductSearchBy {
    Kota;

    @Override
    public String toString() {
        return switch (this) {
            case Kota -> "Kota";
        };
    }
}
