package com.kmkbe.core.domain.model;

import com.kmkbe.core.domain.entity.FinancingHdr;
import com.kmkbe.helpers.constant.StatusConstants;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class MappedFinancingStatusExtraTest {
  private FinancingHdr hdr(String st, String step){ FinancingHdr h=new FinancingHdr(); h.setFinancingStatus(st); h.setFinancingStep(step); return h; }

  @Test void allTypesAndBranches() {
    for (MappedFinancingStatus.Type t : MappedFinancingStatus.Type.values()) {
      assertThat(new MappedFinancingStatus(hdr(StatusConstants.FINANCING_STATUS_PAID, StatusConstants.FINANCING_STEP_PAID), t).getStatus()).isNotBlank();
      assertThat(new MappedFinancingStatus(hdr(StatusConstants.FINANCING_STATUS_PAID, StatusConstants.FINANCING_STEP_SIGNED), t).getStatus()).isNotBlank();
      assertThat(new MappedFinancingStatus(hdr("new",""), t).getStatus()).isNotBlank();
      assertThat(new MappedFinancingStatus(hdr("inprocess",StatusConstants.FINANCING_STEP_ASSIGNMENT), t).getStatus()).isNotNull();
      assertThat(new MappedFinancingStatus(hdr("inprocess",StatusConstants.FINANCING_STEP_INPROCESS), t).getStatus()).isNotNull();
      assertThat(new MappedFinancingStatus(hdr("inprocess",StatusConstants.FINANCING_STEP_SIGNING), t).getStatus()).isNotNull();
      assertThat(new MappedFinancingStatus(hdr("inprocess",StatusConstants.FINANCING_STEP_SIGNED), t).getStatus()).isNotNull();
      assertThat(new MappedFinancingStatus(hdr("live",StatusConstants.FINANCING_STEP_GOLIVE), t).getStatus()).isNotBlank();
      assertThat(new MappedFinancingStatus(hdr("live",StatusConstants.FINANCING_STEP_PAID), t).getStatus()).isNotBlank();
      assertThat(new MappedFinancingStatus(hdr("unknown","x"), t).getStatus()).isNotBlank();
    }
  }
}
