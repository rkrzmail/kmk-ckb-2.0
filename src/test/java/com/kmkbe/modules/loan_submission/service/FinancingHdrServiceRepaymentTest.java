package com.kmkbe.modules.loan_submission.service;

import com.kmkbe.core.domain.entity.FinancingDtl;
import com.kmkbe.core.domain.entity.FinancingHdr;
import com.kmkbe.core.domain.entity.Invoice;
import com.kmkbe.core.domain.repository.AgreementRepository;
import com.kmkbe.core.domain.repository.DisbursementLogRepository;
import com.kmkbe.core.domain.repository.FinancingDtlRepository;
import com.kmkbe.core.domain.repository.FinancingHdrRepository;
import com.kmkbe.core.domain.repository.InvoiceRepository;
import com.kmkbe.core.domain.repository.RedisRepository;
import com.kmkbe.core.domain.request.PaginationRequest;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FinancingHdrServiceRepaymentTest {

  @Test
  @SuppressWarnings("unchecked")
  void repaymentListIncludesPaidStatusAndPaidStep() {
    InvoiceRepository invoices = mock(InvoiceRepository.class);
    FinancingHdrService service = new FinancingHdrService(
      mock(FinancingHdrRepository.class), invoices, mock(RedisRepository.class),
      mock(AgreementRepository.class), mock(DisbursementLogRepository.class),
      mock(FinancingDtlRepository.class));
    when(invoices.findAll(any(Specification.class), any(Pageable.class))).thenReturn(Page.empty());

    service.listPaidUnpaidInvoice(new PaginationRequest());

    org.mockito.ArgumentCaptor<Specification<Invoice>> specification =
      org.mockito.ArgumentCaptor.forClass(Specification.class);
    verify(invoices).findAll(specification.capture(), any(Pageable.class));

    Root<Invoice> root = mock(Root.class);
    Join<Invoice, FinancingDtl> detail = mock(Join.class);
    Join<FinancingDtl, FinancingHdr> header = mock(Join.class);
    CriteriaBuilder builder = mock(CriteriaBuilder.class);
    Path<String> statusPath = mock(Path.class);
    Path<String> stepPath = mock(Path.class);
    Expression<String> status = mock(Expression.class);
    Expression<String> step = mock(Expression.class);
    Predicate paidStatus = mock(Predicate.class);
    Predicate paidStep = mock(Predicate.class);

    when(root.<Invoice, FinancingDtl>join("financingDtl", JoinType.INNER)).thenReturn(detail);
    when(detail.<FinancingDtl, FinancingHdr>join("financingHdr", JoinType.INNER)).thenReturn(header);
    when(header.<String>get("financingStatus")).thenReturn(statusPath);
    when(header.<String>get("financingStep")).thenReturn(stepPath);
    when(builder.lower(statusPath)).thenReturn(status);
    when(builder.lower(stepPath)).thenReturn(step);
    when(builder.equal(status, "paid")).thenReturn(paidStatus);
    when(builder.equal(step, "paid")).thenReturn(paidStep);

    specification.getValue().toPredicate(root, mock(CriteriaQuery.class), builder);

    verify(builder).and(paidStatus, paidStep);
  }
}
