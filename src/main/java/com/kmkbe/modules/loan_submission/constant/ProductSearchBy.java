package com.kmkbe.modules.loan_submission.constant;

public enum ProductSearchBy {
    Kota;

    @Override
    public String toString() {
        return switch (this) {
            case Kota -> "Kota";
        };
    }
}
