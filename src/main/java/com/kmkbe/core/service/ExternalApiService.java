package com.kmkbe.core.service;

import com.kmkbe.core.domain.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.kmkbe.feign.client.ConfinsR3FeignClient;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExternalApiService {
    private final RestTemplate restTemplate;

    private final ConfinsR3FeignClient confinsR3FeignClient;

    private String xapiKey = "YiByHB@CSUL_DEV";

    public LocalDateTime getAgreementDate(String agreementCode) {
        if (agreementCode == null || agreementCode.isBlank() || "-".equals(agreementCode)) {
            throw new IllegalArgumentException("Nomor perjanjian untuk tanggal pengalihan piutang tidak tersedia");
        }
        InquiryDataAgreementDto response = confinsR3FeignClient.getAgreementByAgrmntNo(Map.of(
            "TrxNo", agreementCode,
            "RequestDateTime", LocalDate.now().toString()
        ));
        if (response == null || !"200".equals(response.getStatusCode())
            || response.getAgrmntObj() == null || response.getAgrmntObj().getAgrmntDt() == null) {
            throw new IllegalStateException("AgrmntDt Confins untuk perjanjian " + agreementCode + " tidak tersedia");
        }
        return response.getAgrmntObj().getAgrmntDt();
    }

    public AppResponse getAppByAppNo(String applicationCode) {
        try {
            AppRequest request = new AppRequest();
            request.setTrxNo(applicationCode);
            request.setRequestDateTime(LocalDate.now().toString());

            AppResponse response = confinsR3FeignClient.getAppByAppNo(request);
            if (response == null || response.getHeaderObj() == null) {
                throw new RuntimeException("Invalid API response structure");
            }

            return response;

        } catch (Exception e) {
            throw new RuntimeException("Failed to call Confins API: " + e.getMessage());
        }
    }

    public FinancialDataResponse getFinancialData(String agreementCode) {
        try {
            FinancialDataRequest request = new FinancialDataRequest();
            request.setTrxNo(agreementCode);
            request.setRequestDateTime(LocalDate.now().toString());

            FinancialDataResponse response = confinsR3FeignClient.getFinancialData(request);

            if (response == null ||
                    response.getHeader() == null ||
                    !"200".equals(response.getHeader().getStatusCode())) {
                throw new RuntimeException("Invalid financial data response");
            }

            return response;

        } catch (Exception e) {
            throw new RuntimeException("Failed to get financial data: " + e.getMessage());
        }
    }

    public AppFactoringResponse getAppFactoringData(Integer appId) {
        try {
            AppFactoringRequest request = new AppFactoringRequest();
            request.setId(appId);
            request.setRequestDateTime(LocalDate.now().toString());

            AppFactoringResponse response = confinsR3FeignClient.getAppFactoringData(request);

            if (response == null ||
                    response.getHeader() == null ||
                    !"200".equals(response.getHeader().getStatusCode())) {
                throw new RuntimeException("Invalid response from AppFctr API");
            }

            return response;

        } catch (Exception e) {
            throw new RuntimeException("Failed to get AppFctr data for AppId: " + appId, e);
        }
    }

    public RekDebiturResponse getRekDebitur(String applicationCode) {
        try {
            RekDebiturRequest request = new RekDebiturRequest();
            request.setTrxNo(applicationCode);
            request.setRequestDateTime(LocalDate.now().toString());

            RekDebiturResponse response = confinsR3FeignClient.getRekDebitur(request);
            if (response == null || response.getHeader() == null) {
                throw new RuntimeException("Invalid API response structure");
            }

            return response;

        } catch (Exception e) {
            throw new RuntimeException("Failed to call Confins API: " + e.getMessage());
        }
    }

    public DisbursementAccountResponse.DisbInfo getDebtorDisbursementAccount(String agreementCode) {
        DisbursementAccountResponse response = confinsR3FeignClient.getDisbursementAccounts(
            new DisbursementAccountRequest(agreementCode, LocalDate.now().toString()));
        if (response == null || response.getHeader() == null
            || !"200".equals(response.getHeader().getStatusCode())
            || !"200".equals(response.getStatusCode()) || response.getListDisbInfo() == null) {
            throw new IllegalStateException("Respons rekening pencairan Confins tidak valid untuk agreement " + agreementCode);
        }
        var debtorAccounts = response.getListDisbInfo().stream()
            .filter(info -> "DEBTOR".equalsIgnoreCase(info.getDisbTo()))
            .filter(info -> info.getBankName() != null && !info.getBankName().isBlank()
                && info.getAccNo() != null && !info.getAccNo().isBlank()
                && info.getNominal() != null)
            .toList();
        if (debtorAccounts.size() != 1) {
            throw new IllegalStateException("Diperlukan tepat satu rekening pencairan DEBTOR untuk agreement "
                + agreementCode + "; ditemukan " + debtorAccounts.size());
        }
        return debtorAccounts.getFirst();
    }

    public LocalDateTime getDisbursementExecutionDate(String apNo, String officeCode) {
        if (apNo == null || apNo.isBlank() || officeCode == null || officeCode.isBlank()) {
            throw new IllegalArgumentException("APNo dan OfficeCode wajib tersedia untuk mencari tanggal pencairan");
        }

        String requestDate = LocalDate.now().toString();
        String apNoArrayExpression = "CASE WHEN AP.ACC_PAYABLE_NO IS NULL THEN '-' ELSE "
            + "(SELECT string_agg(AP2.ACC_PAYABLE_NO, ',') FROM PAY_VOUCHER_H PVH2 "
            + "LEFT JOIN PAY_VOUCHER_D PVD2 ON PVH2.PAY_VOUCHER_H_ID = PVD2.PAY_VOUCHER_H_ID "
            + "JOIN ACC_PAYABLE AP2 ON PVD2.ACC_PAYABLE_ID = AP2.ACC_PAYABLE_ID "
            + "WHERE PVH2.PAY_VOUCHER_H_ID = PVH.PAY_VOUCHER_H_ID GROUP BY PVH2.PAY_VOUCHER_H_ID) END";
        PaymentVoucherRemote.PagingRequest pagingRequest = new PaymentVoucherRemote.PagingRequest(
            true, true, true,
            new PaymentVoucherRemote.QueryString("searchPayVoucherInquiry"),
            "", null, "INNER", 1, 10,
            new PaymentVoucherRemote.OrderBy("PVH.PAY_VOUCHER_H_ID", "false"),
            List.of(
                new PaymentVoucherRemote.Criterion(0, 0, "text", false, "Eq", "AP.REF_ACC_PAYABLE_TYPE_ID", "10009"),
                new PaymentVoucherRemote.Criterion(0, 0, "text", false, "Eq", "AP.OFFICE_CODE", officeCode),
                new PaymentVoucherRemote.Criterion(0, 0, "text", false, "Eq", "PVH.CURR_CODE", "IDR"),
                new PaymentVoucherRemote.Criterion(0, 0, "", false, "Eq", apNoArrayExpression, apNo)
            ), requestDate
        );

        PaymentVoucherRemote.PagingResponse paging = confinsR3FeignClient.getPaymentVouchers(pagingRequest);
        if (paging == null || paging.header() == null || !"200".equals(paging.header().statusCode())
            || !"200".equals(paging.statusCode()) || paging.data() == null) {
            throw new IllegalStateException("Respons pencarian Payment Voucher Confins tidak valid untuk AP " + apNo);
        }
        if (paging.count() != null && paging.count() > paging.data().size()) {
            throw new IllegalStateException("Hasil Payment Voucher melebihi halaman pertama untuk AP " + apNo);
        }
        List<PaymentVoucherRemote.PagingRow> paidVouchers = paging.data().stream()
            .filter(row -> "PAID".equalsIgnoreCase(row.statusCode()))
            .filter(row -> row.payVoucherNo() != null && !row.payVoucherNo().isBlank())
            .filter(row -> apNo.equals(row.apNo()) || row.apNoArray() != null
                && Arrays.stream(row.apNoArray().split(",")).map(String::trim).anyMatch(apNo::equals))
            .toList();
        if (paidVouchers.size() != 1) {
            throw new IllegalStateException("Diperlukan tepat satu Payment Voucher PAID untuk AP " + apNo
                + "; ditemukan " + paidVouchers.size());
        }

        String voucherNo = paidVouchers.getFirst().payVoucherNo();
        PaymentVoucherRemote.DetailResponse detail = confinsR3FeignClient.getPaymentVoucher(
            new PaymentVoucherRemote.DetailRequest(voucherNo, requestDate));
        if (detail == null || !voucherNo.equals(detail.payVoucherNo())
            || !"PAID".equalsIgnoreCase(detail.statusCode()) || detail.executionValueDt() == null) {
            throw new IllegalStateException("ExecutionValueDt Payment Voucher tidak valid untuk " + voucherNo);
        }
        return detail.executionValueDt();
    }

    public CwrBwhrResponse getCwrBwhr(String CwrCode) {
        try {
            CwrBwhrRequest request = new CwrBwhrRequest();
            request.setTrxNo(CwrCode);
            request.setRequestDateTime(LocalDate.now().toString());

            CwrBwhrResponse response = confinsR3FeignClient.getCwrBwhr(request);
            if (response == null || response.getHeader() == null) {
                throw new RuntimeException("Invalid API response structure");
            }

            return response;

        } catch (Exception e) {
            throw new RuntimeException("Failed to call Confins API: " + e.getMessage());
        }
    }

    public CwrListBwhrResponse getListCwrBwhr(String CwrCode, String CwrBouwheerCustNo) {
        try {
            CwrListBwhrRequest request = new CwrListBwhrRequest();
            request.setCwrNo(CwrCode);
            request.setCwrBouwheerCustNo(CwrBouwheerCustNo);
            request.setRequestDateTime(LocalDate.now().toString());

            CwrListBwhrResponse response = confinsR3FeignClient.getListCwrBwhr(request);
            if (response == null || response.getHeader() == null) {
                throw new RuntimeException("Invalid API response structure");
            }

            return response;

        } catch (Exception e) {
            throw new RuntimeException("Failed to call Confins API: " + e.getMessage());
        }
    }

    public SignerApiResponse getKaryawan(String CwrCode, String CustNo) {
        try {
            SignerRequestDto request = new SignerRequestDto();
            request.setCwrNo(CwrCode);
            request.setCustNo(CustNo);
            request.setRequestDateTime(LocalDate.now().toString());

            SignerApiResponse response = confinsR3FeignClient.getKaryawan(request);
            if (response == null || response.getHeader() == null) {
                throw new RuntimeException("Invalid API response structure");
            }

            return response;

        } catch (Exception e) {
            throw new RuntimeException("Failed to call Confins API: " + e.getMessage());
        }
    }

    public ExternalSigningResponse callEsignApi(ExternalSigningRequest request) {
        String esignUrl = "https://gdkwebserver.ad-ins.com/adimobile/demo/esign/services/external/document/sendDocumentSigning";

        HttpHeaders headers = new HttpHeaders();
        headers.set("x-api-key", xapiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<ExternalSigningRequest> httpEntity = new HttpEntity<>(request, headers);

        ResponseEntity<ExternalSigningResponse> response = restTemplate.exchange(
                esignUrl,
                HttpMethod.POST,
                httpEntity,
                ExternalSigningResponse.class
        );

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Failed to call e-sign API: " + response.getStatusCode());
        }

        return response.getBody();
    }

}
