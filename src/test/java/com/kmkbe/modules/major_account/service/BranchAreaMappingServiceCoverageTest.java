package com.kmkbe.modules.major_account.service;

import com.kmkbe.core.domain.entity.BranchAreaMapping;
import com.kmkbe.core.domain.repository.BranchAreaMappingRepository;
import com.kmkbe.modules.user.repository.MstBranchRepository;
import com.kmkbe.core.domain.request.PaginationRequest;
import com.kmkbe.helpers.base.BasePaginationRequest;
import com.kmkbe.modules.common.service.AuditTrailService;
import com.kmkbe.modules.user.entity.MstBranch;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BranchAreaMappingServiceCoverageTest {
  @Mock BranchAreaMappingRepository repository;
  @Mock MstBranchRepository branchRepository;
  @Mock BranchAreaMappingExcelParser parser;
  @Mock AuditTrailService auditTrailService;
  BranchAreaMappingService service;

  @BeforeEach
  void setUp() {
    service = new BranchAreaMappingService(repository, branchRepository, parser, auditTrailService);
  }

  @Test
  void placementBranchAndMstBranchService() {
    when(repository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(mapping())));
    PaginationRequest request = new PaginationRequest();
    request.setPageNo(2);
    request.setPageSize(5);
    assertThat(service.placementBranch(request).getList()).hasSize(1);
    when(repository.findAll(any(Pageable.class))).thenThrow(new RuntimeException("db down"));
    assertThatThrownBy(() -> service.placementBranch(request)).hasMessage("db down");

    MstBranchService branchService = new MstBranchService(branchRepository);
    MstBranch cbu = MstBranch.builder().branchCode("B1").branchName("Branch").businessUnit("CBU").build();
    MstBranch other = MstBranch.builder().branchCode("B2").businessUnit("OTHER").build();
    when(branchRepository.findAllActive()).thenReturn(List.of(cbu, other));
    assertThat(branchService.branchList("")).extracting("branchCode").containsExactly("B1");
    when(branchRepository.findAllActive()).thenReturn(null);
    assertThat(branchService.branchList("")).isEmpty();
    when(branchRepository.findAllActive()).thenThrow(new RuntimeException("db down"));
    assertThatThrownBy(() -> branchService.branchList("")).hasMessage("db down");
  }

  @Test
  void updateBranchImportsRowsAndCoversErrors() {
    MultipartFile file = mock(MultipartFile.class);
    when(file.getOriginalFilename()).thenReturn("mapping.xlsx");
    when(parser.parse(file)).thenReturn(List.of(
      new BranchAreaMappingExcelParser.BranchAreaMappingRow(1L, "A", "B", "C", "B1"),
      new BranchAreaMappingExcelParser.BranchAreaMappingRow(2L, "D", "E", "F", "MISSING")));
    when(branchRepository.findByBranchCode("B1"))
      .thenReturn(Optional.of(MstBranch.builder().branchCode("B1").build()));
    assertThat(service.updateBranch(new MockHttpServletRequest(), file)).isNull();
    verify(auditTrailService).recordEvent(anyString(), any(), anyString(), any(), any(), any());

    org.mockito.Mockito.doThrow(new RuntimeException("bad file")).when(parser).parse(file);
    assertThatThrownBy(() -> service.updateBranch(new MockHttpServletRequest(), file))
      .isInstanceOf(RuntimeException.class).hasMessageContaining("bad file");

    org.mockito.Mockito.doReturn(List.of()).when(parser).parse(file);
    org.mockito.Mockito.doThrow(new RuntimeException("save failed")).when(repository).saveAll(any());
    assertThatThrownBy(() -> service.updateBranch(new MockHttpServletRequest(), file))
      .isInstanceOf(RuntimeException.class).hasMessageContaining("save failed");
  }

  @Test
  void listBranchValidationBranches() {
    PaginationRequest request = new PaginationRequest();
    request.setSortType("bad");
    assertThatThrownBy(() -> service.listBranch(request)).isInstanceOf(RuntimeException.class);

    request.setSortType(null);
    request.setPageNo(0);
    assertThatThrownBy(() -> service.listBranch(request)).isInstanceOf(RuntimeException.class);

    request.setPageNo(1);
    when(repository.findAll(any(Specification.class), any(Pageable.class)))
      .thenAnswer(call -> {
        evaluate(call.getArgument(0));
        return new PageImpl<>(List.of());
      });
    request.setSearchValue("Jakarta");
    assertThatThrownBy(() -> service.listBranch(request)).isInstanceOf(RuntimeException.class)
      .hasMessageContaining("searchBy wajib");

    request.setSearchBy("unknown");
    assertThatThrownBy(() -> service.listBranch(request)).isInstanceOf(RuntimeException.class)
      .hasMessageContaining("searchBy tidak didukung");

    request.setSearchBy("branchAreaMappingId");
    request.setSearchValue("not-number");
    when(repository.findAll(any(Specification.class), any(Pageable.class)))
      .thenReturn(new PageImpl<>(List.of()));
    assertThat(service.listBranch(request).getList()).isEmpty();

    BasePaginationRequest base = new BasePaginationRequest();
    base.setPageNo(1);
    base.setPageSize(10);
    assertThat(service.listBranch(base).getList()).isEmpty();
  }

  @Test
  void listBranchSpecBranches() {
    PaginationRequest request = new PaginationRequest();
    request.setSearchValue("value");
    when(repository.findAll(any(Specification.class), any(Pageable.class)))
      .thenAnswer(call -> {
        evaluate(call.getArgument(0));
        return new PageImpl<>(List.of());
      });
    for (String field : List.of("branchAreaMappingId", "area", "province", "city", "branch")) {
      request.setSearchBy(field);
      assertThatCode(() -> service.listBranch(request)).doesNotThrowAnyException();
    }
    request.setSearchValue(null);
    assertThatCode(() -> service.listBranch(request)).doesNotThrowAnyException();
  }

  private void evaluate(Specification<BranchAreaMapping> spec) {
    Root root = mock(Root.class);
    CriteriaBuilder builder = mock(CriteriaBuilder.class);
    Path path = mock(Path.class);
    Predicate predicate = mock(Predicate.class);
    lenient().when(root.get(anyString())).thenReturn(path);
    lenient().when(root.join(anyString(), any())).thenReturn(mock(jakarta.persistence.criteria.Join.class));
    lenient().when(builder.conjunction()).thenReturn(predicate);
    lenient().when(builder.disjunction()).thenReturn(predicate);
    lenient().when(builder.equal(any(), any())).thenReturn(predicate);
    lenient().when(builder.lower(any())).thenReturn(mock(Expression.class));
    lenient().when(builder.like(any(), anyString())).thenReturn(predicate);
    assertThat(spec.toPredicate(root, mock(jakarta.persistence.criteria.CriteriaQuery.class), builder))
      .isSameAs(predicate);
  }

  private static BranchAreaMapping mapping() {
    return BranchAreaMapping.builder().branchAreaMappingId(1L).area("Area")
      .province("Province").city("City")
      .mstBranch(MstBranch.builder().branchName("Branch").build()).build();
  }
}
