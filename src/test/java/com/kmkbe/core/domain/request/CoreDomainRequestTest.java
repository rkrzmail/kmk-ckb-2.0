package com.kmkbe.core.domain.request;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;
class CoreDomainRequestTest {
  @Test void inquiryDisburseQueryString() {
    var q = new InquiryDisburseQueryString(); q.setName("a");
    assertThat(q.getName()).isEqualTo("a");
  }
  @Test void inquiryDisburseOrderBy() {
    var o = new InquiryDisburseOrderBy(); o.setKey("k"); o.setValue("asc");
    assertThat(o.getKey()).isEqualTo("k");
    assertThat(o.getValue()).isEqualTo("asc");
  }
  @Test void inquiryDisburseCriterion() {
    var c = new InquiryDisburseCriterion(); c.setLow(1); c.setHigh(10); c.setPropName("p");
    assertThat(c.getLow()).isEqualTo(1);
    assertThat(c.getPropName()).isEqualTo("p");
  }
  @Test void inquiryDisburseRequest() {
    var r = new InquiryDisburseRequest(); 
    r.setCriteria(new java.util.ArrayList<>());
    r.setQueryString(new InquiryDisburseQueryString());
    assertThat(r.getCriteria()).isNotNull();
    assertThat(r.getQueryString()).isNotNull();
  }
}
