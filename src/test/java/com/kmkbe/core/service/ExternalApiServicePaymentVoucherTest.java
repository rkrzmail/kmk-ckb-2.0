package com.kmkbe.core.service;

import com.kmkbe.core.domain.dto.PaymentVoucherRemote;
import com.kmkbe.core.domain.dto.InquiryDataAgreementDto;
import com.kmkbe.core.domain.dto.InquiryDataAgreementDtoAgrmntObj;
import com.kmkbe.feign.client.ConfinsR3FeignClient;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ExternalApiServicePaymentVoucherTest {
    private final ConfinsR3FeignClient client = mock(ConfinsR3FeignClient.class);
    private final ExternalApiService service = new ExternalApiService(mock(RestTemplate.class), client);

    @Test
    void rejectsAgreementWithoutAgrmntDt() {
        InquiryDataAgreementDto response = new InquiryDataAgreementDto();
        response.setStatusCode("200");
        response.setAgrmntObj(new InquiryDataAgreementDtoAgrmntObj());
        when(client.getAgreementByAgrmntNo(any())).thenReturn(response);

        assertThatThrownBy(() -> service.getAgreementDate("AGR001"))
            .isInstanceOf(IllegalStateException.class).hasMessageContaining("AgrmntDt");
    }

    @Test
    void findsPaidVoucherByApAndUsesItsExecutionValueDate() {
        when(client.getPaymentVouchers(any())).thenReturn(paging(
            new PaymentVoucherRemote.PagingRow("412AP20270200006", "412AP20270200006", "999PVC20270500004", "PAID")));
        when(client.getPaymentVoucher(any())).thenReturn(new PaymentVoucherRemote.DetailResponse(
            "999PVC20270500004", "PAID", LocalDateTime.of(2027, 5, 12, 0, 0)));

        assertThat(service.getDisbursementExecutionDate("412AP20270200006", "412"))
            .isEqualTo(LocalDateTime.of(2027, 5, 12, 0, 0));

        ArgumentCaptor<PaymentVoucherRemote.PagingRequest> pagingRequest =
            ArgumentCaptor.forClass(PaymentVoucherRemote.PagingRequest.class);
        verify(client).getPaymentVouchers(pagingRequest.capture());
        assertThat(pagingRequest.getValue().queryString().name()).isEqualTo("searchPayVoucherInquiry");
        assertThat(pagingRequest.getValue().criteria()).extracting(PaymentVoucherRemote.Criterion::value)
            .containsExactly("10009", "412", "IDR", "412AP20270200006");
        assertThat(pagingRequest.getValue().criteria().getLast().propName()).contains("string_agg(AP2.ACC_PAYABLE_NO");
        assertThat(pagingRequest.getValue().requestDateTime()).isEqualTo(LocalDate.now().toString());

        ArgumentCaptor<PaymentVoucherRemote.DetailRequest> detailRequest =
            ArgumentCaptor.forClass(PaymentVoucherRemote.DetailRequest.class);
        verify(client).getPaymentVoucher(detailRequest.capture());
        assertThat(detailRequest.getValue().payVoucherNo()).isEqualTo("999PVC20270500004");
    }

    @Test
    void refusesMissingOrAmbiguousPaidVoucher() {
        when(client.getPaymentVouchers(any())).thenReturn(paging(
            new PaymentVoucherRemote.PagingRow("412AP20270200006", "412AP20270200006", "PV1", "NEW")));
        assertThatThrownBy(() -> service.getDisbursementExecutionDate("412AP20270200006", "412"))
            .isInstanceOf(IllegalStateException.class).hasMessageContaining("ditemukan 0");

        when(client.getPaymentVouchers(any())).thenReturn(paging(
            new PaymentVoucherRemote.PagingRow("412AP20270200006", "412AP20270200006", "PV1", "PAID"),
            new PaymentVoucherRemote.PagingRow("412AP20270200006", "412AP20270200006", "PV2", "PAID")));
        assertThatThrownBy(() -> service.getDisbursementExecutionDate("412AP20270200006", "412"))
            .isInstanceOf(IllegalStateException.class).hasMessageContaining("ditemukan 2");
        verify(client, never()).getPaymentVoucher(any());
    }

    @Test
    void refusesMissingExecutionValueDate() {
        when(client.getPaymentVouchers(any())).thenReturn(paging(
            new PaymentVoucherRemote.PagingRow("412AP20270200006", "412AP20270200006", "PV1", "PAID")));
        when(client.getPaymentVoucher(any())).thenReturn(new PaymentVoucherRemote.DetailResponse("PV1", "PAID", null));

        assertThatThrownBy(() -> service.getDisbursementExecutionDate("412AP20270200006", "412"))
            .isInstanceOf(IllegalStateException.class).hasMessageContaining("ExecutionValueDt");
    }

    @Test
    void refusesPaidVoucherForAnotherAp() {
        when(client.getPaymentVouchers(any())).thenReturn(paging(
            new PaymentVoucherRemote.PagingRow("OTHER-AP", "OTHER-AP", "PV1", "PAID")));

        assertThatThrownBy(() -> service.getDisbursementExecutionDate("412AP20270200006", "412"))
            .isInstanceOf(IllegalStateException.class).hasMessageContaining("ditemukan 0");
        verify(client, never()).getPaymentVoucher(any());
    }

    private static PaymentVoucherRemote.PagingResponse paging(PaymentVoucherRemote.PagingRow... rows) {
        return new PaymentVoucherRemote.PagingResponse(List.of(rows), rows.length,
            new PaymentVoucherRemote.Header("200"), "200");
    }
}
