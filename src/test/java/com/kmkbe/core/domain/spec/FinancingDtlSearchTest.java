package com.kmkbe.core.domain.spec;

import com.kmkbe.core.domain.entity.FinancingDtl;
import jakarta.persistence.criteria.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import java.util.UUID;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

class FinancingDtlSearchTest {
  @ParameterizedTest
  @CsvSource({"customerInvoiceNo,INV CV 1,%inv cv 1%", "invoiceDescription,Dokumen PT ABC,%dokumen pt abc%",
    "poNumber,PO. 01,%po. 01%", "invoiceDate,10/09/2026,%10/09/2026%"})
  @SuppressWarnings({"unchecked", "rawtypes"})
  void preservesTextAndDateCharactersInSearchPredicate(String field, String input, String expected) {
    Root<FinancingDtl> root = mock(Root.class);
    Join invoice = mock(Join.class);
    Join header = mock(Join.class);
    Join customer = mock(Join.class);
    Join agreement = mock(Join.class);
    Join bouwheer = mock(Join.class);
    CriteriaBuilder cb = mock(CriteriaBuilder.class);
    doReturn(invoice).when(root).join("invoice", JoinType.INNER);
    doReturn(header).when(root).join("financingHdr", JoinType.INNER);
    doReturn(customer).when(header).join("customer", JoinType.INNER);
    doReturn(agreement).when(header).join("agreement", JoinType.LEFT);
    doReturn(bouwheer).when(header).join("bouwheer", JoinType.INNER);
    FinancingDtlSpec.custInvoiceFilterBy(UUID.randomUUID(), field, input)
      .toPredicate(root, mock(CriteriaQuery.class), cb);
    verify(cb).like(nullable(Expression.class), eq(expected));
  }
}
