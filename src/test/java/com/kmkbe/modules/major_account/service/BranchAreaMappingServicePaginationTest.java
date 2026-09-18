package com.kmkbe.modules.major_account.service;

import com.kmkbe.core.domain.entity.BranchAreaMapping;
import com.kmkbe.core.domain.repository.BranchAreaMappingRepository;
import com.kmkbe.core.domain.request.PaginationRequest;
import com.kmkbe.exception.BusinessException;
import com.kmkbe.modules.common.service.AuditTrailService;
import com.kmkbe.modules.user.entity.MstBranch;
import com.kmkbe.modules.user.repository.MstBranchRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BranchAreaMappingServicePaginationTest {
  @Mock BranchAreaMappingRepository repository;
  @Mock MstBranchRepository branchRepository;
  @Mock BranchAreaMappingExcelParser parser;
  @Mock AuditTrailService auditTrailService;

  @Test
  void appliesRequestedSortAndPageAtRepository() {
    var service = new BranchAreaMappingService(repository, branchRepository, parser, auditTrailService);
    var request = new PaginationRequest();
    request.setPageNo(2);
    request.setPageSize(1);
    request.setSortBy("branch");
    request.setSortType("desc");
    request.setSearchBy("city");
    request.setSearchValue("jakarta");
    var row = BranchAreaMapping.builder().branchAreaMappingId(4L).area("Area")
      .province("DKI").city("Jakarta")
      .mstBranch(MstBranch.builder().branchName("Jakarta 1").build()).build();
    when(repository.findAll(any(Specification.class), any(Pageable.class)))
      .thenReturn(new PageImpl<>(List.of(row), PageRequest.of(1, 1), 2));

    var result = service.listBranch(request);

    assertThat(result.getTotalData()).isEqualTo(2);
    assertThat(result.getCurrentPage()).isEqualTo(2);
    assertThat(result.getList()).extracting(e -> e.getBranch()).containsExactly("Jakarta 1");
    ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
    verify(repository).findAll(any(Specification.class), pageable.capture());
    assertThat(pageable.getValue().getPageNumber()).isEqualTo(1);
    assertThat(pageable.getValue().getPageSize()).isEqualTo(1);
    assertThat(pageable.getValue().getSort().getOrderFor("mstBranch.branchName").isDescending()).isTrue();
  }

  @Test
  void rejectsUnknownSortField() {
    var service = new BranchAreaMappingService(repository, branchRepository, parser, auditTrailService);
    var request = new PaginationRequest();
    request.setSortBy("unknown");
    assertThatThrownBy(() -> service.listBranch(request))
      .isInstanceOf(BusinessException.class).hasMessageContaining("sortBy tidak didukung");
  }
}
