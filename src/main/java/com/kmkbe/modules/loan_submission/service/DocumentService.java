package com.kmkbe.modules.loan_submission.service;

import com.kmkbe.core.service.FileStorageService;
import com.kmkbe.core.utils.FileUtils;
import com.kmkbe.core.utils.UriUtils;
import com.kmkbe.core.domain.entity.Customer;
import com.kmkbe.modules.customer.utils.CustomerUtils;
import com.kmkbe.core.domain.dto.DocumentTemplateFinancingDto;
import com.kmkbe.core.domain.dto.LegalFileDto;
import com.kmkbe.core.domain.dto.MstFileTypeDto;
import com.kmkbe.core.domain.entity.LegalFile;
import com.kmkbe.core.domain.entity.MstFileType;
import com.kmkbe.core.domain.mapper.FileTypeMapper;
import com.kmkbe.core.domain.repository.MstFileTypeRepository;
import com.kmkbe.core.domain.dto.InquiryVendorRemoteDto;
import com.kmkbe.modules.remote.service.CustomerRemoteService;
import io.netty.util.internal.StringUtil;
import jakarta.servlet.http.HttpServletRequest;
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
    private final CustomerRemoteService customerRemoteService;

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

    public List<MstFileTypeDto> fetchAllLoanDocumentRequirement(
            HttpServletRequest httpServletRequest,
            Authentication authentication
    ) throws SignatureException {
        try {
            final Customer customer = CustomerUtils.authenticateCustomer(authentication);
            InquiryVendorRemoteDto vendor = null;
            try {
                vendor = customerRemoteService.inquiryVendor(customer.getCustExternalCode()).getData();
            } catch (Exception ignored) {

            }

            if (vendor != null) {
                vendor.getAktaPerubahanLink();
                vendor.getAktaPendirianLink();
                vendor.getPengesahanKemenkumhamLink();
                vendor.getPkpLink();
            }


            return mstFileTypeRepository.findAll()
                    .stream()
                    .map((file) -> {
                        MstFileTypeDto dto = FileTypeMapper.INSTANCE.mstFileToDto(file);
                        LegalFile legalFile = null;
                        try {
                            legalFile = legalFileService.fetchByCust(CustomerUtils.authenticateCustomer(authentication), file);
                        } catch (SignatureException ignored) {
                        }

                        if (legalFile != null) {
                            LegalFileDto legalFileDto = FileTypeMapper.INSTANCE.legalFileToDto(legalFile);
                            legalFileDto.setUploadedDate(legalFile.getDtmUpd());
                            legalFileDto.setFileUrl(
                                    UriUtils.getBaseUrl(httpServletRequest)
                                            //+ "/api/v1"
                                            + legalFile.getFilePath()
                                            + "/"
                                            + legalFile.getFileName()
                            );

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
    public LegalFileDto uploadLoanDocument(
            HttpServletRequest httpServletRequest,
            Authentication authentication,
            MultipartFile file,
            String fileTypeCode
    ) throws Exception {
        String code = null;
        try {
            final Customer customer = CustomerUtils.authenticateCustomer(authentication);
            final MstFileType mstFileType = mstFileTypeRepository.findByFileTypeCode(fileTypeCode).orElseThrow();
            final LegalFile existingFile = legalFileService.fetchByCust(CustomerUtils.authenticateCustomer(authentication), mstFileType);
            code = customer.getCustName();

            String requireExt = FileUtils.getFileNameExtension(mstFileType.getFileTypeName());
            if (!StringUtil.isNullOrEmpty(requireExt)) {
                requireExt = requireExt.toLowerCase();
            }

            final String uploadDir = customer.getCustCode() + "/loan_submission";
            final String uploadName = mstFileType.getFileTypeCode() + "_" + file.getOriginalFilename();
            final String uploadedPath = fileStorageService.save(
                    file,
                    uploadDir,
                    uploadName,//,
                    requireExt
            );

            LegalFileDto dto = legalFileService.create(
                    httpServletRequest,
                    customer,
                    mstFileType,
                    file,
                    FileUtils.getFilePathFromFullPath(uploadedPath),
                    uploadName
            );

            if (existingFile != null) {
                fileStorageService.delete(existingFile.getFilePath() + "/" + existingFile.getFileName(), "");
            }

            return dto;
        } catch (Exception e) {
            if (code != null) {
                fileStorageService.delete(file.getOriginalFilename(), code);
            }

            log.error("uploadLoanDocument, error {}", e.getMessage());
            throw e;
        }
    }

    public void delete(Long id) {
        try {
            legalFileService.delete(id);
        } catch (Exception e) {
            log.error("deteleLegalFile, error {}", e.getMessage());
            throw e;
        }
    }
}
