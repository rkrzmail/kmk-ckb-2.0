package com.kmkbe.modules.loan_submission.service;

import com.kmkbe.core.service.FileStorageService;
import com.kmkbe.modules.customer.entity.Customer;
import com.kmkbe.modules.customer.utils.CustomerUtils;
import com.kmkbe.modules.loan_submission.dto.DocumentTemplateFinancingDto;
import com.kmkbe.modules.loan_submission.dto.LegalFileDto;
import com.kmkbe.modules.loan_submission.dto.MstFileTypeDto;
import com.kmkbe.modules.loan_submission.entity.MstFileType;
import com.kmkbe.modules.loan_submission.mapper.FileTypeMapper;
import com.kmkbe.modules.loan_submission.repository.MstFileTypeRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.security.SignatureException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentService {
    private final MstFileTypeRepository mstFileTypeRepository;
    private final FileStorageService fileStorageService;
    private final LegalFileService legalFileService;

    public List<DocumentTemplateFinancingDto> fetchDocumentTemplateFinancing() throws Exception {
        try {
            return Arrays.asList(
                    DocumentTemplateFinancingDto.builder()
                            .fileName("Surat Instruksi Transfer (SI)")
                            .fileUrl("https://google.com")
                            .build(),
                    DocumentTemplateFinancingDto.builder()
                            .fileName("Formulir Aplikasi Pembiayaan")
                            .fileUrl("https://google.com")
                            .build()
            );
        } catch (Exception e) {
            log.error("fetchDocumentTemplateFinancing: {}", e.getMessage());
            throw e;
        }
    }

    public List<MstFileTypeDto> fetchAllLoanDocumentRequirement(Authentication authentication) throws Exception {
        try {
            return mstFileTypeRepository.findAll()
                    .stream()
                    .map((file) -> {
                        MstFileTypeDto dto = FileTypeMapper.INSTANCE.mstFileToDto(file);
                        LegalFileDto legalFileDto = null;
                        try {
                            legalFileDto = FileTypeMapper.INSTANCE.legalFileToDto(
                                    legalFileService.fetchByCust(CustomerUtils.authenticateCustomer(authentication), file)
                            );
                        } catch (SignatureException ignored) {
                        }

                        if (legalFileDto != null) {
                            legalFileDto.setFileUrl(file.getLegalFile().getFilePath());
                            legalFileDto.setUploadedDate(file.getLegalFile().getDtmCrt());
                            dto.setLegalFile(legalFileDto);
                        }

                        return dto;
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("getAllLoanDocumentRequirement: {}", e.getMessage());
            throw e;
        }
    }

    @Transactional
    public void uploadLoanDocument(
            Authentication authentication,
            MultipartFile file,
            String fileTypeCode
    ) throws Exception {
        String code = null;
        try {
            final Customer customer = CustomerUtils.authenticateCustomer(authentication);
            final MstFileType mstFileType = mstFileTypeRepository.findByFileTypeCode(fileTypeCode).orElseThrow();
            code = customer.getCustName();

            final String uploadedPath = fileStorageService.save(file, code);
            legalFileService.create(
                    customer,
                    mstFileType,
                    file,
                    uploadedPath
            );
        } catch (Exception e) {
            if (code != null) {
                fileStorageService.delete(file.getOriginalFilename(), code);
            }

            log.error("uploadLoanDocument, error {}", e.getMessage());
            throw e;
        }
    }
}
