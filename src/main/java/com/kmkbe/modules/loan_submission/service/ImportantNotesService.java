package com.kmkbe.modules.loan_submission.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.kmkbe.core.domain.dto.ExternalIntegrationLoanSimulationDto;
import com.kmkbe.core.domain.dto.ImportantNotesDto;
import com.kmkbe.core.domain.dto.InquiryVendorRemoteDto;
import com.kmkbe.core.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class ImportantNotesService {
    private final JdbcTemplate jdbcTemplate;

    public ImportantNotesService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public ImportantNotesDto importantNotesDto() {
        return ImportantNotesDto.builder()
                .description("<p>Data yang tersebut di bawah ini terkait Legalitas, Keuangan & informasi transaksi Perusahaan Bapak/Ibu. Perusahaan dan/atau Perseorangan dalam E-procurement dan POST (Purchase Order System Tracking) PT Trakindo Utama (TU), tidak berkeberatan dan memberikan persetujuannya untuk membagikan setiap dan seluruh data legalitas, keuangan, dan informasi transaksi kepada PT Chandra Sakti Utama Leasing (CSULFinance) sebagai anak usaha Grup Tiara Marga Trakindo (TMT) yang memfasilitasi Pembiayaan Tagihan antara vendor dengan TU. Setiap dan seluruh data/informasi tersebut hanya akan dipakai untuk kebutuhan transaksi Pembiayaan Anjak Piutang/Tagihan di CSULFinance.<br>CSULfinance berizin dan diawasi oleh Otoritas Jasa Keuangan (OJK)</p>")
                .legals(List.of(
                        "Akta Pendirian",
                        "Akta Penyesuaian Anggaran Dasar terhadap UU 40/2007 (Jika PT)",
                        "Akta Perubahan mengenai Modal Ditempatkan dan Disetor",
                        "Akta Perubahan Maksud dan Tujuan Persero",
                        "Akta Perubahan Terakhir mengenai Perubahan Susunan Pengurus Perseroan",
                        "Akta-Akta Perubahan Terakhir Lainnya + SK Persetujuan Menkumhan / Surat Penerimaan Pemberitahuan Perubahan Anggaran Dasar / Data Perseroan (Jika ada)",
                        "Identitas Pengurus (KTP/Paspor/KITAS)",
                        "NPWP",
                        "NIB (RBA)",
                        "Izin Usaha Lainnya",

                        "Company Profile",
                        "Rekap Invoice Tagihan Trakindo",
                        "Rekening Koran",
                        "PO dari Trakindo",
                        "FAP (Formulir Aplikasi Pembiayaan)",
                        "Laporan Keuangan",
                        "Foto Gedung",
                        "Pengalaman Kerja",
                        "Struktur Organisasi",
                        "Bank Detail"
                ))
                .build();
    }
    public ImportantNotesDto importantNotesDto_() {
        return ImportantNotesDto.builder()
                .description("<p>Semua <b>data legalitas, keuangan, & informasi transaksi</b> perusahaan bapak/ibu dalam E-procurement dan POST (Purchase Order System Tracking) PT. Trakindo Utama akan diberikan secara otomatis kepada PT. Chandra Sakti Utama Leasing (CSULfinance) sebagai anak usaha Grup TMT (Tiara Marga Trakindo) yang memfasilitasi pembiayaan tagihan antara vendor dengan PT.Trakindo Utama. Semua data informasi ini dipakai hanya untuk transaksi pembiayaan anjak piutang / tagihan di CSULfinance. \n" +
                        "CSULfinance berizin dan diawasi oleh Otoritas Jasa Keuangan (OJK)</p>")
                .legals(List.of(
                        "Akta Pendirian",
                        "Akta Penyesuaian Anggaran Dasar terhadap UU 40/2007 (Jika PT)",
                        "Akta Perubahan mengenai Modal Ditempatkan dan Disetor",
                        "Akta Perubahan Maksud dan Tujuan Persero",
                        "Akta Perubahan Terakhir mengenai Perubahan Susunan Pengurus Perseroan",
                        "Akta-Akta Perubahan Terakhir Lainnya + SK Persetujuan Menkumhan / Surat Penerimaan Pemberitahuan Perubahan Anggaran Dasar / Data Perseroan (Jika ada)",
                        "Identitas Pengurus (KTP/Paspor/KITAS)",
                        "NPWP",
                        "NIB (RBA)",
                        "Izin Usaha Lainnya",
                        "Izin Lokasi",
                        "Company Profile",
                        "Rekap Invoice Tagihan Trakindo",
                        "Rekening Koran",
                        "PO dari Trakindo",
                        "FAP (Formulir Aplikasi Pembiayaan)",
                        "Laporan Keuangan",
                        "Foto Gedung",
                        "Pengalaman Kerja",
                        "Struktur Organisasi",
                        "Bank Detail"
                ))
                .build();
    }

    public ExternalIntegrationLoanSimulationDto create(
            InquiryVendorRemoteDto inquiryVendorRemote,
            String bouwheerCode,
            String vendorCode
    ) throws JsonProcessingException {
        try {
            Optional<ExternalIntegrationLoanSimulationDto> find = findExternalIntegrationByBouwheerCode(
                    bouwheerCode,
                    vendorCode
            );

            final ExternalIntegrationLoanSimulationDto result;
            if (find.isEmpty()) {
                jdbcTemplate.update(
                        "insert into _loan_important_notes (vendor_code, bouwheer_code, already_accept_important_notes, dtm_crt) values (?, ?, ?, ?)",
                        vendorCode,
                        bouwheerCode,
                        true,
                        new Date()
                );

                result = findExternalIntegrationByBouwheerCode(bouwheerCode, vendorCode)
                        .get();
            } else {
                result = find.get();
            }


            result.setVendor(
                    Base64
                            .getUrlEncoder()
                            .encodeToString(
                                    ObjectUtils
                                            .jsonToStr(generateVendor(inquiryVendorRemote, vendorCode, "Perusahaan"))
                                            .getBytes(StandardCharsets.UTF_8)
                            )
            );

            return result;
        } catch (Exception e) {
            log.error("create, error {}", e.getMessage());
            throw e;
        }
    }

    public ExternalIntegrationLoanSimulationDto.VendorDto generateVendor(
            InquiryVendorRemoteDto inquiryVendorRemote,
            String vendorCode,
            String type
    ) {
        return ExternalIntegrationLoanSimulationDto.VendorDto.builder()
                .vendorCode(vendorCode)
                .name(inquiryVendorRemote.getVendorName())
                .customerType(type)
                .email(inquiryVendorRemote.getEmail())
                .mobilePhone(inquiryVendorRemote.getPhone())
                .customerIdNo(inquiryVendorRemote.getNpwp())
                .build();
    }

    public Optional<ExternalIntegrationLoanSimulationDto> findExternalIntegrationByBouwheerCode(String bouwheerCode, String vendorCode) {
        try {
            ExternalIntegrationLoanSimulationDto result = jdbcTemplate.queryForObject(
                    "select bouwheer_code, vendor_code, already_accept_important_notes, dtm_crt from public._loan_important_notes where bouwheer_code = ? and vendor_code = ? order by id desc limit 1",
                    (rs, rowNum) -> ExternalIntegrationLoanSimulationDto.builder()
                            .bouwheerCode(rs.getString("bouwheer_code"))
                            .alreadyAcceptImportantNotes(rs.getBoolean("already_accept_important_notes"))
                            .dtmCrt(rs.getTimestamp("dtm_crt"))
                            .build(),
                    bouwheerCode,
                    vendorCode
            );
            return Optional.ofNullable(result);
        } catch (EmptyResultDataAccessException emptyResultDataAccessException) {
            return Optional.empty();
        } catch (Exception e) {
            log.error("findExternalIntegrationByBouwheerCode, error {}", e.getMessage());
            throw e;
        }
    }
}
