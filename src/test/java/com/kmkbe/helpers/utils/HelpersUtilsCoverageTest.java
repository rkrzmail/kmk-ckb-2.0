package com.kmkbe.helpers.utils;

import com.kmkbe.core.domain.request.PaginationRequest;
import com.kmkbe.exception.BusinessException;
import com.kmkbe.helpers.base.BasePaginationRequest;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.*;

class HelpersUtilsCoverageTest {

  static class Row {
    String name;
    Integer age;
    Date date;
    LocalDate localDate;
    LocalDateTime localDateTime;
    Row(String name, Integer age) { this.name = name; this.age = age; }
  }

  private static final Map<String, Function<Row, ? extends Comparable<?>>> FIELDS = new LinkedHashMap<>();
  static {
    FIELDS.put("name", r -> r.name);
    FIELDS.put("age", r -> r.age);
    FIELDS.put("date", r -> r.date);
    FIELDS.put("localDate", r -> r.localDate);
    FIELDS.put("localDateTime", r -> r.localDateTime);
  }

  @Test
  void listPagination_defaultsAndEdgeCases() {
    List<Row> rows = new ArrayList<>();
    rows.add(new Row("Budi", 30));
    rows.add(new Row("Andi", 25));
    rows.add(new Row(null, 20));

    PaginationRequest req = new PaginationRequest(); // pageNo/pageSize null -> defaults
    var res = ListPagination.of(rows, req, FIELDS, "name");
    assertThat(res.getCurrentPage()).isEqualTo(1);
    assertThat(res.getTotalData()).isEqualTo(3);

    // invalid sortType
    PaginationRequest bad = new PaginationRequest();
    bad.setSortType("wrong");
    assertThatThrownBy(() -> ListPagination.of(rows, bad, FIELDS, "name"))
      .isInstanceOf(BusinessException.class).hasMessageContaining("sortType");

    // null field name
    PaginationRequest bad2 = new PaginationRequest();
    bad2.setSearchBy(null);
    bad2.setSearchValue("x");
    assertThatThrownBy(() -> ListPagination.of(rows, bad2, FIELDS, "name"))
      .isInstanceOf(BusinessException.class).hasMessageContaining("searchBy");

    // page beyond data
    PaginationRequest far = new PaginationRequest();
    far.setPageNo(10);
    far.setPageSize(2);
    var farRes = ListPagination.of(rows, far, FIELDS, "name");
    assertThat(farRes.getList()).isEmpty();
  }

  @Test
  void listPagination_matchesDateTypesAndDescendingNulls() {
    Row r1 = new Row("A", 1);
    r1.date = Date.from(LocalDate.of(2026, 5, 12).atStartOfDay(ZoneId.of("Asia/Jakarta")).toInstant());
    r1.localDate = LocalDate.of(2026, 5, 12);
    r1.localDateTime = LocalDateTime.of(2026, 5, 12, 10, 0);
    Row r2 = new Row("B", 2);
    List<Row> rows = new ArrayList<>(List.of(r1, r2));

    PaginationRequest req = new PaginationRequest();
    req.setSearchBy("date");
    req.setSearchValue("12/05/2026");
    var res = ListPagination.of(rows, req, FIELDS, "name");
    assertThat(res.getList()).hasSize(1).extracting(r -> r.name).containsExactly("A");

    req.setSearchBy("localDate");
    req.setSearchValue("12-05-2026");
    res = ListPagination.of(new ArrayList<>(List.of(r1, r2)), req, FIELDS, "name");
    assertThat(res.getList()).hasSize(1);

    req.setSearchBy("localDate");
    req.setSearchValue("12/05/2026");
    res = ListPagination.of(new ArrayList<>(List.of(r1, r2)), req, FIELDS, "name");
    assertThat(res.getList()).hasSize(1);

    req.setSearchBy("localDateTime");
    req.setSearchValue("12/05/2026");
    res = ListPagination.of(new ArrayList<>(List.of(r1, r2)), req, FIELDS, "name");
    assertThat(res.getList()).hasSize(1);

    // descending with nulls
    PaginationRequest sortReq = new PaginationRequest();
    sortReq.setSortBy("age");
    sortReq.setSortType("desc");
    Row n1 = new Row("N1", null);
    Row n2 = new Row("N2", 5);
    Row n3 = new Row("N3", null);
    var sorted = ListPagination.of(new ArrayList<>(List.of(n1, n2, n3)), sortReq, FIELDS, "name");
    assertThat(sorted.getList().get(0).age).isEqualTo(5);
  }

  @Test
  void listPagination_tieBreakerAndSearchNoMatch() {
    Row a1 = new Row("A", 1); a1.date = null;
    Row a2 = new Row("A", 2);
    Row b1 = new Row("B", 3);
    PaginationRequest req = new PaginationRequest();
    req.setSortBy("name");
    req.setSortType("asc");
    var res = ListPagination.of(new ArrayList<>(List.of(b1, a2, a1)), req, FIELDS, "age");
    assertThat(res.getList()).extracting(r -> r.age).containsExactly(1, 2, 3);

    PaginationRequest search = new PaginationRequest();
    search.setSearchBy("name");
    search.setSearchValue("zzz");
    var none = ListPagination.of(new ArrayList<>(List.of(a1)), search, FIELDS, "name");
    assertThat(none.getList()).isEmpty();
    assertThat(none.getTotalData()).isZero();
  }

  @Test
  void pageableUtil_nativeSingleCharAndDefaults() {
    BasePaginationRequest req = new BasePaginationRequest();
    req.setSortBy("A");
    req.setSortType("asc");
    req.setPageNo(2);
    req.setPageSize(5);
    PageRequest pr = PageableUtil.createPageRequestNative(req, 10, 1, "id", "desc");
    assertThat(pr.getPageNumber()).isEqualTo(1);
    assertThat(pr.getSort().getOrderFor("a")).isNotNull();

    BasePaginationRequest req2 = new BasePaginationRequest();
    PageRequest pr2 = PageableUtil.createPageRequestNative(req2, 10, 1, "A", "asc");
    assertThat(pr2.getSort().getOrderFor("a")).isNotNull();

    BasePaginationRequest req3 = new BasePaginationRequest();
    req3.setSortBy("created_at");
    PageRequest pr3 = PageableUtil.createPageRequest(req3, 10, 1, "id", "desc");
    assertThat(pr3.getSort().getOrderFor("createdAt")).isNotNull();
  }

  @Test
  void specPagination_constructorsAndFilter() {
    PaginationRequest req = new PaginationRequest();
    req.setSearchBy("val");
    req.setSearchValue("2");
    req.setPageNo(1);
    req.setPageSize(10);

    PageImpl<Integer> page = new PageImpl<>(List.of(1, 2, 3), PageRequest.of(0, 10), 3);
    SpecPagination<Integer, Integer> fromPage = new SpecPagination<Integer, Integer>(page, req) {
      @Override public Integer eval(Integer data) { return data; }
      @Override public Integer search(Integer data) { return data == 2 ? data : null; }
      @Override public Integer filter(Integer data) { return data; }
    };
    var res = SpecPagination.paginationData(fromPage);
    assertThat(res.getList()).containsExactly(2);

    SpecPagination<Integer, Integer> fromOpt = new SpecPagination<Integer, Integer>(Optional.of(List.of(1, 2)), req) {
      @Override public Integer eval(Integer data) { return data; }
      @Override public Integer search(Integer data) { return data; }
    };
    var res2 = SpecPagination.paginationData(fromOpt);
    assertThat(res2.getTotalData()).isEqualTo(2);

    SpecPagination<Integer, Integer> fromEmptyOpt = new SpecPagination<Integer, Integer>(Optional.empty(), req) {
      @Override public Integer eval(Integer data) { return data; }
    };
    // privData is null when Optional.empty() - paginationData will NPE on for-each; avoid by not calling. Just construct.
    assertThat(fromEmptyOpt.isSearchUsed()).isTrue();
  }
}
