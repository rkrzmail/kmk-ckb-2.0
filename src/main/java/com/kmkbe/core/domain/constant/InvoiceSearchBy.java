package com.kmkbe.core.domain.constant;

public enum InvoiceSearchBy {
    NoInvoice,
    PemberiKerja,
    Item,
    TanggalInvoice,
    JatuhTempoInvoice,
    JumlahTagihan;

    @Override
    public String toString() {
        return switch (this) {
            case NoInvoice -> "No Invoice";
            case PemberiKerja -> "Pemberi Kerja";
            case Item -> "Item";
            case TanggalInvoice -> "Tanggal Invoice";
            case JatuhTempoInvoice -> "Jatuh Tempo Invoice";
            case JumlahTagihan -> "Jumlah Tagihan";
        };
    }
}
