package com.kmkbe.core.domain.constant;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;
class CoreDomainConstantsExtraTest {
  @Test void enumMethods() {
    for (InvoiceSearchBy e : InvoiceSearchBy.values()) assertThat(InvoiceSearchBy.valueOf(e.name())).isEqualTo(e);
    for (GenderType e : GenderType.values()) assertThat(GenderType.valueOf(e.name())).isEqualTo(e);
    for (ProductSearchBy e : ProductSearchBy.values()) assertThat(ProductSearchBy.valueOf(e.name())).isEqualTo(e);
  }
}
