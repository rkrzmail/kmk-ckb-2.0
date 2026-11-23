package com.kmkbe.core.domain.model;

import com.kmkbe.core.domain.entity.EmailTemplate;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Map;
import static org.assertj.core.api.Assertions.*;

class CoreDomainModelTest {
  @Test void invoiceEmailPayload() {
    var p = InvoiceEmailPayload.builder().invoiceNo("INV").description("d").bouwheerName("b")
      .invoiceDate("01/01/2026").invoiceDueDate("02/01/2026").invoiceAmt("100").build();
    assertThat(p.getInvoiceNo()).isEqualTo("INV");
    assertThat(InvoiceEmailPayload.esc("<a>")).isEqualTo("&lt;a&gt;");
    assertThat(InvoiceEmailPayload.esc(null)).isEmpty();
    assertThat(InvoiceEmailPayload.toHtmlListBody(List.of())).isEmpty();
    String html = InvoiceEmailPayload.toHtmlListBody(List.of(p, p));
    assertThat(html).contains("INV").contains("<tr style");
  }

  @Test void commonResultCount() {
    CommonResultCount<String> c = new CommonResultCount<>();
    assertThat(c.success("d")).isSameAs(c);
    assertThat(c.success("d","m",1)).isSameAs(c);
    assertThat(c.fail(400,"bad")).isSameAs(c);
    assertThat(c.fail(401,"bad2","d")).isSameAs(c);
  }

  @Test void paginationResult() {
    PaginationResult<String> p = PaginationResult.<String>builder().currentPage(1).totalPage(1).totalData(0L).list(List.of()).build();
    assertThat(p.getCurrentPage()).isEqualTo(1);
    PaginationResult<String> empty = PaginationResult.empty(1);
    assertThat(empty.getTotalData()).isEqualTo(0L);
  }

  @Test void httpLoggerPayload() {
    var p = HttpLoggerPayload.builder().uri("/x").httpMethod("GET").statusCode(200).request(Map.of()).response(Map.of()).controller("c").build();
    assertThat(p.toCollection().get("uri")).isEqualTo("/x");
  }

  @Test void bouwheerPaymentEmailPayload() {
    EmailTemplate t = new EmailTemplate();
    t.setBodyMail("{bouwheerName}-{vendorName}-{vendorCode}-{accountNo}-{bankAccount}-{bankName}-{bankKey}-{tglPengajuan}");
    var p = BouwheerPaymentEmailPayload.builder().bouwheerName("b").vendorName("v").vendorCode("vc")
      .accountNo("a").bankAccount("ba").bankName("bn").bankKey("bk").tglPengajuan("tgl").invoices(List.of()).build();
    assertThat(p.bodyMail(t)).isEqualTo("b-v-vc-a-ba-bn-bk-tgl");
  }
}
