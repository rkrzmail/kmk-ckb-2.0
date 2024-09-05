package com.kmkbe.modules.loan_submission.service;

import com.kmkbe.core.domain.entity.*;
import com.kmkbe.core.domain.repository.*;
import com.kmkbe.modules.remote.request.UpdateFinancingStatusRequest;
import com.kmkbe.modules.remote.service.FinancingRemoteService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FinancingService {
    private final AgreementRepository agreementRepository;
    private final FinancingRemoteService financingRemoteService;
    private final FinancingHdrRepository financingHdrRepository;
    private final CustomerRepository customerRepository;

    public void recallApprovalStatus() {
        //find all aggremmnet with flag false or null
        List<Agreement> list = agreementRepository.viewApprovalStatusPending();
        if (list != null && !list.isEmpty()) {
            for (Agreement agreement : list) {
                UpdateFinancingStatusRequest updateFinancingStatusRequest = UpdateFinancingStatusRequest.builder()
                        .vendorCode(agreement.getFinancingHdr().getCustomer().getCustExternalCode())
                        .financingCode(agreement.getFinancingHdr().getFinancingHdrCode().toString())
                        .status(UpdateFinancingStatusRequest.Status.Approved)
                        .build();
                try {
                    financingRemoteService.updateFinancingStatus(updateFinancingStatusRequest);
                    agreement.setApprovalFlag("true");
                    agreementRepository.save(agreement);
                } catch (Exception ignored) {
                }
            }
        }
    }
}
