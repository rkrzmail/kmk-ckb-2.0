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

      String financingStatusFilter = null,
        custNameFilter = null,
        bouwheerNameFilter = null;


      //add role
      Optional<MstAppRoleFormUser> findPermission = mstAppRoleFormUserRepository
        .findTopByUserOrderByAppRoleFormUserId(user);
      MstAppRoleFormUser permission = findPermission
        .orElseGet(() -> MstAppRoleFormUser.builder().build());
      String roleCode = permission
        .getAppRoleForm()
        .getApplicationRole()
        .getRoleCode()
        .getRoleCode();


      if (
        !StringUtil.isNullOrEmpty(request.getSearchBy())
          && !StringUtil.isNullOrEmpty(request.getSearchValue())
      ) {
        switch (request.getSearchBy().toLowerCase()) {
          case "status":
            break;
          case "namadebitur":
          case "custname":
            custNameFilter = request.getSearchValue();
            break;
          case "pemberikerja":
          case "bouwheername":
            bouwheerNameFilter = request.getSearchValue();
            break;
          case "cabang":
            break;
        }
      }

      // Role-specific statuses are mapped below, so pagination must happen after that filter.
      Page<FinancingHdr> financingHdrPage = financingHdrRepository.findAllAssignmentFinancingRaw(
        user.getEmployee().getBranch().getBranchCode(),
        financingStatusFilter,
        custNameFilter,
        bouwheerNameFilter,
        org.springframework.data.domain.Pageable.unpaged(PaginationSort.assignments(request))
      );


      return SpecPagination.paginationData(new SpecPagination<FinancingHdr, AssignmentDto>(financingHdrPage.stream().toList(), request) {
        @Override
        public AssignmentDto filter(AssignmentDto data) {

          if (isSearchBy("financingHdrCode") && equal(data.getFinancingHdrCode().toString())) {
            return data;
          } else if ((isSearchBy("custName") || isSearchBy("NamaDebitur")) && like(data.getCustName())) {
            return data;
          } else if ((isSearchBy("bouwheerName") || isSearchBy("PemberiKerja")) && like(data.getBouwheerName())) {
            return data;
          } else if (isSearchBy("status") && like(data.getStatus())) {
            return data;
          } else if (isSearchBy("Cabang") && like(user.getEmployee().getBranch().getBranchName())) {
            return data;
          }

          return null;
        }

        @Override
        public AssignmentDto eval(FinancingHdr e) {
          if (e.getCustomer() == null || e.getBouwheer() == null) {
            return null;
          }

          boolean isNewCust = financingHdrRepository
            .countByCustomerAndFinancingStatus(
              e.getCustomer(),
              "PAID"
            ) == 0;


          MappedFinancingStatus financingStatus;
          if (roleCode.equalsIgnoreCase("account_officer")) {
            financingStatus = new MappedFinancingStatus(
              e,
              MappedFinancingStatus.Type.AccountOfficer
            );

          } else {
            financingStatus = new MappedFinancingStatus(
              e,
              MappedFinancingStatus.Type.BranchAdmin
            );
            if (financingStatus.getStatus().equalsIgnoreCase("NEW")) {
              return null;
            }
          }


          Agreement agreement = agreementRepository.findTopByFinancingHdr(e).orElse(null);
          AgreementFile agreementFile = null;

          String agreementDoc = null, agreementCode = null;
          if (agreement != null) {
            agreementCode = agreement.getAgreementCode();
            agreementFile = agreementFileRepository.findTopByAgreementOrderByAgreementFileId(
              agreement
            ).orElse(null);
          }

          if (agreementFile != null) {
            agreementDoc = UriUtils.fileUlr(
              httpServletRequest,
              Math.toIntExact(agreementFile.getAgreementFileId()),
              UriUtils.DocType.agreement
            );
          }

          // Check verify date
          Optional<AgreementFileSigning>agreementFileSigning = agreementFileSigningRepository.findFirstByAgreementCode(agreementCode);
          return AssignmentDto.builder()
            .financingHdrCode(e.getFinancingHdrCode())
            .agreementCode(agreementCode)
            .custCode(e.getCustomer().getCustCode())
            .custName(e.getCustomer().getCustName())
            .bouwheerCode(e.getBouwheer().getBouwheerCode())
            .bouwheerName(e.getBouwheer().getBouwheerName())
            .verifDate(agreementFileSigning
              .map(AgreementFileSigning::getVerifDate)
              .orElse(null))
            .dueDate(Utils.fromInstant(e.getFinancingDueDate()))
            .financingAmount(BigDecimal.valueOf(e.getFinancingAmt()))
            .custStatus(isNewCust ? "New Customer" : "Existing Customer")
            .status(financingStatus.getStatus())
            .statusLabel(financingStatus.getLabel())
            .agreementDoc(agreementDoc)
            .build();
        }
      });

    } catch (Exception e) {
      log.error("assignmentList: error {}", e.getMessage());
      throw e;
    }
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
          if (isSearchBy("financingAmt")) {
            if (e.getFinancingAmt() == Utils.getIntCurr(getSearchValue())) {
              return e;
            }
          }
          if (isSearchBy("schema")) {
            if ((100 - e.getRetention()) == Utils.getIntCurr(getSearchValue())) {
              return e;
            }
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
