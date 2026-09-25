package com.kmkbe.helpers.utils;

import com.kmkbe.core.domain.repository.AgreementRepository;
import com.kmkbe.core.domain.repository.FinancingHdrRepository;
import com.kmkbe.core.domain.request.PaginationRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import com.kmkbe.exception.BusinessException;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.query.QueryEnhancer;
import org.springframework.data.jpa.repository.query.QueryEnhancerFactory;
import java.lang.reflect.Method;
import java.util.Arrays;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NativePaginationSortTest {
  @ParameterizedTest
  @CsvSource({"bankName,asc", "bankName,desc", "rekeningNo,asc", "rekeningNo,desc"})
  void sharedBankFieldsUseStableTieBreakerInBothQueries(String field, String direction) throws Exception {
    var request = new PaginationRequest();
    request.setSortBy(field);
    request.setSortType(direction);
    for (String query : new String[]{"findAllListByCwrAndFinancingRaw", "findAllListByCwrAndFinancingSearch"}) {
      assertThat(enhance(AgreementRepository.class, query, PaginationSort.agreements(request)).toLowerCase())
        .contains("order by ag.agreement_id asc")
        .doesNotContain("ag.bank_name", "ag.rekening_no");
    }
  }

  @ParameterizedTest
  @CsvSource({"bankName", "rekeningNo"})
  void sharedBankFieldsStillRejectInvalidDirection(String field) {
    var request = new PaginationRequest();
    request.setSortBy(field);
    request.setSortType("invalid");
    assertThatThrownBy(() -> PaginationSort.agreements(request)).isInstanceOf(BusinessException.class);
  }

  @Test
  void filteredAgreementKeepsSearchAndCountRestrictionsWithSorting() throws Exception {
    String sql = enhance(AgreementRepository.class, "findAllListByCwrAndFinancingSearch", sort("agreementNo", true));
    assertThat(sql.toLowerCase()).contains("order by ag.agreement_code asc, ag.agreement_id asc")
      .contains("case :searchby").contains("lower(:searchvalue)");
    Method method = Arrays.stream(AgreementRepository.class.getMethods())
      .filter(m -> m.getName().equals("findAllListByCwrAndFinancingSearch")).findFirst().orElseThrow();
    assertThat(method.getAnnotation(Query.class).countQuery()).contains(AgreementRepository.SEARCH_FROM);
  }

  @Test
  void nativeAgreementSortUsesJoinedAliasAndPreservesWhere() throws Exception {
    String sql = enhance(AgreementRepository.class, "findAllListByCwrAndFinancingRaw", sort("agreementNo", true));
    assertThat(sql.toLowerCase()).contains("order by ag.agreement_code asc, ag.agreement_id asc")
      .contains("ag.cwr_code = :cwrCode".toLowerCase());
  }

  @Test
  void nativeAssignmentSortUsesJoinedAliasAndPreservesBranchRestriction() throws Exception {
    String sql = enhance(FinancingHdrRepository.class, "findAllAssignmentFinancingRaw", sort("custName", false));
    assertThat(sql.toLowerCase()).contains("order by c.cust_name asc, fh.financing_hdr_id asc")
      .contains("fh.branch_code = :branchcode");
  }

  private Sort sort(String field, boolean agreement) {
    var request = new PaginationRequest();
    request.setSortBy(field);
    request.setSortType("asc");
    return agreement ? PaginationSort.agreements(request) : PaginationSort.assignments(request);
  }

  private String enhance(Class<?> repository, String methodName, Sort sort) throws Exception {
    Method method = Arrays.stream(repository.getMethods()).filter(m -> m.getName().equals(methodName)).findFirst().orElseThrow();
    String sql = method.getAnnotation(Query.class).value();
    Class<?> declaredQuery = Class.forName("org.springframework.data.jpa.repository.query.DeclaredQuery");
    Method of = declaredQuery.getDeclaredMethod("of", String.class, boolean.class);
    of.setAccessible(true);
    QueryEnhancer enhancer = (QueryEnhancer) QueryEnhancerFactory.class.getMethod("forQuery", declaredQuery)
      .invoke(null, of.invoke(null, sql, true));
    return enhancer.applySorting(sort);
  }
}
