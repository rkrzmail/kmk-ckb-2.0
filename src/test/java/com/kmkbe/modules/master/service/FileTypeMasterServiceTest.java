package com.kmkbe.modules.master.service;

import com.kmkbe.core.domain.entity.MstFileType;
import com.kmkbe.core.domain.repository.AgreementFileRepository;
import com.kmkbe.core.domain.repository.AgreementFileSigningRepository;
import com.kmkbe.core.domain.repository.LegalFileRepository;
import com.kmkbe.core.domain.repository.MstFileTypeRepository;
import com.kmkbe.core.security.CurrentUserService;
import com.kmkbe.exception.BusinessException;
import com.kmkbe.helpers.base.BasePaginationRequest;
import com.kmkbe.modules.master.request.FileTypeRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FileTypeMasterServiceTest {
    @Mock MstFileTypeRepository repository;
    @Mock LegalFileRepository legalFileRepository;
    @Mock AgreementFileRepository agreementFileRepository;
    @Mock AgreementFileSigningRepository signingRepository;
    @Mock CurrentUserService currentUserService;
    @InjectMocks FileTypeMasterService service;

    private static FileTypeRequest request(String code) {
        return new FileTypeRequest(code, "Perjanjian", "Dokumen perjanjian", "Financing", true, 10L, null);
    }

    @Test
    void listsWithDatabasePagingAndSort() {
        BasePaginationRequest request = new BasePaginationRequest();
        request.setPageNo(2);
        request.setPageSize(5);
        request.setSortBy("fileTypeName");
        request.setSortType("DESC");
        request.setSearchBy("fileTypeName");
        request.setSearchValue("perjanjian");
        when(repository.findAll(any(Specification.class), any(Pageable.class)))
            .thenReturn(new PageImpl<>(List.of(MstFileType.builder().fileTypeCode("AGGREMENT01").build())));

        var result = service.list(request);

        assertThat(result.getCurrentPage()).isEqualTo(2);
        assertThat(result.getList()).extracting("fileTypeCode").containsExactly("AGGREMENT01");
        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(repository).findAll(any(Specification.class), captor.capture());
        assertThat(captor.getValue().getPageNumber()).isEqualTo(1);
        assertThat(captor.getValue().getPageSize()).isEqualTo(5);
        assertThat(captor.getValue().getSort().getOrderFor("fileTypeName").getDirection().name()).isEqualTo("DESC");
    }

    @Test
    void rejectsUnsupportedSort() {
        BasePaginationRequest request = new BasePaginationRequest();
        request.setSortBy("legalFiles");
        assertThatThrownBy(() -> service.list(request)).isInstanceOf(BusinessException.class);
        verify(repository, never()).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void rejectsDuplicateCode() {
        when(repository.existsById("AGGREMENT01")).thenReturn(true);
        assertThatThrownBy(() -> service.create(request("AGGREMENT01"))).isInstanceOf(BusinessException.class);
        verify(repository, never()).save(any());
    }

    @Test
    void createsFileTypeWithAuditFields() {
        when(repository.save(any(MstFileType.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(currentUserService.usernameOrDefault("SYSTEM")).thenReturn("admin");

        var result = service.create(request("AGGREMENT01"));

        assertThat(result.fileTypeCode()).isEqualTo("AGGREMENT01");
        assertThat(result.usrCrt()).isEqualTo("admin");
        assertThat(result.dtmCrt()).isNotNull();
        assertThat(result.maxSizeMb()).isEqualTo(10L);
    }

    @Test
    void rejectsPrimaryKeyChange() {
        when(repository.findById("AGGREMENT01"))
            .thenReturn(Optional.of(MstFileType.builder().fileTypeCode("AGGREMENT01").build()));
        assertThatThrownBy(() -> service.update("AGGREMENT01", request("OTHER")))
            .isInstanceOf(BusinessException.class);
        verify(repository, never()).save(any());
    }

    @Test
    void refusesDeleteForReferencedFileType() {
        when(repository.findById("AGGREMENT01"))
            .thenReturn(Optional.of(MstFileType.builder().fileTypeCode("AGGREMENT01").build()));
        when(agreementFileRepository.existsByMstFileType_FileTypeCode("AGGREMENT01")).thenReturn(true);
        assertThatThrownBy(() -> service.delete("AGGREMENT01"))
            .isInstanceOf(BusinessException.class);
        verify(repository, never()).delete(any(MstFileType.class));
    }

    @Test
    void deletesUnusedFileType() {
        MstFileType entity = MstFileType.builder().fileTypeCode("TEST").build();
        when(repository.findById("TEST")).thenReturn(Optional.of(entity));
        service.delete("TEST");
        verify(repository).delete(entity);
        verify(repository).flush();
    }
}
