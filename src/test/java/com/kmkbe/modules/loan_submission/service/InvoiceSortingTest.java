package com.kmkbe.modules.loan_submission.service;

import com.kmkbe.core.domain.entity.FinancingDtl;
import com.kmkbe.core.domain.entity.FinancingHdr;
import com.kmkbe.core.domain.repository.FinancingDtlRepository;
import com.kmkbe.core.domain.repository.InvoiceRepository;
import com.kmkbe.core.domain.request.PaginationRequest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.mockito.ArgumentCaptor;
import java.util.UUID;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
import static org.assertj.core.api.Assertions.assertThat;

class InvoiceSortingTest {
  @ParameterizedTest
  @ValueSource(booleans = {false, true})
  void forwardsSortingWithAndWithoutSearch(boolean filtered) {
    var repository = mock(FinancingDtlRepository.class);
    var service = new InvoiceService(repository, mock(InvoiceRepository.class));
    var header = mock(FinancingHdr.class);
    if (filtered) when(header.getFinancingHdrCode()).thenReturn(UUID.randomUUID());
    var request = new PaginationRequest();
    request.setPageNo(2);
    request.setPageSize(5);
    request.setSortBy("customerInvoiceNo");
    request.setSortType("desc");
    if (filtered) {
      request.setSearchBy("customerInvoiceNo");
      request.setSearchValue("INV");
      when(repository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(Page.<FinancingDtl>empty());
    } else {
      when(repository.findByFinancingHdr(eq(header), any(Pageable.class))).thenReturn(Page.empty());
    }
    var baseRequest = new com.kmkbe.helpers.base.BasePaginationRequest(5, 2, "customerInvoiceNo", "desc",
      request.getSearchBy(), request.getSearchValue());
    service.invoiceSubmissionByFinancingHdr(header, baseRequest);
    var captor = ArgumentCaptor.forClass(Pageable.class);
    if (filtered) verify(repository).findAll(any(Specification.class), captor.capture());
    else verify(repository).findByFinancingHdr(eq(header), captor.capture());
    assertThat(captor.getValue().getPageNumber()).isEqualTo(1);
    assertThat(captor.getValue().getPageSize()).isEqualTo(5);
    assertThat(captor.getValue().getSort().getOrderFor("invoice.custInvNo").isDescending()).isTrue();
  }
}
