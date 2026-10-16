package com.kmkbe.core.domain.model;

import com.kmkbe.core.domain.entity.FinancingHdr;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MappedFinancingStatusTest {

  @Test
  void paidHeaderMapsToPaidForEveryAudience() {
    FinancingHdr financing = new FinancingHdr();
    financing.setFinancingStatus("PAID");
    financing.setFinancingStep("PAID");

    for (MappedFinancingStatus.Type type : MappedFinancingStatus.Type.values()) {
      MappedFinancingStatus mapped = new MappedFinancingStatus(financing, type);
      assertThat(mapped.getStatus()).as(type.name()).isEqualTo("PAID");
      assertThat(mapped.getLabel()).as(type.name()).isEqualTo("Paid");
    }
  }

  @Test
  void livePaidRemainsPaidAndNewRemainsNew() {
    FinancingHdr financing = new FinancingHdr();
    financing.setFinancingStatus("LIVE");
    financing.setFinancingStep("PAID");
    assertThat(new MappedFinancingStatus(financing, MappedFinancingStatus.Type.MajorAccount).getStatus())
      .isEqualTo("PAID");

    financing.setFinancingStatus("NEW");
    financing.setFinancingStep("NEW");
    assertThat(new MappedFinancingStatus(financing, MappedFinancingStatus.Type.MajorAccount).getStatus())
      .isEqualTo("NEW");
  }
}
