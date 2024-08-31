package com.kmkbe.modules.loan_submission.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.kmkbe.core.domain.dto.BaseSimpleRemoteResponseDto;
import com.kmkbe.core.domain.dto.InvoiceDto;
import com.kmkbe.core.domain.entity.*;
import com.kmkbe.core.domain.mapper.InvoiceMapper;
import com.kmkbe.core.domain.model.PostedInvoicePayload;
import com.kmkbe.core.domain.repository.*;
import com.kmkbe.modules.remote.request.UpdateFinancingStatusRequest;
import com.kmkbe.modules.remote.service.FinancingRemoteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class FinancingService {
    private final AgreementRepository agreementRepository;

    private final FinancingRemoteService financingRemoteService;


    private final FinancingHdrRepository financingHdrRepository;
    private final CustomerRepository customerRepository;



    public void recallApprovalStatus()   {
        //find all aggremmnet with flag false or null
        List<Agreement> list = agreementRepository.viewApprovalStatusPending();
        for (Agreement agreement : list) {

            UpdateFinancingStatusRequest updateFinancingStatusRequest = UpdateFinancingStatusRequest.builder()
                    .vendorCode(agreement.getFinancingHdr().getCustomer().getCustExternalCode() )
                    .financingCode(agreement.getFinancingHdr().getFinancingHdrCode().toString())
                    .status(UpdateFinancingStatusRequest.Status.Approve)
                    .build();
            try {
                BaseSimpleRemoteResponseDto<ObjectUtils.Null> nullBaseSimpleRemoteResponseDto =  financingRemoteService.updateFinancingStatus(updateFinancingStatusRequest);
                //update
                agreement.setApprovalFlag("true");
                agreementRepository.save(agreement);

            }catch (Exception exception){
                //next
            }
            //end
        }

    }

}
