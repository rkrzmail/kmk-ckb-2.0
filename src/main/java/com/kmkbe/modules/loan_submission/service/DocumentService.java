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
import com.kmkbe.core.security.CurrentUserService;
import com.kmkbe.core.service.FileStorageService;
import com.kmkbe.core.utils.DateTimeUtils;
import com.kmkbe.core.utils.UriUtils;
import com.kmkbe.exception.BusinessException;
import com.kmkbe.helpers.constant.ErrorConstant;
import com.kmkbe.modules.common.service.AuditTrailService;
import com.kmkbe.modules.customer.model.entity.Customer;
import com.kmkbe.modules.customer.repository.CustomerRepository;
import com.kmkbe.modules.remote.service.CustomerRemoteService;
import com.kmkbe.modules.user.entity.MstUser;
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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.net.URISyntaxException;
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
  private final DokumentHistoryUploadRepository dokumentHistoryUploadRepository;
  private final AuditTrailService auditTrailService;
  private final CurrentUserService currentUserService;

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

  public PaginationResult<MstFileTypeDto> fetchAllLoanDocumentRequirement(
    HttpServletRequest httpServletRequest,
    Customer customer,
    PaginationRequest request,
    Boolean isFirst
  ) {
    try {
      if (isFirst != null && isFirst) {
        //fetchAndMappingDocVendor(authentication);
      }

      List<String> fileAllocation;
      if ("debitur".equalsIgnoreCase(String.valueOf(httpServletRequest.getParameter("owner")))) {
        fileAllocation = List.of("Legal", "Financing");
      } else {
        fileAllocation = List.of("internal", "Legal", "Financing");
      }

      // Fetch ALL data tanpa paging
      List<MstFileType> allData = mstFileTypeRepository.findAllByFileAllocationInAndBouwheerCodeOrderByFileTypeIdDesc(fileAllocation, UUID.fromString(customer.getBouwheer()));

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

      return paginateDocuments(result, request);

    } catch (Exception e) {
      log.error("getAllLoanDocumentRequirement: {}", e.getMessage(), e);
      throw e;
    }
  }

  private static boolean hasDocumentQuery(PaginationRequest request) {
    return request.getSearchValue() != null && !request.getSearchValue().isBlank()
      || request.getSortBy() != null && !request.getSortBy().isBlank()
      || request.getSortType() != null && !request.getSortType().isBlank();
  }

  private static PaginationResult<MstFileTypeDto> paginateDocuments(
    List<MstFileTypeDto> documents, PaginationRequest request) {
    int pageNo = request.getPageNo() == null ? 1 : request.getPageNo();
    int pageSize = request.getPageSize() == null ? 10 : request.getPageSize();
    if (pageNo < 1 || pageSize < 1) {
      throw new BusinessException(HttpStatus.BAD_REQUEST, 400, "pageNo dan pageSize harus minimal 1.");
    }

    List<MstFileTypeDto> matching = documents;
    if (request.getSearchValue() != null && !request.getSearchValue().isBlank()) {
      String field = request.getSearchBy() == null ? "" : request.getSearchBy().trim().toLowerCase(Locale.ROOT);
      if (!List.of("filetypecode", "filetypename", "filetypedesc", "legalfile.filename").contains(field)) {
        throw new BusinessException(HttpStatus.BAD_REQUEST, 400, "searchBy dokumen tidak didukung.");
      }
      String needle = request.getSearchValue().trim().toLowerCase(Locale.ROOT);
      matching = documents.stream().filter(document -> {
        String value = switch (field) {
          case "filetypecode" -> document.getFileTypeCode();
          case "filetypename" -> document.getFileTypeName();
          case "filetypedesc" -> document.getFileTypeDesc();
          default -> document.getLegalFile() == null ? null : document.getLegalFile().getFileName();
        };
        return value != null && value.toLowerCase(Locale.ROOT).contains(needle);
      }).toList();
    }

    String sortBy = request.getSortBy();
    String sortType = request.getSortType();
    if (sortType != null && !sortType.isBlank() && (sortBy == null || sortBy.isBlank())) {
      throw new BusinessException(HttpStatus.BAD_REQUEST, 400, "sortBy wajib diisi jika sortType digunakan.");
    }
    if (sortBy != null && !sortBy.isBlank()) {
      Sort.Direction direction;
      try {
        direction = sortType == null || sortType.isBlank()
          ? Sort.Direction.ASC : Sort.Direction.fromString(sortType.trim());
      } catch (IllegalArgumentException exception) {
        throw new BusinessException(HttpStatus.BAD_REQUEST, 400, "sortType harus asc atau desc.");
      }
      Comparator<String> textOrder = direction == Sort.Direction.DESC
        ? String.CASE_INSENSITIVE_ORDER.reversed() : String.CASE_INSENSITIVE_ORDER;
      Comparator<java.time.LocalDateTime> dateOrder = direction == Sort.Direction.DESC
        ? Comparator.reverseOrder() : Comparator.naturalOrder();
      Comparator<Boolean> statusOrder = direction == Sort.Direction.DESC
        ? Comparator.reverseOrder() : Comparator.naturalOrder();
      Comparator<MstFileTypeDto> comparator = switch (sortBy.trim()) {
        case "fileTypeCode" -> Comparator.comparing(MstFileTypeDto::getFileTypeCode, Comparator.nullsLast(textOrder));
        case "fileTypeName" -> Comparator.comparing(MstFileTypeDto::getFileTypeName, Comparator.nullsLast(textOrder));
        case "fileTypeDesc" -> Comparator.comparing(MstFileTypeDto::getFileTypeDesc, Comparator.nullsLast(textOrder));
        case "legalFile.fileName" -> Comparator.comparing(
          document -> document.getLegalFile() == null ? null : document.getLegalFile().getFileName(),
          Comparator.nullsLast(textOrder));
        case "legalFile.uploadedDate" -> Comparator.comparing(
          document -> document.getLegalFile() == null ? null : document.getLegalFile().getUploadedDate(),
          Comparator.nullsLast(dateOrder));
        case "legalFile" -> Comparator.comparing(
          document -> document.getLegalFile() != null && document.getLegalFile().getUploadedDate() != null,
          statusOrder);
        default -> throw new BusinessException(HttpStatus.BAD_REQUEST, 400, "sortBy dokumen tidak didukung.");
      };
      matching = matching.stream().sorted(comparator.thenComparing(
        MstFileTypeDto::getFileTypeCode, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER))).toList();
    }

    long offset = (long) (pageNo - 1) * pageSize;
    int start = (int) Math.min(offset, matching.size());
    int end = (int) Math.min(offset + pageSize, matching.size());
    return PaginationResult.<MstFileTypeDto>builder()
      .currentPage(pageNo)
      .totalData((long) matching.size())
      .totalPage((int) (((long) matching.size() + pageSize - 1) / pageSize))
      .list(matching.subList(start, end))
      .build();
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
      final MstFileType mstFileType = mstFileTypeRepository.findByFileTypeCodeAndBouwheerCode(fileTypeCode, UUID.fromString(customer.getBouwheer())).orElseThrow(
        () -> new IllegalStateException("File type not found")
      );
      final LegalFile existingFile = legalFileService.fetchByMstFileTypeAndCust(customer, mstFileType);
      code = customer.getCustName();
      final String uploadDir = customer.getCustCode() + "/loan_submission";
      final String uploadName = file.getOriginalFilename();
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

  /**
   * Security issue #3 (SECURITY.md): owner/otorisasi download.
   * Customer hanya boleh mengunduh dokumen miliknya sendiri;
   * user internal (admin/branch admin) boleh mengunduh dokumen debitur.
   */
  private void authorizeDownload(LegalFile legalFile) throws AccessDeniedException {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
      throw new AccessDeniedException("Forbidden");
    }

    if (authentication.getPrincipal() instanceof Customer principal) {
      UUID ownerCustCode = legalFile.getCustCode() != null ? legalFile.getCustCode().getCustCode() : null;
      if (ownerCustCode == null || !ownerCustCode.equals(principal.getCustCode())) {
        throw new AccessDeniedException("Forbidden");
      }
      return;
    }

    if (authentication.getPrincipal() instanceof MstUser) {
      return;
    }

    throw new AccessDeniedException("Forbidden");
  }

  public ResponseEntity<Resource> documentByLegalFileId(
    HttpServletRequest httpServletRequest,
    Long id
  ) {
    try {
      HttpHeaders headers = new HttpHeaders();
      LegalFile legalFile = legalFileService.findByFileId(id);
      if (legalFile == null) {
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        return new ResponseEntity<>(null, headers, HttpStatus.OK);
      }
      authorizeDownload(legalFile);
      if (String.valueOf(httpServletRequest.getParameter("cd")).equalsIgnoreCase("attachment")) {
        headers.set("Content-Disposition", "attachment; filename=" + legalFile.getFileName());
      } else {
        headers.set("Content-Disposition", "inline");
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

  /**
   * Security issue #3 (SECURITY.md): owner/otorisasi download agreement.
   * Agreement -> Cwr -> Customer; customer hanya boleh agreement miliknya.
   */
  private void authorizeAgreementDownload(AgreementFile agreementFile) throws AccessDeniedException {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
      throw new AccessDeniedException("Forbidden");
    }

    if (authentication.getPrincipal() instanceof Customer principal) {
      UUID ownerCustCode = null;
      if (agreementFile.getAgreement() != null
        && agreementFile.getAgreement().getCwr() != null
        && agreementFile.getAgreement().getCwr().getCustomer() != null
      ) {
        ownerCustCode = agreementFile.getAgreement().getCwr().getCustomer().getCustCode();
      }

      if (ownerCustCode == null || !ownerCustCode.equals(principal.getCustCode())) {
        throw new AccessDeniedException("Forbidden");
      }
      return;
    }

    if (authentication.getPrincipal() instanceof MstUser) {
      return;
    }

    throw new AccessDeniedException("Forbidden");
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

      authorizeAgreementDownload(agreementFile);
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
          mstFileTypeRepository.findTopByFileTypeNameAndBouwheerCodeOrderByFileTypeIdDesc("Akta Pendirian", UUID.fromString(customer.getBouwheer()));
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
        Optional<MstFileType> findMst = mstFileTypeRepository.findTopByFileTypeNameAndBouwheerCodeOrderByFileTypeIdDesc("Akta Perubahan Terakhir Lainnya",UUID.fromString(customer.getBouwheer()));
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
        Optional<MstFileType> findMst = mstFileTypeRepository.findTopByFileTypeNameAndBouwheerCodeOrderByFileTypeIdDesc("Sk Persetujuan Kemenkumham",UUID.fromString(customer.getBouwheer()));
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
        Optional<MstFileType> findMst = mstFileTypeRepository.findTopByFileTypeNameAndBouwheerCodeOrderByFileTypeIdDesc("NPWP",UUID.fromString(customer.getBouwheer()));
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
        Optional<MstFileType> findMst = mstFileTypeRepository.findTopByFileTypeNameAndBouwheerCodeOrderByFileTypeIdDesc("NIB",UUID.fromString(customer.getBouwheer()));
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
        Optional<MstFileType> findMst = mstFileTypeRepository.findTopByFileTypeNameAndBouwheerCodeOrderByFileTypeIdDesc("PKP",UUID.fromString(customer.getBouwheer()));
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

        Optional<MstFileType> findMst1 = mstFileTypeRepository.findTopByFileTypeNameAndBouwheerCodeOrderByFileTypeIdDesc("IPS",UUID.fromString(customer.getBouwheer()));
        findMst1.ifPresent(mstFileTypeRepository::delete);

        final MstFileType m7;
        Optional<MstFileType> findMst = mstFileTypeRepository.findTopByFileTypeNameAndBouwheerCodeOrderByFileTypeIdDesc("Identitas Pengurus",UUID.fromString(customer.getBouwheer()));
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
        Optional<MstFileType> findMst = mstFileTypeRepository.findTopByFileTypeNameAndBouwheerCodeOrderByFileTypeIdDesc("Laporan Keuangan",UUID.fromString(customer.getBouwheer()));
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
        Optional<MstFileType> findMst = mstFileTypeRepository.findTopByFileTypeNameAndBouwheerCodeOrderByFileTypeIdDesc("Ktp Pengurus",UUID.fromString(customer.getBouwheer()));
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
        Optional<MstFileType> findMst = mstFileTypeRepository.findTopByFileTypeNameAndBouwheerCodeOrderByFileTypeIdDesc("Bank Detail",UUID.fromString(customer.getBouwheer()));
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
          Optional<MstFileType> findMst = mstFileTypeRepository.findTopByFileTypeNameAndBouwheerCodeOrderByFileTypeIdDesc(doc.getDocumentName(),UUID.fromString(customer.getBouwheer()));
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

      Page<LegalFile> paginate = legalFileRepository.findAllByCustCodeAndFinancingHdrIsNullOrderByFileName(
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
    String financingHdrCode
  ) {
    try {
      if (request.getPageNo() != null && request.getPageNo() < 1
        || request.getPageSize() != null && request.getPageSize() < 1) {
        throw new BusinessException(HttpStatus.BAD_REQUEST, 400, "pageNo dan pageSize harus minimal 1.");
      }
      Optional<FinancingHdr> financingHdr = financingHdrRepository.findByFinancingHdrCode(UUID.fromString(financingHdrCode));
      if (financingHdr.isEmpty()){
        log.info(ErrorConstant.ERROR_MESSAGE_81 + "{}", financingHdrCode);
        throw new BusinessException(HttpStatus.CONFLICT, ErrorConstant.ERROR_CODE_81, ErrorConstant.ERROR_MESSAGE_81 +" Financing Code");
      }
      int pageNo = 0;
      int pageSize = 10;

      if (request.getPageNo() != null) {
        pageNo = request.getPageNo();
      }
      if (request.getPageSize() != null) {
        pageSize = request.getPageSize();
      }

      if (pageNo > 0) {
        pageNo = pageNo - 1;
      }

      List<String> allocations = List.of("Internal", "Legal", "Financing");
      UUID bouwheerCode = financingHdr.get().getBouwheer().getBouwheerCode();
      boolean customQuery = hasDocumentQuery(request);
      Page<MstFileType> page = customQuery ? null : mstFileTypeRepository
        .findAllByFileAllocationInAndBouwheerCodeOrderByFileTypeIdDesc(
          allocations, bouwheerCode, PageRequest.of(pageNo, pageSize, Sort.by("fileTypeId").descending()));
      List<MstFileType> files = customQuery
        ? mstFileTypeRepository.findAllByFileAllocationInAndBouwheerCodeOrderByFileTypeIdDesc(allocations, bouwheerCode)
        : page.getContent();

      List<MstFileTypeDto> result = files.stream().map((file) -> {
          MstFileTypeDto dto = FileTypeMapper.INSTANCE.mstFileToDto(file);
          if ("DOC006".equals(file.getFileTypeCode())) {
            legalFileRepository
              .findByFinancingHdr_FinancingHdrCodeAndFileTypeCode_FileTypeCode(
                financingHdr.get().getFinancingHdrCode(), file.getFileTypeCode())
              .ifPresent(active -> {
                LegalFileDto uploaded = FileTypeMapper.INSTANCE.legalFileToDto(active);
                uploaded.setUploadedDate(active.getDtmUpd() != null ? active.getDtmUpd() : active.getDtmCrt());
                uploaded.setFileUrl(UriUtils.fileUlr(
                  httpServletRequest,
                  Math.toIntExact(active.getFileId()),
                  UriUtils.DocType.loan
                ));
                dto.setLegalFile(uploaded);
              });
            return dto;
          }
          Optional<DokumentHistoryUpload> latest = dokumentHistoryUploadRepository
            .findTopByFinancingHdr_FinancingHdrCodeAndFileTypeCodeOrderByUploadedAtDescIdDesc(
              financingHdr.get().getFinancingHdrCode(), file.getFileTypeCode());
          if (latest.isPresent()) {
            DokumentHistoryUpload upload = latest.get();
            LegalFileDto uploaded = new LegalFileDto();
            uploaded.setFileId(upload.getId());
            uploaded.setFileName(upload.getFileName());
            uploaded.setUploadedDate(upload.getUploadedAt());
            uploaded.setFileUrl("/api/v1/document-uploads/" + financingHdrCode + "/"
              + file.getFileTypeCode() + "/" + upload.getId() + "/download");
            dto.setLegalFile(uploaded);
            return dto;
          }
          LegalFile legalFile = legalFileService.fetchByMstFileTypeAndCust(financingHdr.get().getCustomer(), file);

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

      if (customQuery) {
        return paginateDocuments(result, request);
      }

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
