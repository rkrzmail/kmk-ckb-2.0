package com.kmkbe.modules.branch_admin.service;

import com.kmkbe.core.domain.dto.AssignmentDto;
import com.kmkbe.core.domain.dto.SimulationHistDto;
import com.kmkbe.core.domain.entity.*;
import com.kmkbe.core.domain.model.MappedFinancingStatus;
import com.kmkbe.core.domain.repository.*;
import com.kmkbe.core.domain.request.PaginationRequest;
import com.kmkbe.core.domain.model.PaginationResult;
import com.kmkbe.core.security.CurrentUserService;
import com.kmkbe.core.utils.UriUtils;
import com.kmkbe.modules.user.entity.MstAppRoleFormUser;
import com.kmkbe.modules.user.entity.MstUser;
import com.kmkbe.modules.user.repository.MstAppRoleFormUserRepository;
import com.kmkbe.modules.user.repository.MstUserRepository;
import com.kmkbe.helpers.utils.SpecPagination;
import com.kmkbe.helpers.utils.PaginationSort;
import com.kmkbe.helpers.utils.PaginationRequests;
import com.kmkbe.helpers.base.BasePaginationRequest;
import com.kmkbe.helpers.utils.Utils;
import io.netty.util.internal.StringUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.security.SignatureException;
import java.util.*;

@Slf4j
@Service
public class AssignmentSubmissionService {
  private final FinancingHdrRepository financingHdrRepository;
  private final MstUserRepository mstUserRepository;
  private final AgreementRepository agreementRepository;
  private final AgreementFileRepository agreementFileRepository;
  private final MstAppRoleFormUserRepository mstAppRoleFormUserRepository;
  private final SimulationHistRepository simulationHistRepository;
  private final CurrentUserService currentUserService;
  private final AgreementFileSigningRepository agreementFileSigningRepository;

  public AssignmentSubmissionService(FinancingHdrRepository financingHdrRepository,
                                     MstUserRepository mstUserRepository,
                                     AgreementRepository agreementRepository,
                                     AgreementFileRepository agreementFileRepository,
                                     MstAppRoleFormUserRepository mstAppRoleFormUserRepository,
                                     SimulationHistRepository simulationHistRepository,
                                     CurrentUserService currentUserService, AgreementFileSigningRepository agreementFileSigningRepository) {
    this.financingHdrRepository = financingHdrRepository;
    this.mstUserRepository = mstUserRepository;
    this.agreementRepository = agreementRepository;
    this.agreementFileRepository = agreementFileRepository;
    this.mstAppRoleFormUserRepository = mstAppRoleFormUserRepository;
    this.simulationHistRepository = simulationHistRepository;
    this.currentUserService = currentUserService;
    this.agreementFileSigningRepository = agreementFileSigningRepository;
  }

  public PaginationResult<AssignmentDto>  assignmentList(
    HttpServletRequest httpServletRequest, BasePaginationRequest request
  ) throws SignatureException {
    return assignmentList(httpServletRequest, PaginationRequests.from(request));
  }

  public PaginationResult<AssignmentDto>  assignmentList(
    HttpServletRequest httpServletRequest,
    PaginationRequest request
  ) throws SignatureException {
    try {
      MstUser authenticateUser = currentUserService.internalUser();
      MstUser user = mstUserRepository.findById(authenticateUser.getUserCode()).orElseThrow();
      String roleCode = resolveRoleCode(user);
      AssignmentFilters filters = resolveSearchFilters(request);
      String branchCode = user.getEmployee().getBranch().getBranchCode();
      Page<FinancingHdr> financingHdrPage = findAssignmentPage(request, branchCode, filters);

      return SpecPagination.paginationData(new SpecPagination<FinancingHdr, AssignmentDto>(
        financingHdrPage.stream().toList(), request
      ) {
        @Override
        public void sort(List<AssignmentDto> data) {
          Comparator<AssignmentDto> comparator = PaginationSort.assignmentVerifDateComparator(request);
          if (comparator != null) data.sort(comparator);
        }

        @Override
        public AssignmentDto filter(AssignmentDto data) {
          return matchesSearch(data, request, user) ? data : null;
        }

        @Override
        public AssignmentDto eval(FinancingHdr e) {
          return toAssignmentDto(e, roleCode, httpServletRequest);
        }
      });

    } catch (Exception e) {
      log.error("assignmentList: error {}", e.getMessage());
      throw e;
    }
  }

  private String resolveRoleCode(MstUser user) {
    Optional<MstAppRoleFormUser> findPermission = mstAppRoleFormUserRepository
      .findTopByUserOrderByAppRoleFormUserId(user);
    MstAppRoleFormUser permission = findPermission
      .orElseGet(() -> MstAppRoleFormUser.builder().build());
    return permission
      .getAppRoleForm()
      .getApplicationRole()
      .getRoleCode()
      .getRoleCode();
  }

  private AssignmentFilters resolveSearchFilters(PaginationRequest request) {
    if (StringUtil.isNullOrEmpty(request.getSearchBy())
      || StringUtil.isNullOrEmpty(request.getSearchValue())) {
      return new AssignmentFilters(null, null);
    }

    return switch (request.getSearchBy().toLowerCase()) {
      case "namadebitur", "custname" -> new AssignmentFilters(request.getSearchValue(), null);
      case "pemberikerja", "bouwheername" -> new AssignmentFilters(null, request.getSearchValue());
      default -> new AssignmentFilters(null, null);
    };
  }

  private Page<FinancingHdr> findAssignmentPage(
    PaginationRequest request,
    String branchCode,
    AssignmentFilters filters
  ) {
    if ("financingHdrCode".equalsIgnoreCase(request.getSearchBy())
      && !StringUtil.isNullOrEmpty(request.getSearchValue())) {
      return findAssignmentPageByCode(request.getSearchValue().trim(), branchCode, request);
    }

    return financingHdrRepository.findAllAssignmentFinancingRaw(
      branchCode,
      null,
      filters.custName(),
      filters.bouwheerName(),
      Pageable.unpaged(PaginationSort.assignments(request))
    );
  }

  private Page<FinancingHdr> findAssignmentPageByCode(
    String codeSearch,
    String branchCode,
    PaginationRequest request
  ) {
    if (!codeSearch.matches("(?i)[0-9a-f-]{1,36}")) {
      return Page.empty();
    }
    if (codeSearch.length() == 36) {
      UUID code = parseUuid(codeSearch);
      return new PageImpl<>(code == null ? List.of() : financingHdrRepository
        .findByFinancingHdrCodeAndMstBranch_BranchCode(code, branchCode).stream().toList());
    }
    return financingHdrRepository.findAllAssignmentFinancingByCodeFragment(
      branchCode, codeSearch, Pageable.unpaged(PaginationSort.assignments(request)));
  }

  private UUID parseUuid(String value) {
    try {
      return UUID.fromString(value);
    } catch (IllegalArgumentException exception) {
      return null;
    }
  }

  private boolean matchesSearch(AssignmentDto data, PaginationRequest request, MstUser user) {
    String searchBy = Utils.valueOf(request.getSearchBy());
    String searchValue = Utils.valueOf(request.getSearchValue());
    if (isSearchBy(searchBy, "financingHdrCode") && like(data.getFinancingHdrCode().toString(), searchValue)) {
      return true;
    }
    if ((isSearchBy(searchBy, "custName") || isSearchBy(searchBy, "NamaDebitur"))
      && like(data.getCustName(), searchValue)) {
      return true;
    }
    if ((isSearchBy(searchBy, "bouwheerName") || isSearchBy(searchBy, "PemberiKerja"))
      && like(data.getBouwheerName(), searchValue)) {
      return true;
    }
    if (isSearchBy(searchBy, "status") && like(data.getStatus(), searchValue)) {
      return true;
    }
    if (isSearchBy(searchBy, "financingAmount")) {
      try {
        return data.getFinancingAmount() != null
          && data.getFinancingAmount().compareTo(new BigDecimal(searchValue.trim())) == 0;
      } catch (NumberFormatException ignored) {
        return false;
      }
    }
    if (isSearchBy(searchBy, "custStatus") && like(data.getCustStatus(), searchValue)) {
      return true;
    }
    return isSearchBy(searchBy, "Cabang")
      && like(user.getEmployee().getBranch().getBranchName(), searchValue);
  }

  private boolean isSearchBy(String actual, String expected) {
    return actual.equalsIgnoreCase(expected);
  }

  private boolean like(String value, String searchValue) {
    return Utils.valueOf(value).toLowerCase().contains(searchValue.toLowerCase());
  }

  private AssignmentDto toAssignmentDto(
    FinancingHdr e,
    String roleCode,
    HttpServletRequest httpServletRequest
  ) {
    if (e.getCustomer() == null || e.getBouwheer() == null) {
      return null;
    }

    MappedFinancingStatus financingStatus = resolveFinancingStatus(e, roleCode);
    if (financingStatus == null) {
      return null;
    }

    Agreement agreement = agreementRepository.findTopByFinancingHdr(e).orElse(null);
    AgreementFile agreementFile = null;
    String agreementDoc = null;
    String agreementCode = null;
    if (agreement != null) {
      agreementCode = agreement.getAgreementCode();
      agreementFile = agreementFileRepository.findTopByAgreementOrderByAgreementFileId(agreement).orElse(null);
    }

    if (agreementFile != null) {
      agreementDoc = UriUtils.fileUlr(
        httpServletRequest,
        Math.toIntExact(agreementFile.getAgreementFileId()),
        UriUtils.DocType.agreement
      );
    }

    Optional<AgreementFileSigning> agreementFileSigning =
      agreementFileSigningRepository.findFirstByAgreementCode(agreementCode);
    return AssignmentDto.builder()
      .financingHdrCode(e.getFinancingHdrCode())
      .agreementCode(agreementCode)
      .custCode(e.getCustomer().getCustCode())
      .custName(e.getCustomer().getCustName())
      .bouwheerCode(e.getBouwheer().getBouwheerCode())
      .bouwheerName(e.getBouwheer().getBouwheerName())
      .verifDate(agreementFileSigning.map(AgreementFileSigning::getVerifDate).orElse(null))
      .dueDate(e.getFinancingDueDate())
      .financingAmount(BigDecimal.valueOf(e.getFinancingAmt()))
      .totalInvoiceAmount(e.getTotalInvoiceAmt() == null ? null : BigDecimal.valueOf(e.getTotalInvoiceAmt()))
      .invoiceQty(e.getInvoiceQty())
      .custStatus(e.getCustomer().getExistingCust())
      .status(financingStatus.getStatus())
      .statusLabel(financingStatus.getLabel())
      .agreementDoc(agreementDoc)
      .dtmCrt(e.getDtmCrt())
      .build();
  }

  private MappedFinancingStatus resolveFinancingStatus(FinancingHdr e, String roleCode) {
    if (roleCode.equalsIgnoreCase("account_officer")) {
      return new MappedFinancingStatus(e, MappedFinancingStatus.Type.AccountOfficer);
    }

    MappedFinancingStatus financingStatus = new MappedFinancingStatus(e, MappedFinancingStatus.Type.BranchAdmin);
    return financingStatus.getStatus().equalsIgnoreCase("NEW") ? null : financingStatus;
  }

  private record AssignmentFilters(String custName, String bouwheerName) {
  }

  public PaginationResult<SimulationHistDto> tocList(
    String financingHdrCode,
    BasePaginationRequest request
  ) {
    return tocList(financingHdrCode, PaginationRequests.from(request));
  }

  public PaginationResult<SimulationHistDto> tocList(
    String financingHdrCode,
    PaginationRequest request
  ) {
    try {

      Optional<FinancingHdr> finHdr = financingHdrRepository.findByFinancingHdrCode(UUID.fromString(financingHdrCode));
      Optional<List<SimulationHist>> simHists = simulationHistRepository.findAllByFinancingHdr(finHdr.get());
      return SpecPagination.paginationData(new SpecPagination<SimulationHist, SimulationHistDto>(simHists, request) {

        @Override
        public SimulationHist search(SimulationHist e) {
          if (isSearchBy("financingAmt")
            && e.getFinancingAmt() == Utils.getIntCurr(getSearchValue())) {
            return e;
          }
          if (isSearchBy("schema")
            && (100 - e.getRetention()) == Utils.getIntCurr(getSearchValue())) {
            return e;
          }
          return null;
        }

        @Override
        public SimulationHistDto eval(SimulationHist e) {
          return SimulationHistDto.builder()
            .adminAmt(e.getAdminAmt())
            .simulationHistCode(e.getSimulationHistCode())
            .financingAmt(e.getFinancingAmt())
            .effetiveRate(e.getEffectiveRate())
            .dibursmentAmt(e.getEstDisbust())
            .schema(100 - e.getRetention())
            .adminFee(e.getAdminAmt())
            .build();
        }

        @Override
        public void sort(List<SimulationHistDto> data) {
          Comparator<SimulationHistDto> comparator = PaginationSort.tocComparator(request);
          if (comparator != null) {
            data.sort(comparator);
          }
          for (int i = 0; i < data.size(); i++) {
            data.get(i).setNo(i + 1);
          }
        }
      });

    } catch (Exception e) {
      log.error("tocList: error {}", e.getMessage());
      throw e;
    }
  }
}
