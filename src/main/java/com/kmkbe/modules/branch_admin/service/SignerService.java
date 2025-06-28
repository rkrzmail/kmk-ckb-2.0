package com.kmkbe.modules.branch_admin.service;

import com.kmkbe.core.domain.dto.*;
import com.kmkbe.core.domain.entity.*;
import com.kmkbe.core.domain.model.MappedFinancingStatus;
import com.kmkbe.core.domain.model.PaginationResult;
import com.kmkbe.core.domain.repository.*;
import com.kmkbe.core.domain.request.PaginationRequest;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SignerService {
    private final FinancingHdrRepository financingHdrRepository;
    private final MstUserRepository mstUserRepository;
    private final AgreementRepository agreementRepository;
    private final AgreementFileRepository agreementFileRepository;
    private final MstAppRoleFormUserRepository mstAppRoleFormUserRepository;
    private final SimulationHistRepository simulationHistRepository;
    private final SignerPersonRepository signerPersonRepository;

    public PaginationResult<SignerDto> assignmentList(
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


            String financingStatusFilter = null,
                    custNameFilter = null,
                    bouwheerNameFilter = null;


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


            return SpecPagination.paginationData(new SpecPagination<FinancingHdr, SignerDto>(financingHdrPage.stream().toList(), request)
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
                public SignerDto eval(FinancingHdr e) {
                    if (e.getCustomer() == null || e.getBouwheer() == null) {
                        return null;
                    }

                    boolean isNewCust = financingHdrRepository
                            .countByCustomerAndFinancingStatus(
                                    e.getCustomer(),
                                    "PAID"
                            ) == 0;



                    return SignerDto.builder()
                            .financingHdrCode(e.getFinancingHdrCode())
                            .custCode(e.getCustomer().getCustCode())
                            .custName(e.getCustomer().getCustName())
                            .bouwheerName(e.getBouwheer().getBouwheerName())
                            .custStatus(isNewCust ? "New Customer" : "Existing Customer")
                            .build();
                }
            });

        } catch (Exception e) {
            log.error("assignmentList: error {}", e.getMessage());
            throw e;
        }
    }

    public List<SignerPersonDto> signerPersonList() {
        // Ambil semua data PolicyAgreement
        List<SignerPerson> SignerPerson = signerPersonRepository.findAll();

        // Convert list entity ke DTO tanpa builder
        List<SignerPersonDto> dtoList = new ArrayList<>();

        for (SignerPerson signer : SignerPerson) {
            SignerPersonDto signerPersonDto = new SignerPersonDto();
            signerPersonDto.setKaryawanId(signer.getKaryawanId());
            signerPersonDto.setKaryawanName(signer.getKaryawanName());
            signerPersonDto.setJabatan(signer.getJabatan());
            signerPersonDto.setSignerStatus(signer.getSignerStatus());
            signerPersonDto.setSignhubStatus(signer.getSignhubStatus());
            signerPersonDto.setUsrCrt(signer.getUsrCrt());
            signerPersonDto.setDtmCrt(signer.getDtmCrt());
            signerPersonDto.setUsrUpd(signer.getUsrUpd());
            signerPersonDto.setDtmUpd(signer.getDtmUpd());

            dtoList.add(signerPersonDto);
        }

        return dtoList;
    }
}
