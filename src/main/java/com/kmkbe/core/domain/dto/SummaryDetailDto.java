package com.kmkbe.core.domain.dto;

import ch.qos.logback.classic.spi.LoggerContextAware;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@Getter
@Setter
@Builder
@NoArgsConstructor
public class SummaryDetailDto implements Serializable {
    private String debtorName;
    private String npwp;
    private String debtorStatus;
    private String bouwheerName;
    private String aoName;
    private String branch;
    private String cwrCode;
    private String agreementCode;
    private long utilizationSeqNoCount;
    private double persenPencairan;
    private double jumlahPlafonAmount;
    private double totalUtilizationAmount;
    private double sisaPlafon;
    private double adminPencairanFee;
    private double factoringFee;
    private double utilizationDate;
    private LocalDateTime disburseDate;
    private String danaSaktiStatus;
    private LocalDateTime invoiceDueDate;
    private LocalDateTime tanggalAktivasi;
    private LocalDateTime tanggalPengajuan;
    private LocalDateTime goliveDate;

    public SummaryDetailDto(String debtorName, String npwp, String debtorStatus, String bouwheerName, String aoName,
                            String branch, String cwrCode, String agreementCode, long utilizationSeqNoCount, double persenPencairan,
                            double jumlahPlafonAmount, double totalUtilizationAmount, double sisaPlafon,
                            double adminPencairanFee, double factoringFee, double utilizationDate,
                            LocalDateTime disburseDate, String danaSaktiStatus, LocalDateTime invoiceDueDate,
                            LocalDateTime tanggalAktivasi, LocalDateTime tanggalPengajuan, LocalDateTime goliveDate
    ) {
        this.debtorName = debtorName;
        this.npwp = npwp;
        this.debtorStatus = debtorStatus;
        this.bouwheerName = bouwheerName;
        this.aoName = aoName;
        this.branch = branch;
        this.cwrCode = cwrCode;
        this.agreementCode = agreementCode;
        this.utilizationSeqNoCount = utilizationSeqNoCount;
        this.persenPencairan = persenPencairan;
        this.jumlahPlafonAmount = jumlahPlafonAmount;
        this.totalUtilizationAmount = totalUtilizationAmount;
        this.sisaPlafon = sisaPlafon;
        this.adminPencairanFee = adminPencairanFee;
        this.factoringFee = factoringFee;
        this.utilizationDate = utilizationDate;
        this.disburseDate = disburseDate;
        this.danaSaktiStatus = danaSaktiStatus;
        this.invoiceDueDate = invoiceDueDate;
        this.tanggalAktivasi = tanggalAktivasi;
        this.tanggalPengajuan = tanggalPengajuan;
        this.goliveDate = goliveDate;
    }
}
