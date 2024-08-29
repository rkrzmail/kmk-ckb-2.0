package com.kmkbe.modules.loan_submission.service;

import com.kmkbe.core.domain.dto.DocumentTemplateFinancingDto;
import com.kmkbe.core.domain.dto.InquiryVendorRemoteDto;
import com.kmkbe.core.domain.dto.LegalFileDto;
import com.kmkbe.core.domain.dto.MstFileTypeDto;
import com.kmkbe.core.domain.entity.Customer;
import com.kmkbe.core.domain.entity.LegalFile;
import com.kmkbe.core.domain.entity.MstFileType;
import com.kmkbe.core.domain.mapper.FileTypeMapper;
import com.kmkbe.core.domain.model.PaginationResult;
import com.kmkbe.core.domain.repository.CustomerRepository;
import com.kmkbe.core.domain.repository.LegalFileRepository;
import com.kmkbe.core.domain.repository.MstFileTypeRepository;
import com.kmkbe.core.domain.request.PaginationRequest;
import com.kmkbe.core.service.FileStorageService;
import com.kmkbe.core.utils.FileUtils;
import com.kmkbe.core.utils.HttpUtils;
import com.kmkbe.core.utils.UriUtils;
import com.kmkbe.modules.customer.utils.CustomerUtils;
import com.kmkbe.modules.remote.service.CustomerRemoteService;
import io.netty.util.internal.StringUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.security.SignatureException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentService {
    private final MstFileTypeRepository mstFileTypeRepository;
    private final LegalFileRepository legalFileRepository;
    private final FileStorageService fileStorageService;
    private final LegalFileService legalFileService;
    private final CustomerRemoteService customerRemoteService;
    private final CustomerRepository customerRepository;

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
            InquiryVendorRemoteDto inquiryVendorRemote = null;
            try {
                inquiryVendorRemote = customerRemoteService
                        .inquiryVendor(customer.getCustExternalCode()).getData();

            } catch (Exception e) {
                //throw new IllegalStateException("Your vendor is not registered from MST Integeration");
            }

            if (inquiryVendorRemote != null) {
                mappingFromInquiryVendor(customer, inquiryVendorRemote);
            }

            return mstFileTypeRepository.findAll(Sort.by("fileTypeId").descending())
                    .stream()
                    .map((file) -> {
                        MstFileTypeDto dto = FileTypeMapper.INSTANCE.mstFileToDto(file);
                        LegalFile legalFile = null;

                        try {
                            legalFile = legalFileService.fetchByMstFileTypeAndCust(CustomerUtils.authenticateCustomer(authentication), file);
                        } catch (SignatureException ignored) {
                        }

                        if (legalFile != null) {
                            LegalFileDto legalFileDto = FileTypeMapper.INSTANCE.legalFileToDto(legalFile);
                            legalFileDto.setUploadedDate(legalFile.getDtmUpd());

                            String generatedUrl = fileUlr(
                                    httpServletRequest,
                                    legalFile
                            );

                            if (legalFile.getFilePath() != null && legalFile.getFilePath().contains("http")) {
                                generatedUrl = legalFile.getFilePath();
                            }

                            legalFileDto.setFileUrl(generatedUrl);
                            dto.setLegalFile(legalFileDto);
                        }

                        return dto;
                    })
                    .toList();
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
            final LegalFile existingFile = legalFileService.fetchByMstFileTypeAndCust(CustomerUtils.authenticateCustomer(authentication), mstFileType);
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
            if (code != null && file != null && file.getOriginalFilename() != null) {
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

    public ResponseEntity<Resource> documentByLegalFileId(
            HttpServletRequest httpServletRequest,
            Long id
    ) {
        try {
            HttpHeaders headers = new HttpHeaders();
            LegalFile legalFile = legalFileService.findByFileId(Long.valueOf(id));
            if (legalFile == null) {
                headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
                return new ResponseEntity<>(null, headers, HttpStatus.OK);
            }
            if (legalFile.getFilePath().contains("http")) {
                headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
                return new ResponseEntity<>(null, headers, HttpStatus.OK);
            }

            return fileStorageService.downloadUploadFile(
                    httpServletRequest,
                    legalFile.getFilePath(),
                    legalFile.getFileName()
            );
        } catch (Exception e) {
            log.error("documentByLegalFileId, error {}", e.getMessage());
            throw e;
        }
    }

    public void mappingFromInquiryVendor(
            Customer customer,
            InquiryVendorRemoteDto vendor
    ) {
        try {
            if (vendor == null) {
                return;
            }

            List<LegalFile> legalFiles = new ArrayList<>();
            if (!StringUtil.isNullOrEmpty(vendor.getAktaPendirianLink())) {
                legalFileRepository.findTopByFileNameOrderByFileIdDesc("Akta Pendirian")
                        .ifPresent(legalFileRepository::delete);
                mstFileTypeRepository.findTopByFileTypeNameOrderByFileTypeIdDesc("Akta Pendirian")
                        .ifPresent(mstFileTypeRepository::delete);

                MstFileType m1 = MstFileType.builder()
                        .fileTypeCode("APN01")
                        .fileTypeName("Akta Pendirian")
                        .fileTypeDesc("Akta Pendirian")
                        .fileAllocation("Financing")
                        .usrCrt("system")
                        .dtmCrt(Instant.now())
                        .build();
                mstFileTypeRepository.save(m1);

                legalFiles.add(
                        LegalFile.builder()
                                .custCode(customer)
                                .fileName("Akta Pendirian")
                                .filePath(vendor.getAktaPendirianLink())
                                .contentType("")
                                .fileTypeCode(m1)
                                .usrCrt("system")
                                .dtmCrt(Instant.now())
                                .build()
                );
            }

            if (!StringUtil.isNullOrEmpty(vendor.getAktaPerubahanLink())) {
                legalFileRepository.findTopByFileNameOrderByFileIdDesc("Akta Perubahan Terakhir Lainnya")
                        .ifPresent(legalFileRepository::delete);
                mstFileTypeRepository.findTopByFileTypeNameOrderByFileTypeIdDesc("Akta Perubahan Terakhir Lainnya")
                        .ifPresent(mstFileTypeRepository::delete);

                MstFileType m2 = MstFileType.builder()
                        .fileTypeCode("APTL01")
                        .fileTypeName("Akta Perubahan Terakhir Lainnya")
                        .fileTypeDesc("Akta Perubahan Terakhir Lainnya")
                        .fileAllocation("Financing")
                        .usrCrt("system")
                        .dtmCrt(Instant.now())
                        .build();
                mstFileTypeRepository.save(m2);

                legalFiles.add(
                        LegalFile.builder()
                                .custCode(customer)
                                .fileName("Akta Perubahan Terakhir Lainnya")
                                .filePath(vendor.getAktaPerubahanLink())
                                .contentType("")
                                .fileTypeCode(m2)
                                .usrCrt("system")
                                .dtmCrt(Instant.now())
                                .build()
                );
            }

            if (!StringUtil.isNullOrEmpty(vendor.getPengesahanKemenkumhamLink())) {
                legalFileRepository.findTopByFileNameOrderByFileIdDesc("Sk Persetujuan Kemenkumham")
                        .ifPresent(legalFileRepository::delete);
                mstFileTypeRepository.findTopByFileTypeNameOrderByFileTypeIdDesc("Sk Persetujuan Kemenkumham")
                        .ifPresent(mstFileTypeRepository::delete);

                MstFileType m3 = MstFileType.builder()
                        .fileTypeCode("SKPK01")
                        .fileTypeName("Sk Persetujuan Kemenkumham")
                        .fileTypeDesc("Sk Persetujuan Kemenkumham")
                        .fileAllocation("Financing")
                        .usrCrt("system")
                        .dtmCrt(Instant.now())
                        .build();
                mstFileTypeRepository.save(m3);

                legalFiles.add(
                        LegalFile.builder()
                                .custCode(customer)
                                .fileName("Sk Persetujuan Kemenkumham")
                                .filePath(vendor.getPengesahanKemenkumhamLink())
                                .contentType("")
                                .fileTypeCode(m3)
                                .usrCrt("system")
                                .dtmCrt(Instant.now())
                                .build()
                );
            }

            if (!StringUtil.isNullOrEmpty(vendor.getNpwpLink())) {
                legalFileRepository.findTopByFileNameOrderByFileIdDesc("NPWP")
                        .ifPresent(legalFileRepository::delete);
                mstFileTypeRepository.findTopByFileTypeNameOrderByFileTypeIdDesc("NPWP")
                        .ifPresent(mstFileTypeRepository::delete);

                MstFileType m4 = MstFileType.builder()
                        .fileTypeCode("NPWP01")
                        .fileTypeName("NPWP")
                        .fileTypeDesc("NPWP")
                        .fileAllocation("Legal")
                        .usrCrt("system")
                        .dtmCrt(Instant.now())
                        .build();
                mstFileTypeRepository.save(m4);

                legalFiles.add(
                        LegalFile.builder()
                                .custCode(customer)
                                .fileName("NPWP")
                                .fileNo(vendor.getNpwp())
                                .filePath(vendor.getNpwpLink())
                                .contentType("")
                                .fileTypeCode(m4)
                                .usrCrt("system")
                                .dtmCrt(Instant.now())
                                .build()
                );
            }

            if (!StringUtil.isNullOrEmpty(vendor.getNipSiupLink())) {
                legalFileRepository.findTopByFileNameOrderByFileIdDesc("NIB")
                        .ifPresent(legalFileRepository::delete);
                mstFileTypeRepository.findTopByFileTypeNameOrderByFileTypeIdDesc("NIB")
                        .ifPresent(mstFileTypeRepository::delete);

                MstFileType m5 = MstFileType.builder()
                        .fileTypeCode("NIB01")
                        .fileTypeName("NIB")
                        .fileTypeDesc("NIB")
                        .fileAllocation("Legal")
                        .usrCrt("system")
                        .dtmCrt(Instant.now())
                        .build();
                mstFileTypeRepository.save(m5);

                legalFiles.add(
                        LegalFile.builder()
                                .custCode(customer)
                                .fileName("NIB")
                                .fileNo(vendor.getNipSiup())
                                .filePath(vendor.getNipSiupLink())
                                .contentType("")
                                .fileTypeCode(m5)
                                .usrCrt("system")
                                .dtmCrt(Instant.now())
                                .build()
                );
            }

            if (!StringUtil.isNullOrEmpty(vendor.getPkpLink())) {
                legalFileRepository.findTopByFileNameOrderByFileIdDesc("PKP")
                        .ifPresent(legalFileRepository::delete);
                mstFileTypeRepository.findTopByFileTypeNameOrderByFileTypeIdDesc("PKP")
                        .ifPresent(mstFileTypeRepository::delete);

                MstFileType m6 = MstFileType.builder()
                        .fileTypeCode("PKP01")
                        .fileTypeName("PKP")
                        .fileTypeDesc("PKP")
                        .fileAllocation("Legal")
                        .usrCrt("system")
                        .dtmCrt(Instant.now())
                        .build();
                mstFileTypeRepository.save(m6);

                legalFiles.add(
                        LegalFile.builder()
                                .custCode(customer)
                                .fileName("PKP")
                                .fileNo(vendor.getPkpNumber())
                                .filePath(vendor.getPkpLink())
                                .contentType("")
                                .fileTypeCode(m6)
                                .usrCrt("system")
                                .dtmCrt(Instant.now())
                                .build()
                );
            }

            if (!StringUtil.isNullOrEmpty(vendor.getKtpNpwpVendorStockLink())) {
                legalFileRepository.findTopByFileNameOrderByFileIdDesc("Identitas Pengurus")
                        .ifPresent(legalFileRepository::delete);
                legalFileRepository.findTopByFileNameOrderByFileIdDesc("IPS")
                        .ifPresent(legalFileRepository::delete);
                mstFileTypeRepository.findTopByFileTypeNameOrderByFileTypeIdDesc("IPS")
                        .ifPresent(mstFileTypeRepository::delete);
                mstFileTypeRepository.findTopByFileTypeNameOrderByFileTypeIdDesc("Identitas Pengurus")
                        .ifPresent(mstFileTypeRepository::delete);


                MstFileType m7 = MstFileType.builder()
                        .fileTypeCode("IPS01")
                        .fileTypeName("Identitas Pengurus")
                        .fileTypeDesc("Identitas Pengurus")
                        .fileAllocation("Legal")
                        .usrCrt("system")
                        .dtmCrt(Instant.now())
                        .build();
                mstFileTypeRepository.save(m7);

                legalFiles.add(
                        LegalFile.builder()
                                .custCode(customer)
                                .fileName("Identitas Pengurus")
                                .fileNo(vendor.getKtpNpwpVendorStockId())
                                .filePath(vendor.getKtpNpwpVendorStockLink())
                                .contentType("")
                                .fileTypeCode(m7)
                                .usrCrt("system")
                                .dtmCrt(Instant.now())
                                .build()
                );
            }

            if (!StringUtil.isNullOrEmpty(vendor.getLaporanKeuanganLink())) {
                legalFileRepository.findTopByFileNameOrderByFileIdDesc("Laporan Keuangan")
                        .ifPresent(legalFileRepository::delete);
                mstFileTypeRepository.findTopByFileTypeNameOrderByFileTypeIdDesc("Laporan Keuangan")
                        .ifPresent(mstFileTypeRepository::delete);

                MstFileType m8 = MstFileType.builder()
                        .fileTypeCode("LKN01")
                        .fileTypeName("Laporan Keuangan")
                        .fileTypeDesc("Laporan Keuangan")
                        .fileAllocation("Financing")
                        .usrCrt("system")
                        .dtmCrt(Instant.now())
                        .build();
                mstFileTypeRepository.save(m8);

                legalFiles.add(
                        LegalFile.builder()
                                .custCode(customer)
                                .fileName("Laporan Keuangan")
                                //.fileNo(vendor.getKtpNpwpVendorStockId())
                                .filePath(vendor.getLaporanKeuanganLink())
                                .contentType("")
                                .fileTypeCode(m8)
                                .usrCrt("system")
                                .dtmCrt(Instant.now())
                                .build()
                );
            }

            if (!StringUtil.isNullOrEmpty(vendor.getKtpDirekturLink())) {
                legalFileRepository.findTopByFileNameOrderByFileIdDesc("Ktp Pengurus")
                        .ifPresent(legalFileRepository::delete);
                mstFileTypeRepository.findTopByFileTypeNameOrderByFileTypeIdDesc("Ktp Pengurus")
                        .ifPresent(mstFileTypeRepository::delete);

                MstFileType m9 = MstFileType.builder()
                        .fileTypeCode("KPS01")
                        .fileTypeName("Ktp Pengurus")
                        .fileTypeDesc("Ktp Pengurus")
                        .fileAllocation("Legal")
                        .usrCrt("system")
                        .dtmCrt(Instant.now())
                        .build();
                mstFileTypeRepository.save(m9);

                legalFiles.add(
                        LegalFile.builder()
                                .custCode(customer)
                                .fileName("Ktp Pengurus")
                                .fileNo(vendor.getKtpDirectur())
                                .filePath(vendor.getKtpDirekturLink())
                                .contentType("")
                                .fileTypeCode(m9)
                                .usrCrt("system")
                                .dtmCrt(Instant.now())
                                .build()
                );
            }

            if (
                    vendor.getBankDetail() != null &&
                            !vendor.getBankDetail().isEmpty()
                            && vendor.getBankDetail().getFirst().getDocLink() != null
            ) {
                legalFileRepository.findTopByFileNameOrderByFileIdDesc("Bank Detail")
                        .ifPresent(legalFileRepository::delete);
                mstFileTypeRepository.findTopByFileTypeNameOrderByFileTypeIdDesc("Bank Detail")
                        .ifPresent(mstFileTypeRepository::delete);

                MstFileType m10 = MstFileType.builder()
                        .fileTypeCode("BDL01")
                        .fileTypeName("Bank Detail")
                        .fileTypeDesc("Bank Detail")
                        .fileAllocation("Financing")
                        .usrCrt("system")
                        .dtmCrt(Instant.now())
                        .build();
                mstFileTypeRepository.save(m10);

                legalFiles.add(
                        LegalFile.builder()
                                .custCode(customer)
                                .fileName("Bank Detail")
                                //.fileNo(vendor.getKtpDirectur())
                                .filePath(vendor.getBankDetail().getFirst().getDocLink().toString())
                                .contentType("")
                                .fileTypeCode(m10)
                                .usrCrt("system")
                                .dtmCrt(Instant.now())
                                .build()
                );
            }

            if (
                    vendor.getOtherDocument() != null
                            && !vendor.getOtherDocument().isEmpty()
            ) {
                int index = 1;
                for (InquiryVendorRemoteDto.OtherDocument doc : vendor.getOtherDocument()) {
                    legalFileRepository.findTopByFileNameOrderByFileIdDesc(doc.getDocumentName())
                            .ifPresent(legalFileRepository::delete);
                    mstFileTypeRepository.findTopByFileTypeNameOrderByFileTypeIdDesc(doc.getDocumentName())
                            .ifPresent(mstFileTypeRepository::delete);

                    MstFileType m11 = MstFileType.builder()
                            .fileTypeCode("DOCOTHER_0" + index)
                            .fileTypeName(doc.getDocumentName())
                            .fileTypeDesc(doc.getDocumentName())
                            .fileAllocation("Legal")
                            .usrCrt("system")
                            .dtmCrt(Instant.now())
                            .build();
                    mstFileTypeRepository.save(m11);

                    legalFiles.add(
                            LegalFile.builder()
                                    .custCode(customer)
                                    .fileName(doc.getDocumentName())
                                    .fileNo(doc.getDocumentNo())
                                    .filePath(doc.getDocumentUrl())
                                    .contentType("")
                                    .fileTypeCode(m11)
                                    .usrCrt("system")
                                    .dtmCrt(Instant.now())
                                    .build()
                    );
                    index++;
                }
            }

            if (!legalFiles.isEmpty()) {
                legalFileRepository.saveAll(legalFiles);
            }

        } catch (Exception e) {
            log.error("mappingFromInquiryVendor, error {}", e.getMessage());
            throw e;
        }
    }

    public PaginationResult<LegalFileDto> uploadedCustomerDoc(
            String custCode,
            HttpServletRequest httpServletRequest,
            PaginationRequest request
    ) {
        try {
            int pageNo = 0, pageSize = 10;

            if (request.getPageNo() != null) {
                pageNo = request.getPageNo();
            }
            if (request.getPageSize() != null) {
                pageSize = request.getPageSize();
            }

            if (pageNo > 0) {
                pageNo = pageNo - 1;
            }

            Customer customer = customerRepository.findByCustCode(UUID.fromString(custCode))
                    .orElseThrow(
                            () -> new IllegalStateException("Customer not found")
                    );

            Page<LegalFile> paginate = legalFileRepository.findAllByCustCodeOrderByFileName(
                    customer,
                    PageRequest.of(pageNo, pageSize, Sort.by(Sort.Direction.DESC, "fileName"))
            );

            List<LegalFileDto> result = paginate
                    .stream()
                    .map((legal) -> {
                        LegalFileDto legalFileDto = FileTypeMapper.INSTANCE.legalFileToDto(legal);
                        legalFileDto.setUploadedDate(legal.getDtmUpd());
                        String generatedUrl = fileUlr(
                                httpServletRequest,
                                legal
                        );

                        if (legal.getFilePath() != null && legal.getFilePath().contains("http")) {
                            generatedUrl = legal.getFilePath();
                        }

                        legalFileDto.setFileUrl(generatedUrl);
                        return legalFileDto;
                    })
                    .toList();

            return PaginationResult.<LegalFileDto>builder()
                    .currentPage(pageNo + 1)
                    .totalData(paginate.getTotalElements())
                    .totalPage(paginate.getTotalPages())
                    .list(result)
                    .build();
        } catch (Exception e) {
            log.error("uploadedCustomerDoc: error {}", e.getMessage());
            throw e;
        }
    }

    private String fileUlr(HttpServletRequest httpServletRequest, LegalFile legalFile) {
        return UriUtils.getBaseUrl(httpServletRequest)
                + "/api/v1"
                + "/documents/download/loan"
                + "/"
                + legalFile.getFileId()
                + "?token="
                + HttpUtils.getHeaderBearerToken(httpServletRequest);
    }
}
