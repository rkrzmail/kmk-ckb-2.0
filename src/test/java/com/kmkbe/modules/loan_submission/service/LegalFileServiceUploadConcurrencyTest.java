package com.kmkbe.modules.loan_submission.service;

import com.kmkbe.core.domain.dto.LegalFileDto;
import com.kmkbe.core.domain.entity.LegalFile;
import com.kmkbe.core.domain.entity.MstFileType;
import com.kmkbe.core.domain.repository.LegalFileRepository;
import com.kmkbe.core.service.FileStorageService;
import com.kmkbe.modules.customer.model.entity.Customer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LegalFileServiceUploadConcurrencyTest {

    @Mock private LegalFileRepository legalFileRepository;
    @Mock private FileStorageService fileStorageService;

    @InjectMocks
    private LegalFileService legalFileService;

    @Test
    @DisplayName("Fix concurrency bug: re-uploading an existing document updates entity in place without delete+save duplicate ID")
    void reuploadExistingDocumentUpdatesInPlaceWithoutDeletingAndReinsertingId() throws Exception {
        Customer customer = Customer.builder()
                .custId(100L)
                .custCode(UUID.randomUUID())
                .custName("PT Sejahtera")
                .build();

        MstFileType fileType = MstFileType.builder()
                .fileTypeId(10L)
                .fileTypeCode("DOC001")
                .fileTypeName("Akta")
                .build();

        LegalFile existingFile = new LegalFile();
        existingFile.setFileId(530L);
        existingFile.setCustCode(customer);
        existingFile.setFileTypeCode(fileType);
        existingFile.setFileName("old_akta.pdf");
        existingFile.setFilePath("cust/loan_submission/old_akta.pdf");

        // Existing file found in DB
        when(legalFileRepository.findByCustCodeAndFileTypeCode(customer, fileType))
                .thenReturn(Optional.of(existingFile));

        when(legalFileRepository.save(any(LegalFile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MockMultipartFile file = new MockMultipartFile("file", "new_akta.pdf", "application/pdf", "dummy".getBytes());
        MockHttpServletRequest request = new MockHttpServletRequest();

        LegalFileDto result = legalFileService.create(request, customer, fileType, file, "cust/loan_submission/new_akta.pdf", "new_akta.pdf");

        // MUST NOT call delete! Calling delete + save with the same ID causes Hibernate:
        // "Row was updated or deleted by another transaction (or unsaved-value mapping was incorrect): [LegalFile#530]"
        verify(legalFileRepository, never()).delete(any(LegalFile.class));

        // Must update existing file
        verify(legalFileRepository).save(existingFile);
        assertThat(existingFile.getFileId()).isEqualTo(530L);
        assertThat(existingFile.getFileName()).isEqualTo("new_akta.pdf");
        assertThat(result.getFileId()).isEqualTo(530L);
    }
}
