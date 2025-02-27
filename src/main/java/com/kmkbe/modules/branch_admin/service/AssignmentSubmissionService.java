package com.kmkbe.modules.branch_admin.service;

import com.kmkbe.core.domain.dto.AssignmentDto;
import com.kmkbe.core.domain.dto.SimulationHistDto;
import com.kmkbe.core.domain.entity.*;
import com.kmkbe.core.domain.model.MappedFinancingStatus;
import com.kmkbe.core.domain.repository.AgreementFileRepository;
import com.kmkbe.core.domain.repository.AgreementRepository;
import com.kmkbe.core.domain.repository.FinancingHdrRepository;
import com.kmkbe.core.domain.repository.SimulationHistRepository;
import com.kmkbe.core.domain.request.PaginationRequest;
import com.kmkbe.core.domain.model.PaginationResult;
import com.kmkbe.core.utils.UriUtils;
import com.kmkbe.modules.user.entity.MstAppRoleFormUser;
import com.kmkbe.modules.user.entity.MstUser;
import com.kmkbe.modules.user.repository.MstAppRoleFormUserRepository;
import com.kmkbe.modules.user.repository.MstUserRepository;
import com.kmkbe.modules.user.utils.UserInternalUtils;
import com.kmkbe.nikita.utils.SpecPagination;
import com.kmkbe.nikita.utils.Utils;
import io.netty.util.internal.StringUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.security.SignatureException;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class AssignmentSubmissionService {
    private final FinancingHdrRepository financingHdrRepository;
    private final MstUserRepository mstUserRepository;
    private final AgreementRepository agreementRepository;
    private final AgreementFileRepository agreementFileRepository;
    private final MstAppRoleFormUserRepository mstAppRoleFormUserRepository;
    private final SimulationHistRepository simulationHistRepository;

    public PaginationResult<AssignmentDto> assignmentList(
            HttpServletRequest httpServletRequest,
            Authentication authentication,
            PaginationRequest request
    ) throws SignatureException {
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


            MstUser authenticateUser = UserInternalUtils.authenticateUser(authentication);
            MstUser user = mstUserRepository.findById(authenticateUser.getUserCode()).orElseThrow();
            /*Page<FinancingHdr> financingHdrPage = financingHdrRepository.findByMstBranchOrderByFinancingHdrIdDesc(
                    user.getEmployee().getBranch(),
                    PageRequest.of(pageNo, pageSize)
            );*/


            String financingStatusFilter = null,
                    custNameFilter = null,
                    bouwheerNameFilter = null;



            //add role
            Optional<MstAppRoleFormUser> findPermission = mstAppRoleFormUserRepository
                    .findTopByUserOrderByAppRoleFormUserId(user);
            MstAppRoleFormUser permission = findPermission
                    .orElseGet(() -> MstAppRoleFormUser.builder().build());
            String roleCode =  permission
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
                        financingStatusFilter = request.getSearchValue();
                        break;
                    case "namadebitur":
                        custNameFilter = request.getSearchValue();
                        break;
                    case "pemberikerja":
                        bouwheerNameFilter = request.getSearchValue();
                        break;
                    case "cabang":
                        break;
                }
            }

            Page<FinancingHdr> financingHdrPage = financingHdrRepository.findAllAssignmentFinancingRaw(
                    user.getEmployee().getBranch().getBranchCode(),
                    financingStatusFilter,
                    custNameFilter,
                    bouwheerNameFilter,
                    PageRequest.of(pageNo, pageSize)
            );


            return SpecPagination.paginationData(new SpecPagination<FinancingHdr, AssignmentDto>(financingHdrPage.stream().toList(), request)
            {
                @Override
                public FinancingHdr search(FinancingHdr data) {

                    if (isSearchBy("financingHdrCode") && equal(data.getFinancingHdrCode().toString())  ){
                        return data;
                    }else if (isSearchBy("custName") && like(data.getCustomer().getCustName())  ){
                        return data;
                    }else if (isSearchBy("bouwheerName") && like(data.getBouwheer().getBouwheerName())  ){
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
                    if (roleCode.equalsIgnoreCase("account_officer")){
                        financingStatus = new MappedFinancingStatus(
                                e,
                                MappedFinancingStatus.Type.AccountOfficer
                        );

                    }else{
                        financingStatus = new MappedFinancingStatus(
                                e,
                                MappedFinancingStatus.Type.BranchAdmin
                        );
                        if (financingStatus.getStatus().equalsIgnoreCase("NEW")){
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

                    return AssignmentDto.builder()
                            .financingHdrCode(e.getFinancingHdrCode())
                            .agreementCode(agreementCode)
                            .custCode(e.getCustomer().getCustCode())
                            .custName(e.getCustomer().getCustName())
                            .bouwheerName(e.getBouwheer().getBouwheerName())
                            .verifDate(null)
                            .dueDate(Utils.fromInstant(e.getFinancingDueDate()))
                            .financingAmount(BigDecimal.valueOf(e.getFinancingAmt()))
                            .custStatus(isNewCust ? "New Customer" : "Existing Customer")
                            .status(financingStatus.getStatus())
                            .statusLabel(financingStatus.getLabel())
                            .agreementDoc(agreementDoc)
                            .build();

                }
            });



            /*List<AssignmentDto> result = financingHdrPage.stream()
                    .filter(e -> e.getCustomer() != null && e.getBouwheer() != null)
                    .map(e -> {
                        boolean isNewCust = financingHdrRepository
                                .countByCustomerAndFinancingStatus(
                                        e.getCustomer(),
                                        "PAID"
                                ) == 0;


                        MappedFinancingStatus financingStatus;
                        if (roleCode.equalsIgnoreCase("account_officer")){
                            financingStatus = new MappedFinancingStatus(
                                    e,
                                    MappedFinancingStatus.Type.AccountOfficer
                            );

                        }else{
                            financingStatus = new MappedFinancingStatus(
                                    e,
                                    MappedFinancingStatus.Type.BranchAdmin
                            );
                            if (financingStatus.getStatus().equalsIgnoreCase("NEW")){
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

                        return AssignmentDto.builder()
                                .financingHdrCode(e.getFinancingHdrCode())
                                .agreementCode(agreementCode)
                                .custCode(e.getCustomer().getCustCode())
                                .custName(e.getCustomer().getCustName())
                                .bouwheerName(e.getBouwheer().getBouwheerName())
                                .verifDate(null)
                                .dueDate(Utils.fromInstant(e.getFinancingDueDate()))
                                .financingAmount(BigDecimal.valueOf(e.getFinancingAmt()))
                                .custStatus(isNewCust ? "New Customer" : "Existing Customer")
                                .status(financingStatus.getStatus())
                                .statusLabel(financingStatus.getLabel())
                                .agreementDoc(agreementDoc)
                                .build();
                    })
                    .toList();

            List<AssignmentDto> resultNew = new ArrayList<>();
            for (AssignmentDto assignment : result) {
                if (assignment !=null ){
                    resultNew.add(assignment);
                }
            }

            return PaginationResult.<AssignmentDto>builder()
                    .currentPage(pageNo + 1)
                    .totalData(financingHdrPage.getTotalElements())
                    .totalPage(financingHdrPage.getTotalPages())
                    .list(resultNew)
                    .build();*/
        } catch (Exception e) {
            log.error("assignmentList: error {}", e.getMessage());
            throw e;
        }
    }

    public PaginationResult<SimulationHistDto> tocList(
            String financingHdrCode,
            PaginationRequest request
    ) {
        try {

            Optional<FinancingHdr>  finHdr = financingHdrRepository.findByFinancingHdrCode(UUID.fromString(financingHdrCode));
            Optional<List<SimulationHist>> simHists =  simulationHistRepository.findAllByFinancingHdr(finHdr.get());
            return SpecPagination.paginationData(    new SpecPagination<SimulationHist, SimulationHistDto>(simHists, request){

                @Override
                public SimulationHist search(SimulationHist e) {
                    if (isSearchBy("financingAmt") ){
                        if ( e.getFinancingAmt() ==  Utils.getIntCurr( getSearchValue())){
                            return e;
                        }
                    }
                    if (isSearchBy("schema") ){
                        if ( (100-e.getRetention()) == Utils.getIntCurr(getSearchValue())){
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
                            .schema(100-e.getRetention())
                            .adminFee(e.getAdminAmt())
                            .build();
                }

                @Override
                public void sort(List<SimulationHistDto> data) {
                    for (int i = 0; i < data.size(); i++) {
                        data.get(i).setNo(i+1);
                    }
                }
            });

        } catch (Exception e) {
            log.error("tocList: error {}", e.getMessage());
            throw e;
        }
    }
}
