package com.kmkbe.modules.loan_submission.service;

import com.kmkbe.core.domain.constant.AuditAction;
import com.kmkbe.core.domain.dto.DocumentTemplateFinancingDto;
import com.kmkbe.core.domain.dto.InquiryVendorRemoteDto;
import com.kmkbe.core.domain.dto.LegalFileDto;
import com.kmkbe.core.domain.dto.MstFileTypeDto;
import com.kmkbe.core.domain.entity.*;
import com.kmkbe.core.domain.mapper.FileTypeMapper;
import com.kmkbe.core.domain.model.PaginationResult;
import com.kmkbe.core.domain.repository.*;
import com.kmkbe.core.domain.request.PaginationRequest;
import com.kmkbe.core.service.FileStorageService;
import com.kmkbe.core.utils.DateTimeUtils;
import com.kmkbe.core.utils.UriUtils;
import com.kmkbe.modules.common.service.AuditTrailService;
import com.kmkbe.modules.customer.model.entity.Customer;
import com.kmkbe.modules.customer.repository.CustomerRepository;
import com.kmkbe.modules.remote.service.CustomerRemoteService;
import com.kmkbe.helpers.utils.SpecPagination;
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
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.SignatureException;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentService {
  private final AgreementFileRepository agreementFileRepository;
  private final MstFileTypeRepository mstFileTypeRepository;
  private final LegalFileRepository legalFileRepository;
  private final FileStorageService fileStorageService;
  private final LegalFileService legalFileService;
  private final CustomerRemoteService customerRemoteService;
  private final CustomerRepository customerRepository;
  private final FinancingHdrRepository financingHdrRepository;
  private final AuditTrailService auditTrailService;

  public List<DocumentTemplateFinancingDto> fetchDocumentTemplateFinancing(
    Customer customer
  ) throws Exception {
    try {
      return Arrays.asList(
        DocumentTemplateFinancingDto.builder()
          .fileName("Surat Instruksi Transfer (SI)")
          .fileUrl("/admin/document-sit/" + customer.getCustCode())
          .build(),
        DocumentTemplateFinancingDto.builder()
          .fileName("Formulir Aplikasi Pembiayaan")
          .fileUrl("/admin/document-fap/" + customer.getCustCode())
          .build()
      );
    } catch (Exception e) {
      log.error("fetchDocumentTemplateFinancing: {}", e.getMessage());
      throw e;
    }
  }

//    public PaginationResult<MstFileTypeDto> fetchAllLoanDocumentRequirement(
//            HttpServletRequest httpServletRequest,
//            Authentication authentication,
//            PaginationRequest request,
//            Boolean isFirst
//    ) throws SignatureException {
//        try {
//            if (isFirst != null && isFirst) {
//                //fetchAndMappingDocVendor(authentication);
//            }
//
//            int pageNo = 0, pageSize = 10;
//
//            if (request.getPageNo() != null) {
//                pageNo = request.getPageNo();
//            }
//            if (request.getPageSize() != null) {
//                pageSize = request.getPageSize();
//            }
//
//            if (pageNo > 0) {
//                pageNo = pageNo - 1;
//            }
//            List<String> fileAllocation = new ArrayList<>();
//            if (String.valueOf(httpServletRequest.getParameter("owner")).equalsIgnoreCase("debitur")){
//                fileAllocation = List.of(
//                        "Legal",
//                        "Financing"
//                );
//            }else{
//                fileAllocation = List.of(
//                        "internal",
//                        "Legal",
//                        "Financing"
//                );
//            }
//
//            Page<MstFileType> page = mstFileTypeRepository.findAllByFileAllocationInOrderByFileTypeIdDesc(
//                    fileAllocation,
//                    PageRequest.of(
//                            pageNo,
//                            pageSize,
//                            Sort.by("fileTypeId").descending()
//                    )
//            );
//
//            List<MstFileTypeDto> result = page.map((file) -> {
//                        MstFileTypeDto dto = FileTypeMapper.INSTANCE.mstFileToDto(file);
//                        LegalFile legalFile = null;
//
//                        try {
//                            legalFile = legalFileService.fetchByMstFileTypeAndCust(CustomerUtils.authenticateCustomer(authentication), file);
//                        } catch (Exception e) {
//                            log.error("fetchByCust, error {}", e.getMessage());
//                        }
//
//                        if (legalFile != null) {
//                            LegalFileDto legalFileDto = FileTypeMapper.INSTANCE.legalFileToDto(legalFile);
//                            legalFileDto.setUploadedDate(legalFile.getDtmUpd());
//
//                            // Jika fileTypeCode adalah DOC005 atau DOC006, jangan generate URL
//                            if (!"DOC005".equals(file.getFileTypeCode()) && !"DOC006".equals(file.getFileTypeCode())) {
//                                String generatedUrl = UriUtils.fileUlr(
//                                        httpServletRequest,
//                                        Math.toIntExact(legalFile.getFileId()),
//                                        UriUtils.DocType.loan
//                                );
//
//                                if (legalFile.getFilePath() != null && legalFile.getFilePath().contains("http")) {
//                                    generatedUrl = legalFile.getFilePath();
//                                }
//
//                                legalFileDto.setFileUrl(generatedUrl);
//                            } else {
//                                legalFileDto.setFileUrl(null); // Jangan buat URL
//                            }
//
//                            dto.setLegalFile(legalFileDto);
//

  /// /                        if (legalFile != null) {
  /// /                            LegalFileDto legalFileDto = FileTypeMapper.INSTANCE.legalFileToDto(legalFile);
  /// /                            legalFileDto.setUploadedDate(legalFile.getDtmUpd());
  /// /
  /// /                            String generatedUrl = UriUtils.fileUlr(
  /// /                                    httpServletRequest,
  /// /                                    Math.toIntExact(legalFile.getFileId()),
  /// /                                    UriUtils.DocType.loan
  /// /                            );
  /// /
  /// /                            if (legalFile.getFilePath() != null && legalFile.getFilePath().contains("http")) {
  /// /                                try {
  /// /                                    URI uri = new URI(legalFile.getFilePath());
  /// /                                    uri = new URI("https", UriUtils.getDomainUrl(httpServletRequest), uri.getPath(), uri.getFragment());
  /// /                                    //generatedUrl = uri.toString();//byapass
  /// /                                    generatedUrl = legalFile.getFilePath();
  /// /                                } catch (URISyntaxException e) {
  /// /                                    generatedUrl = legalFile.getFilePath();
  /// /                                }
  /// /                            }
  /// /
  /// /                            legalFileDto.setFileUrl(generatedUrl);
  /// /                            dto.setLegalFile(legalFileDto);
//                        }
//
//                        return dto;
//                    })
//                    .toList();
//
//            List<MstFileTypeDto> resultSorted = new ArrayList<>();
//            if (String.valueOf(httpServletRequest.getParameter("owner")).equalsIgnoreCase("debitur")) {
//                //sorting
//                for (MstFileTypeDto dto : result) {
//                    if (dto.getLegalFile()!=null && dto.getLegalFile().getUploadedDate()!=null){
//                        //siudah updaload
//                    }else{
//                        resultSorted.add(dto);
//                    }
//                }
//                for (MstFileTypeDto dto : result) {
//                    if (dto.getLegalFile()!=null && dto.getLegalFile().getUploadedDate()!=null){
//                        resultSorted.add(dto);
//                    }
//                }
//            }else{
//                resultSorted = result;
//            }
//
//            return PaginationResult.<MstFileTypeDto>builder()
//                    .currentPage(pageNo + 1)
//                    .totalData(page.getTotalElements())
//                    .totalPage(page.getTotalPages())
//                    .list(resultSorted)
//                    .build();
//        } catch (Exception e) {
//            log.error("getAllLoanDocumentRequirement: {}", e.getMessage());
//            throw e;
//        }
//    }
  public PaginationResult<MstFileTypeDto> fetchAllLoanDocumentRequirement(
    HttpServletRequest httpServletRequest,
    Customer customer,
    PaginationRequest request,
    Boolean isFirst
  ) throws SignatureException {
    try {
      if (isFirst != null && isFirst) {
        //fetchAndMappingDocVendor(authentication);
      }

      List<String> fileAllocation = new ArrayList<>();
      if ("debitur".equalsIgnoreCase(String.valueOf(httpServletRequest.getParameter("owner")))) {
        fileAllocation = List.of("Legal", "Financing");
      } else {
        fileAllocation = List.of("internal", "Legal", "Financing");
      }

      // Fetch ALL data tanpa paging
      List<MstFileType> allData = mstFileTypeRepository.findAllByFileAllocationInOrderByFileTypeIdDesc(fileAllocation);

      // Mapping ke DTO
      List<MstFileTypeDto> result = allData.stream().map((file) -> {
          MstFileTypeDto dto = FileTypeMapper.INSTANCE.mstFileToDto(file);
          LegalFile legalFile = null;

          try {
            legalFile = legalFileService.fetchByMstFileTypeAndCust(customer, file);
          } catch (Exception e) {
            log.error("fetchByCust, error {}", e.getMessage());
          }

          if (legalFile != null) {
            LegalFileDto legalFileDto = FileTypeMapper.INSTANCE.legalFileToDto(legalFile);
            legalFileDto.setUploadedDate(legalFile.getDtmUpd());

            if (!"DOC005".equals(file.getFileTypeCode()) && !"DOC006".equals(file.getFileTypeCode())) {
              String generatedUrl = UriUtils.fileUlr(
                httpServletRequest,
                Math.toIntExact(legalFile.getFileId()),
                UriUtils.DocType.loan
              );

              if (legalFile.getFilePath() != null && legalFile.getFilePath().contains("http")) {
                generatedUrl = legalFile.getFilePath();
              }

              legalFileDto.setFileUrl(generatedUrl);
            } else {
              legalFileDto.setFileUrl(null);
            }

            dto.setLegalFile(legalFileDto);
          }

          return dto;
        })
        .toList();

      // Baru lakukan filtering + pagination di SpecPagination
      return SpecPagination.paginationData(new SpecPagination<MstFileTypeDto, MstFileTypeDto>(result, request) {
        @Override
        public MstFileTypeDto search(MstFileTypeDto data) {
          if (isSearchBy("fileTypeDesc") && like(data.getFileTypeDesc())) {
            return data;
          } else if (isSearchBy("legalFile.fileName") && data.getLegalFile() != null && like(data.getLegalFile().getFileName())) {
            return data;
          }
          return null;
        }

        @Override
        public MstFileTypeDto eval(MstFileTypeDto data) {
          return data;
        }
      });

    } catch (Exception e) {
      log.error("getAllLoanDocumentRequirement: {}", e.getMessage(), e);
      throw e;
    }
  }


  @Transactional
  public LegalFileDto uploadLoanDocument(
    HttpServletRequest httpServletRequest,
    Customer customer,
    MultipartFile file,
    String fileTypeCode
  ) throws Exception {
    String code = null;
    try {
      final MstFileType mstFileType = mstFileTypeRepository.findByFileTypeCode(fileTypeCode).orElseThrow(
        () -> new IllegalStateException("File type not found")
      );
      final LegalFile existingFile = legalFileService.fetchByMstFileTypeAndCust(customer, mstFileType);
      code = customer.getCustName();

           /* String requireExt = FileUtils.getFileNameExtension(mstFileType.getFileTypeName());
            if (!StringUtil.isNullOrEmpty(requireExt)) {
                requireExt = requireExt.toLowerCase();
            }*/

      final String uploadDir = customer.getCustCode() + "/loan_submission";
      final String uploadName = mstFileType.getFileTypeCode() + "_" + file.getOriginalFilename();
      final String uploadedPath = fileStorageService.save(
        file,
        uploadDir,
        uploadName,//,
        null
      );

      LegalFileDto dto = legalFileService.create(
        httpServletRequest,
        customer,
        mstFileType,
        file,
        uploadedPath,
        uploadName
      );
      auditTrailService.record(
        "DOCUMENT",
        AuditAction.UPLOAD,
        "LegalFile",
        dto.getFileId(),
        toLegalFileAuditData(existingFile),
        new LegalFileAuditData(
          dto.getFileId(),
          customer.getCustCode(),
          customer.getCustName(),
          mstFileType.getFileTypeCode(),
          mstFileType.getFileTypeName(),
          dto.getFileName(),
          uploadedPath,
          file.getContentType(),
          dto.getUploadedDate()
        )
      );
//FileUtils.getFilePathFromFullPath(uploadedPath)
      if (existingFile != null && !existingFile.getFilePath().contains("http")) {
        //fileStorageService.delete(existingFile.getFilePath() + "/" + existingFile.getFileName(), "");
      }

      return dto;
    } catch (Exception e) {
      if (code != null && file != null && file.getOriginalFilename() != null) {
        //fileStorageService.delete(file.getOriginalFilename(), code);
      }

      log.error("uploadLoanDocument, error {}", e.getMessage());
      throw e;
    }
  }

  public void delete(Long id) {
    try {
      LegalFile legalFile = legalFileService.findByFileId(id);
      legalFileService.delete(id);
      auditTrailService.record("DOCUMENT", AuditAction.DELETE, "LegalFile", id, toLegalFileAuditData(legalFile), null);
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
      if (String.valueOf(httpServletRequest.getParameter("cd")).equalsIgnoreCase("attachment")) {
        headers.set("Content-Disposition", "attachment; filename=sample.pdf");
      } else {
        headers.set("Content-Disposition", "inline");
      }
      LegalFile legalFile = legalFileService.findByFileId(Long.valueOf(id));
      if (legalFile == null) {
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        return new ResponseEntity<>(null, headers, HttpStatus.OK);
      }
      if (legalFile.getFilePath().contains("http")) {
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        auditTrailService.record("DOCUMENT", AuditAction.DOWNLOAD, "LegalFile", legalFile.getFileId(), null, toLegalFileAuditData(legalFile));
        return new ResponseEntity<>(null, headers, HttpStatus.OK);
      }

      ResponseEntity<Resource> response = fileStorageService.downloadUploadFile(
        httpServletRequest,
        legalFile.getFilePath(),
        legalFile.getFileName(),
        String.valueOf(httpServletRequest.getParameter("cd"))
      );
      auditTrailService.record("DOCUMENT", AuditAction.DOWNLOAD, "LegalFile", legalFile.getFileId(), null, toLegalFileAuditData(legalFile));
      return response;
    } catch (Exception e) {
      log.error("documentByLegalFileId, error {}", e.getMessage());
      throw e;
    }
  }

  public ResponseEntity<Resource> agreementDocByAgreementId(
    HttpServletRequest httpServletRequest,
    Long id
  ) {
    try {
      HttpHeaders headers = new HttpHeaders();
      AgreementFile agreementFile = agreementFileRepository.findById(id).orElse(null);
      if (agreementFile == null) {
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        return new ResponseEntity<>(null, headers, HttpStatus.OK);
      }

      if (agreementFile.getFilePath().contains("http")) {
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        auditTrailService.record("DOCUMENT", AuditAction.DOWNLOAD, "AgreementFile", agreementFile.getAgreementFileId(), null, toAgreementFileAuditData(agreementFile));
        return new ResponseEntity<>(null, headers, HttpStatus.OK);
      }

      ResponseEntity<Resource> response = fileStorageService.downloadUploadFile(
        httpServletRequest,
        agreementFile.getFilePath(),
        agreementFile.getFileName(), ""
      );
      auditTrailService.record("DOCUMENT", AuditAction.DOWNLOAD, "AgreementFile", agreementFile.getAgreementFileId(), null, toAgreementFileAuditData(agreementFile));
      return response;
    } catch (Exception e) {
      log.error("agreementDocByAgreementId, error {}", e.getMessage());
      throw e;
    }
  }

  private void fetchAndMappingDocVendor(Customer customer) throws SignatureException {
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
  }

  public void mappingFromInquiryVendor(
    Customer customer,
    InquiryVendorRemoteDto vendor
  ) {
    try {
      if (vendor == null) {
        return;
      }

      if (!StringUtil.isNullOrEmpty(vendor.getAktaPendirianLink())) {
        List<LegalFile> exists = legalFileRepository.findAllRawByCustAndFileTypeCodeStr(
          "APN01",
          customer.getCustCode().toString()
        );

        if (!exists.isEmpty()) {
          legalFileRepository.deleteAll(exists);
        }

        final MstFileType m1;
        Optional<MstFileType> findMst =
          mstFileTypeRepository.findTopByFileTypeNameOrderByFileTypeIdDesc("Akta Pendirian");
        if (findMst.isEmpty()) {
          m1 = MstFileType.builder()
            .fileTypeCode("APN01")
            .fileTypeName("Akta Pendirian")
            .fileTypeDesc("Akta Pendirian")
            .fileAllocation("Financing")
            .usrCrt("system")
            .dtmCrt(DateTimeUtils.now())
            .build();
          mstFileTypeRepository.save(m1);
        } else {
          m1 = findMst.get();
        }

        legalFileRepository.save(LegalFile.builder()
          .custCode(customer)
          .fileName("Akta Pendirian")
          .filePath(vendor.getAktaPendirianLink())
          .contentType("")
          .fileTypeCode(m1)
          .usrCrt("system")
          .dtmCrt(DateTimeUtils.now())
          .build());
      }

      if (!StringUtil.isNullOrEmpty(vendor.getAktaPerubahanLink())) {
        List<LegalFile> exists = legalFileRepository.findAllRawByCustAndFileTypeCodeStr(
          "APTL01",
          customer.getCustCode().toString()
        );

        if (!exists.isEmpty()) {
          legalFileRepository.deleteAll(exists);
        }

        MstFileType m2;
        Optional<MstFileType> findMst = mstFileTypeRepository.findTopByFileTypeNameOrderByFileTypeIdDesc("Akta Perubahan Terakhir Lainnya");
        if (findMst.isEmpty()) {
          m2 = MstFileType.builder()
            .fileTypeCode("APTL01")
            .fileTypeName("Akta Perubahan Terakhir Lainnya")
            .fileTypeDesc("Akta Perubahan Terakhir Lainnya")
            .fileAllocation("Financing")
            .usrCrt("system")
            .dtmCrt(DateTimeUtils.now())
            .build();
          mstFileTypeRepository.save(m2);
        } else {
          m2 = findMst.get();
        }

        legalFileRepository.save(
          LegalFile.builder()
            .custCode(customer)
            .fileName("Akta Perubahan Terakhir Lainnya")
            .filePath(vendor.getAktaPerubahanLink())
            .contentType("")
            .fileTypeCode(m2)
            .usrCrt("system")
            .dtmCrt(DateTimeUtils.now())
            .build()
        );
      }

      if (!StringUtil.isNullOrEmpty(vendor.getPengesahanKemenkumhamLink())) {
        List<LegalFile> exists = legalFileRepository.findAllRawByCustAndFileTypeCodeStr(
          "SKPK01",
          customer.getCustCode().toString()
        );

        if (!exists.isEmpty()) {
          legalFileRepository.deleteAll(exists);
        }

        MstFileType m3;
        Optional<MstFileType> findMst = mstFileTypeRepository.findTopByFileTypeNameOrderByFileTypeIdDesc("Sk Persetujuan Kemenkumham");
        if (findMst.isEmpty()) {
          m3 = MstFileType.builder()
            .fileTypeCode("SKPK01")
            .fileTypeName("Sk Persetujuan Kemenkumham")
            .fileTypeDesc("Sk Persetujuan Kemenkumham")
            .fileAllocation("Financing")
            .usrCrt("system")
            .dtmCrt(DateTimeUtils.now())
            .build();
          mstFileTypeRepository.save(m3);
        } else {
          m3 = findMst.get();
        }

        legalFileRepository.save(
          LegalFile.builder()
            .custCode(customer)
            .fileName("Sk Persetujuan Kemenkumham")
            .filePath(vendor.getPengesahanKemenkumhamLink())
            .contentType("")
            .fileTypeCode(m3)
            .usrCrt("system")
            .dtmCrt(DateTimeUtils.now())
            .build()
        );
      }

      if (!StringUtil.isNullOrEmpty(vendor.getNpwpLink())) {
        List<LegalFile> exists = legalFileRepository.findAllRawByCustAndFileTypeCodeStr(
          "NPWP01",
          customer.getCustCode().toString()
        );

        if (!exists.isEmpty()) {
          legalFileRepository.deleteAll(exists);
        }

        MstFileType m4;
        Optional<MstFileType> findMst = mstFileTypeRepository.findTopByFileTypeNameOrderByFileTypeIdDesc("NPWP");
        if (findMst.isEmpty()) {
          m4 = MstFileType.builder()
            .fileTypeCode("NPWP01")
            .fileTypeName("NPWP")
            .fileTypeDesc("NPWP")
            .fileAllocation("Legal")
            .usrCrt("system")
            .dtmCrt(DateTimeUtils.now())
            .build();
          mstFileTypeRepository.save(m4);
        } else {
          m4 = findMst.get();
        }

        legalFileRepository.save(
          LegalFile.builder()
            .custCode(customer)
            .fileName("NPWP")
            .fileNo(vendor.getNpwp())
            .filePath(vendor.getNpwpLink())
            .contentType("")
            .fileTypeCode(m4)
            .usrCrt("system")
            .dtmCrt(DateTimeUtils.now())
            .build()
        );
      }

      if (!StringUtil.isNullOrEmpty(vendor.getNipSiupLink())) {
        List<LegalFile> exists = legalFileRepository.findAllRawByCustAndFileTypeCodeStr(
          "NIB01",
          customer.getCustCode().toString()
        );

        if (!exists.isEmpty()) {
          legalFileRepository.deleteAll(exists);
        }

        MstFileType m5;
        Optional<MstFileType> findMst = mstFileTypeRepository.findTopByFileTypeNameOrderByFileTypeIdDesc("NIB");
        if (findMst.isEmpty()) {
          m5 = MstFileType.builder()
            .fileTypeCode("NIB01")
            .fileTypeName("NIB")
            .fileTypeDesc("NIB")
            .fileAllocation("Legal")
            .usrCrt("system")
            .dtmCrt(DateTimeUtils.now())
            .build();
          mstFileTypeRepository.save(m5);
        } else {
          m5 = findMst.get();
        }

        legalFileRepository.save(
          LegalFile.builder()
            .custCode(customer)
            .fileName("NIB")
            .fileNo(vendor.getNipSiup())
            .filePath(vendor.getNipSiupLink())
            .contentType("")
            .fileTypeCode(m5)
            .usrCrt("system")
            .dtmCrt(DateTimeUtils.now())
            .build()
        );
      }

      if (!StringUtil.isNullOrEmpty(vendor.getPkpLink())) {
        List<LegalFile> exists = legalFileRepository.findAllRawByCustAndFileTypeCodeStr(
          "PKP01",
          customer.getCustCode().toString()
        );

        if (!exists.isEmpty()) {
          legalFileRepository.deleteAll(exists);
        }

        final MstFileType m6;
        Optional<MstFileType> findMst = mstFileTypeRepository.findTopByFileTypeNameOrderByFileTypeIdDesc("PKP");
        if (findMst.isEmpty()) {
          m6 = MstFileType.builder()
            .fileTypeCode("PKP01")
            .fileTypeName("PKP")
            .fileTypeDesc("PKP")
            .fileAllocation("Legal")
            .usrCrt("system")
            .dtmCrt(DateTimeUtils.now())
            .build();
          mstFileTypeRepository.save(m6);
        } else {
          m6 = findMst.get();
        }

        legalFileRepository.save(
          LegalFile.builder()
            .custCode(customer)
            .fileName("PKP")
            .fileNo(vendor.getPkpNumber())
            .filePath(vendor.getPkpLink())
            .contentType("")
            .fileTypeCode(m6)
            .usrCrt("system")
            .dtmCrt(DateTimeUtils.now())
            .build()
        );
      }

      if (!StringUtil.isNullOrEmpty(vendor.getKtpNpwpVendorStockLink())) {
        List<LegalFile> exists = legalFileRepository.findAllRawByCustAndFileTypeCodeStr(
          "IPS01",
          customer.getCustCode().toString()
        );

        if (!exists.isEmpty()) {
          legalFileRepository.deleteAll(exists);
        }

        Optional<MstFileType> findMst1 = mstFileTypeRepository.findTopByFileTypeNameOrderByFileTypeIdDesc("IPS");
        findMst1.ifPresent(mstFileTypeRepository::delete);

        final MstFileType m7;
        Optional<MstFileType> findMst = mstFileTypeRepository.findTopByFileTypeNameOrderByFileTypeIdDesc("Identitas Pengurus");
        if (findMst.isEmpty()) {
          m7 = MstFileType.builder()
            .fileTypeCode("IPS01")
            .fileTypeName("Identitas Pengurus")
            .fileTypeDesc("Identitas Pengurus")
            .fileAllocation("Legal")
            .usrCrt("system")
            .dtmCrt(DateTimeUtils.now())
            .build();
          mstFileTypeRepository.save(m7);
        } else {
          m7 = findMst.get();
        }

        legalFileRepository.save(
          LegalFile.builder()
            .custCode(customer)
            .fileName("Identitas Pengurus")
            .fileNo(vendor.getKtpNpwpVendorStockId())
            .filePath(vendor.getKtpNpwpVendorStockLink())
            .contentType("")
            .fileTypeCode(m7)
            .usrCrt("system")
            .dtmCrt(DateTimeUtils.now())
            .build()
        );
      }

      if (!StringUtil.isNullOrEmpty(vendor.getLaporanKeuanganLink())) {
        List<LegalFile> exists = legalFileRepository.findAllRawByCustAndFileTypeCodeStr(
          "LKN01",
          customer.getCustCode().toString()
        );

        if (!exists.isEmpty()) {
          legalFileRepository.deleteAll(exists);
        }

        final MstFileType m8;
        Optional<MstFileType> findMst = mstFileTypeRepository.findTopByFileTypeNameOrderByFileTypeIdDesc("Laporan Keuangan");
        if (findMst.isEmpty()) {
          m8 = MstFileType.builder()
            .fileTypeCode("LKN01")
            .fileTypeName("Laporan Keuangan")
            .fileTypeDesc("Laporan Keuangan")
            .fileAllocation("Financing")
            .usrCrt("system")
            .dtmCrt(DateTimeUtils.now())
            .build();
          mstFileTypeRepository.save(m8);
        } else {
          m8 = findMst.get();
        }

        legalFileRepository.save(
          LegalFile.builder()
            .custCode(customer)
            .fileName("Laporan Keuangan")
            //.fileNo(vendor.getKtpNpwpVendorStockId())
            .filePath(vendor.getLaporanKeuanganLink())
            .contentType("")
            .fileTypeCode(m8)
            .usrCrt("system")
            .dtmCrt(DateTimeUtils.now())
            .build()
        );
      }

      if (!StringUtil.isNullOrEmpty(vendor.getKtpDirekturLink())) {
        List<LegalFile> exists = legalFileRepository.findAllRawByCustAndFileTypeCodeStr(
          "KPS01",
          customer.getCustCode().toString()
        );

        if (!exists.isEmpty()) {
          legalFileRepository.deleteAll(exists);
        }

        final MstFileType m9;
        Optional<MstFileType> findMst = mstFileTypeRepository.findTopByFileTypeNameOrderByFileTypeIdDesc("Ktp Pengurus");
        if (findMst.isEmpty()) {
          m9 = MstFileType.builder()
            .fileTypeCode("KPS01")
            .fileTypeName("Ktp Pengurus")
            .fileTypeDesc("Ktp Pengurus")
            .fileAllocation("Legal")
            .usrCrt("system")
            .dtmCrt(DateTimeUtils.now())
            .build();
          mstFileTypeRepository.save(m9);
        } else {
          m9 = findMst.get();
        }

        legalFileRepository.save(
          LegalFile.builder()
            .custCode(customer)
            .fileName("Ktp Pengurus")
            .fileNo(vendor.getKtpDirectur())
            .filePath(vendor.getKtpDirekturLink())
            .contentType("")
            .fileTypeCode(m9)
            .usrCrt("system")
            .dtmCrt(DateTimeUtils.now())
            .build()
        );
      }

      if (
        vendor.getBankDetail() != null &&
          !vendor.getBankDetail().isEmpty()
          && vendor.getBankDetail().getFirst().getDocLink() != null
      ) {
        List<LegalFile> exists = legalFileRepository.findAllRawByCustAndFileTypeCodeStr(
          "BDL01",
          customer.getCustCode().toString()
        );

        if (!exists.isEmpty()) {
          legalFileRepository.deleteAll(exists);
        }

        final MstFileType m10;
        Optional<MstFileType> findMst = mstFileTypeRepository.findTopByFileTypeNameOrderByFileTypeIdDesc("Bank Detail");
        if (findMst.isEmpty()) {
          m10 = MstFileType.builder()
            .fileTypeCode("BDL01")
            .fileTypeName("Bank Detail")
            .fileTypeDesc("Bank Detail")
            .fileAllocation("Financing")
            .usrCrt("system")
            .dtmCrt(DateTimeUtils.now())
            .build();
          mstFileTypeRepository.save(m10);
        } else {
          m10 = findMst.get();
        }


        legalFileRepository.save(
          LegalFile.builder()
            .custCode(customer)
            .fileName("Bank Detail")
            //.fileNo(vendor.getKtpDirectur())
            .filePath(vendor.getBankDetail().getFirst().getDocLink().toString())
            .contentType("")
            .fileTypeCode(m10)
            .usrCrt("system")
            .dtmCrt(DateTimeUtils.now())
            .build()
        );
      }

      if (
        vendor.getOtherDocument() != null
          && !vendor.getOtherDocument().isEmpty()
      ) {
        int index = 1;
        for (InquiryVendorRemoteDto.OtherDocument doc : vendor.getOtherDocument()) {
          List<LegalFile> exists = legalFileRepository.findAllRawByCustAndFileTypeCodeStr(
            "DOCOTHER_0" + index,
            customer.getCustCode().toString()
          );

          if (!exists.isEmpty()) {
            legalFileRepository.deleteAll(exists);
          }

          final MstFileType m11;
          Optional<MstFileType> findMst = mstFileTypeRepository.findTopByFileTypeNameOrderByFileTypeIdDesc(doc.getDocumentName());
          if (findMst.isEmpty()) {
            m11 = MstFileType.builder()
              .fileTypeCode("DOCOTHER_0" + index)
              .fileTypeName(doc.getDocumentName())
              .fileTypeDesc(doc.getDocumentName())
              .fileAllocation("Legal")
              .usrCrt("system")
              .dtmCrt(DateTimeUtils.now())
              .build();
            mstFileTypeRepository.save(m11);
          } else {
            m11 = findMst.get();
          }

          legalFileRepository.save(
            LegalFile.builder()
              .custCode(customer)
              .fileName(doc.getDocumentName())
              .fileNo(doc.getDocumentNo())
              .filePath(doc.getDocumentUrl())
              .contentType("")
              .fileTypeCode(m11)
              .usrCrt("system")
              .dtmCrt(DateTimeUtils.now())
              .build()
          );
          index++;
        }
      }
    } catch (Exception e) {
      log.error("mappingFromInquiryVendor, error {}", e.getMessage());
      throw e;
    }
  }

  private LegalFileAuditData toLegalFileAuditData(LegalFile legalFile) {
    if (legalFile == null) {
      return null;
    }

    Customer customer = legalFile.getCustCode();
    MstFileType fileType = legalFile.getFileTypeCode();
    return new LegalFileAuditData(
      legalFile.getFileId(),
      customer != null ? customer.getCustCode() : null,
      customer != null ? customer.getCustName() : null,
      fileType != null ? fileType.getFileTypeCode() : null,
      fileType != null ? fileType.getFileTypeName() : null,
      legalFile.getFileName(),
      legalFile.getFilePath(),
      legalFile.getContentType(),
      legalFile.getDtmUpd() != null ? legalFile.getDtmUpd() : legalFile.getDtmCrt()
    );
  }

  private AgreementFileAuditData toAgreementFileAuditData(AgreementFile agreementFile) {
    if (agreementFile == null) {
      return null;
    }

    Agreement agreement = agreementFile.getAgreement();
    return new AgreementFileAuditData(
      agreementFile.getAgreementFileId(),
      agreement != null ? agreement.getAgreementCode() : null,
      agreement != null && agreement.getFinancingHdr() != null ? agreement.getFinancingHdr().getFinancingHdrCode() : null,
      agreementFile.getMstFileType() != null ? agreementFile.getMstFileType().getFileTypeCode() : null,
      agreementFile.getFileName(),
      agreementFile.getFilePath(),
      agreementFile.getContentType()
    );
  }

  private record LegalFileAuditData(
    Long fileId,
    UUID custCode,
    String custName,
    String fileTypeCode,
    String fileTypeName,
    String fileName,
    String filePath,
    String contentType,
    java.time.LocalDateTime uploadedDate
  ) {
  }

  private record AgreementFileAuditData(
    Long agreementFileId,
    String agreementCode,
    UUID financingHdrCode,
    String fileTypeCode,
    String fileName,
    String filePath,
    String contentType
  ) {
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
          String generatedUrl = UriUtils.fileUlr(
            httpServletRequest,
            Math.toIntExact(legal.getFileId()),
            UriUtils.DocType.loan
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


  public PaginationResult<MstFileTypeDto> fetchAllLoanDocumentDebitur(
    HttpServletRequest httpServletRequest,
    PaginationRequest request,
    Boolean isFirst,
    String custCode,
    String financingHdrCode
  ) throws SignatureException {
    try {
      if (isFirst != null && isFirst) {
        //fetchAndMappingDocVendor(authentication);
      }

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

      Page<MstFileType> page = mstFileTypeRepository.findAllByFileAllocationInOrderByFileTypeIdDesc(
        List.of("Internal",
          "Legal",
          "Financing"
        ),
        PageRequest.of(
          pageNo,
          pageSize,
          Sort.by("fileTypeId").descending()
        )
      );

      List<MstFileTypeDto> result = page.map((file) -> {
          MstFileTypeDto dto = FileTypeMapper.INSTANCE.mstFileToDto(file);
          LegalFile legalFile = null;

          try {
                           /* Optional<Customer> customer =  customerRepository.findByCustCode(UUID.fromString(custCode));

                            if (customer.isPresent()) {
                                legalFile = legalFileService.fetchByMstFileTypeAndCust(customer.get(), file);

                            }*/

            Optional<FinancingHdr> financingHdr = financingHdrRepository.findByFinancingHdrCode(UUID.fromString(financingHdrCode));
            if (financingHdr.isPresent()) {
              legalFile = legalFileService.fetchByMstFileTypeAndCust(financingHdr.get().getCustomer(), file);

            }
          } catch (Exception e) {
            log.error("fetchByCust, error {}", e.getMessage());
          }


          if (legalFile != null) {
            LegalFileDto legalFileDto = FileTypeMapper.INSTANCE.legalFileToDto(legalFile);
            legalFileDto.setUploadedDate(legalFile.getDtmUpd());

            String generatedUrl = UriUtils.fileUlr(
              httpServletRequest,
              Math.toIntExact(legalFile.getFileId()),
              UriUtils.DocType.loan
            );

            if (legalFile.getFilePath() != null && legalFile.getFilePath().contains("http")) {
              try {
                URI uri = new URI(legalFile.getFilePath());
                uri = new URI("https", UriUtils.getDomainUrl(httpServletRequest), uri.getPath(), uri.getFragment());
                //generatedUrl = uri.toString();
                generatedUrl = legalFile.getFilePath();
              } catch (URISyntaxException e) {
                generatedUrl = legalFile.getFilePath();
              }
            }

            legalFileDto.setFileUrl(generatedUrl);
            dto.setLegalFile(legalFileDto);
          }

          return dto;
        })
        .toList();

      return PaginationResult.<MstFileTypeDto>builder()
        .currentPage(pageNo + 1)
        .totalData(page.getTotalElements())
        .totalPage(page.getTotalPages())
        .list(result)
        .build();
    } catch (Exception e) {
      log.error("getAllLoanDocumentRequirement: {}", e.getMessage());
      throw e;
    }
  }
}
