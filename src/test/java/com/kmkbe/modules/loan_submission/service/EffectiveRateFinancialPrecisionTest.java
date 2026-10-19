package com.kmkbe.modules.loan_submission.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EffectiveRateFinancialPrecisionTest {

    @Test
    @DisplayName("H11: normalizeEffectiveRate retains decimal precision for percentage rates (no Math.floor truncation)")
    void retainsDecimalPrecisionStandardRates() {
        // Kasus persentase standar dengan pecahan desimal: 14.75%
        Double rate = 14.75;
        Double normalized = LoanSubmissionService.normalizeEffectiveRate(rate);
        assertThat(normalized).isEqualTo(14.75);

        // Kasus 12.3456%
        Double ratePrecise = 12.3456;
        Double normalizedPrecise = LoanSubmissionService.normalizeEffectiveRate(ratePrecise);
        assertThat(normalizedPrecise).isEqualTo(12.3456);
    }

    @Test
    @DisplayName("H11: normalizeEffectiveRate divides basis points (> 100) retaining decimals instead of Math.floor")
    void dividesBasisPointsWithoutTruncation() {
        // Misal basis points 1475 bps (sebelumnya Math.floor(1475 / 100) memotong 14.75 menjadi 14.0)
        Double bps = 1475.0;
        Double normalized = LoanSubmissionService.normalizeEffectiveRate(bps);
        assertThat(normalized).isEqualTo(14.75);

        // Kasus 1850 bps -> 18.5%
        Double bpsHalf = 1850.0;
        assertThat(LoanSubmissionService.normalizeEffectiveRate(bpsHalf)).isEqualTo(18.5);

        // Kasus 1234.5 bps -> 12.345%
        Double bpsFraction = 1234.5;
        assertThat(LoanSubmissionService.normalizeEffectiveRate(bpsFraction)).isEqualTo(12.345);
    }

    @Test
    @DisplayName("H11: normalizeEffectiveRate scales decimal representation (< 1) to percentage accurately")
    void scalesDecimalRepresentationCorrectly() {
        // Misal 0.1475 -> 14.75%
        Double decimalRate = 0.1475;
        Double normalized = LoanSubmissionService.normalizeEffectiveRate(decimalRate);
        assertThat(normalized).isEqualTo(14.75);

        // 0.0825 -> 8.25%
        Double lowDecimal = 0.0825;
        assertThat(LoanSubmissionService.normalizeEffectiveRate(lowDecimal)).isEqualTo(8.25);
    }

    @Test
    @DisplayName("H11: normalizeEffectiveRate handles null and zero safely")
    void handlesNullAndZeroGracefully() {
        assertThat(LoanSubmissionService.normalizeEffectiveRate(null)).isNull();
        assertThat(LoanSubmissionService.normalizeEffectiveRate(0.0)).isEqualTo(0.0);
    }
}
