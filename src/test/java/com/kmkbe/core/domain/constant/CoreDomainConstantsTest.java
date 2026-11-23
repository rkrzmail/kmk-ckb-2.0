package com.kmkbe.core.domain.constant;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class CoreDomainConstantsTest {
  @Test void enumsHaveValues() {
    for (Class<? extends Enum<?>> c : new Class[]{AuditAction.class, AuditActorType.class, CompanyModel.class, CustomerIdType.class, CustomerModel.class, CustomerType.class, DistributionListSearchBy.class, FileAllocationType.class, FinancingStatus.class, GenderType.class, InvoiceSearchBy.class, LoginRole.class, ProductSearchBy.class}) {
      Enum<?>[] vals = c.getEnumConstants();
      assertThat(vals).isNotEmpty();
      for (Enum<?> v : vals) {
        assertThat(Enum.valueOf(c.asSubclass(Enum.class), v.name())).isEqualTo(v);
      }
    }
  }
}
