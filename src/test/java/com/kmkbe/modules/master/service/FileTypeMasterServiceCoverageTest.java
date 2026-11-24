package com.kmkbe.modules.master.service;

import com.kmkbe.core.domain.entity.MstFileType;
import com.kmkbe.core.domain.repository.AgreementFileRepository;
import com.kmkbe.core.domain.repository.AgreementFileSigningRepository;
import com.kmkbe.core.domain.repository.DokumentHistoryUploadRepository;
import com.kmkbe.core.domain.repository.LegalFileRepository;
import com.kmkbe.core.domain.repository.MstFileTypeRepository;
import com.kmkbe.core.security.CurrentUserService;
import com.kmkbe.exception.BusinessException;
import com.kmkbe.helpers.base.BasePaginationRequest;
import com.kmkbe.modules.bouwheer.model.entity.Bouwheer;
import com.kmkbe.modules.bouwheer.repository.BouwheerRepository;
import com.kmkbe.modules.master.request.FileTypeRequest;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FileTypeMasterServiceCoverageTest {
  @Mock MstFileTypeRepository repository;
  @Mock BouwheerRepository bouwheerRepository;
  @Mock LegalFileRepository legalFileRepository;
  @Mock DokumentHistoryUploadRepository dokumentHistoryUploadRepository;
  @Mock AgreementFileRepository agreementFileRepository;
  @Mock AgreementFileSigningRepository signingRepository;
  @Mock CurrentUserService currentUserService;
  FileTypeMasterService service;

  @BeforeEach
  void setUp() {
    service = new FileTypeMasterService(repository, bouwheerRepository, legalFileRepository,
      dokumentHistoryUploadRepository, agreementFileRepository, signingRepository, currentUserService);
  }

  @Test
  void listValidationBranches() {
    BasePaginationRequest request = new BasePaginationRequest();
    request.setPageNo(0);
    assertThatThrownBy(() -> service.list(request)).isInstanceOf(RuntimeException.class);

    request.setPageNo(1);
    request.setSortType("bad");
    assertThatThrownBy(() -> service.list(request)).isInstanceOf(RuntimeException.class);

    request.setSortType(null);
    request.setSearchValue("value");
    assertThatThrownBy(() -> service.list(request)).isInstanceOf(RuntimeException.class)
      .hasMessageContaining("searchBy tidak didukung");

    request.setSearchBy("fileTypeName");
    when(repository.findAll(any(Specification.class), any(Pageable.class)))
      .thenReturn(new PageImpl<>(List.of(MstFileType.builder().fileTypeCode("DOC001").build())));
    assertThat(service.list(request).getList()).hasSize(1);
  }

  @Test
  void listSpecBranches() {
    BasePaginationRequest request = new BasePaginationRequest();
    request.setSearchValue("value");
    when(repository.findAll(any(Specification.class), any(Pageable.class)))
      .thenAnswer(call -> {
        evaluate(call.getArgument(0));
        return new PageImpl<>(List.of());
      });
    for (String field : List.of("fileTypeId", "maxSizeMb", "isMandatory", "isEsign",
      "bouwheerCode", "fileTypeName")) {
      request.setSearchBy(field);
      assertThatCode(() -> service.list(request)).doesNotThrowAnyException();
    }
  }

  @Test
  void createUpdateDeleteAndGetBranches() {
    FileTypeRequest request = new FileTypeRequest(" DOC001 ", "Doc", "Desc", "Legal", true, true, 10L,
      UUID.randomUUID());
    when(repository.save(any(MstFileType.class))).thenAnswer(call -> call.getArgument(0));
    when(currentUserService.usernameOrDefault("SYSTEM")).thenReturn("admin");
    assertThat(service.create(request).fileTypeCode()).isEqualTo("DOC001");

    MstFileType entity = MstFileType.builder().fileTypeCode("DOC001").build();
    when(repository.findById("DOC001")).thenReturn(Optional.of(entity));
    when(bouwheerRepository.findById(any())).thenReturn(Optional.empty());
    assertThat(service.get("DOC001")).isNotNull();
    assertThat(service.update("DOC001", request).fileTypeName()).isEqualTo("Doc");

    when(repository.findById("MISSING")).thenReturn(Optional.empty());
    assertThatThrownBy(() -> service.get("MISSING")).isInstanceOf(RuntimeException.class);

    when(legalFileRepository.existsByFileTypeCode_FileTypeCode("DOC001")).thenReturn(true);
    assertThatThrownBy(() -> service.delete("DOC001")).isInstanceOf(RuntimeException.class);
    when(legalFileRepository.existsByFileTypeCode_FileTypeCode("DOC001")).thenReturn(false);
    when(dokumentHistoryUploadRepository.existsByFileTypeCode("DOC001")).thenReturn(true);
    assertThatThrownBy(() -> service.delete("DOC001")).isInstanceOf(RuntimeException.class);
    when(dokumentHistoryUploadRepository.existsByFileTypeCode("DOC001")).thenReturn(false);
    when(agreementFileRepository.existsByMstFileType_FileTypeCode("DOC001")).thenReturn(true);
    assertThatThrownBy(() -> service.delete("DOC001")).isInstanceOf(RuntimeException.class);
    when(agreementFileRepository.existsByMstFileType_FileTypeCode("DOC001")).thenReturn(false);
    when(signingRepository.existsByFileTypeCode("DOC001")).thenReturn(true);
    assertThatThrownBy(() -> service.delete("DOC001")).isInstanceOf(RuntimeException.class);

    when(signingRepository.existsByFileTypeCode("DOC001")).thenReturn(false);
    doThrow(new DataIntegrityViolationException("fk")).when(repository).delete(entity);
    assertThatThrownBy(() -> service.delete("DOC001")).isInstanceOf(RuntimeException.class)
      .hasMessageContaining("masih direferensikan");
  }

  @Test
  void privateHelpersCoverBranches() {
    assertThat((Object) ReflectionTestUtils.invokeMethod(service, "toResponse",
      MstFileType.builder().fileTypeCode("DOC001").build())).isNotNull();
    assertThat((Object) ReflectionTestUtils.invokeMethod(service, "badRequest", "error"))
      .isInstanceOf(BusinessException.class);
  }

  private void evaluate(Specification<MstFileType> spec) {
    Root root = mock(Root.class);
    CriteriaBuilder builder = mock(CriteriaBuilder.class);
    Path path = mock(Path.class);
    Predicate predicate = mock(Predicate.class);
    lenient().when(root.get(anyString())).thenReturn(path);
    lenient().when(builder.conjunction()).thenReturn(predicate);
    lenient().when(builder.disjunction()).thenReturn(predicate);
    lenient().when(builder.equal(any(), any())).thenReturn(predicate);
    lenient().when(builder.lower(any())).thenReturn(mock(jakarta.persistence.criteria.Expression.class));
    lenient().when(builder.like(any(), anyString())).thenReturn(predicate);
    assertThat(spec.toPredicate(root, mock(jakarta.persistence.criteria.CriteriaQuery.class), builder))
      .isSameAs(predicate);
  }
}
