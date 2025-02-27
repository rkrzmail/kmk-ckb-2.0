package com.kmkbe.modules.loan_submission.service;

import com.kmkbe.core.domain.dto.BaseMstRemoteResponseDto;
import com.kmkbe.core.domain.dto.InquiryAgreementCwrDto;
import com.kmkbe.core.domain.dto.InquiryNewInfoAgreementDto;
import com.kmkbe.core.domain.entity.*;
import com.kmkbe.core.domain.repository.*;
import com.kmkbe.core.domain.request.InquiryNewInfoAgreementRequest;
import com.kmkbe.core.exception.CommonInvalidException;
import com.kmkbe.core.utils.DateTimeUtils;
import com.kmkbe.modules.remote.request.InquiryAgreementRemoteRequest;
import com.kmkbe.modules.remote.request.UpdateFinancingStatusRequest;
import com.kmkbe.modules.remote.service.CwrRemoteService;
import com.kmkbe.modules.remote.service.FinancingRemoteService;
import com.kmkbe.nikita.utils.Utils;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class FinancingService {
    private final AgreementRepository agreementRepository;
    private final FinancingRemoteService financingRemoteService;
    private final FinancingHdrRepository financingHdrRepository;
    private final CustomerRepository customerRepository;
    private final InquiryDisburseService  inquiryDisburseService;
    private final  FinancingDtlService  financingDtlService;
    private final DisbursementLogRepository disbursementLogRepository;
    private final CwrRemoteService cwrRemoteService;


    public void recallApprovalStatus() {
        //find all aggremmnet with flag false or null
        List<Agreement> list = agreementRepository.viewApprovalStatusNoPending() ;//viewApprovalStatusPending();
        if (list != null && !list.isEmpty()) {
            for (Agreement agreement : list) {
                UpdateFinancingStatusRequest updateFinancingStatusRequest = null;
                try {
                    updateFinancingStatusRequest = UpdateFinancingStatusRequest.builder()
                            .vendorCode(agreement.getFinancingHdr().getCustomer().getCustExternalCode())
                            .financingCode(agreement.getFinancingHdr().getFinancingHdrCode().toString())
                            .status(UpdateFinancingStatusRequest.Status.Approved)
                            .build();
                } catch (Exception ignored) {  }
                try {
                    //stop bila sudah 200
                    //financingRemoteService.updateFinancingStatus(updateFinancingStatusRequest);
                    //agreement.setApprovalFlag("true");
                } catch (Exception ignored) {  }

                try {
                    agreement = updateFromConfin(agreement);
                } catch (Exception ignored) { }

                try {
                    updateFinStatusLiveIfGoLive(agreement);
                } catch (Exception ignored) { }

                try {
                    //stop bila saudha disbur(di log disb ada)
                    List<DisbursementLog> disbursementLogs = disbursementLogRepository.findAllByAgreement(agreement);
                    if (disbursementLogs.isEmpty()){
                        inquiryDisburseService. inquiryDisburseAuto(agreement);
                    }
                } catch (Exception ignored) { }


                try {
                    agreementRepository.save(agreement);
                } catch (Exception ignored) {
                    ignored.printStackTrace();
                }
            }
            System.out.println("");
        }


    }
    public Agreement updateFromConfin(Agreement agreement) {
        final List<InquiryAgreementCwrDto> data;
        try {
            BaseMstRemoteResponseDto<List<InquiryAgreementCwrDto>> response = cwrRemoteService.inquiryAgreementByNoAgreement(
                    InquiryAgreementRemoteRequest.builder()
                            .agreementNo(agreement.getAgreementCode())
                            .build()
            );

            data = response.getData();


            for (InquiryAgreementCwrDto inquiryAgreement : data) {
                String aggrCode = inquiryAgreement.getAgrmntNo();

                agreement.setStatus(inquiryAgreement.getStatus());
                agreement.setCurrency(inquiryAgreement.getCurrency());
                agreement.setFinancingAmt(inquiryAgreement.getNtfAmt());
                agreement.setProductOffering(inquiryAgreement.getProductOffering());
                agreement.setFacility(inquiryAgreement.getFacility());
                agreement.setDtmUpd(DateTimeUtils.now());
            }
        } catch (Exception e) {  }

        return  agreement;
    }
    public void updateFinStatusLiveIfGoLive(Agreement agreement) {
        if (agreement.getStatus().equalsIgnoreCase("Live") ){//Ready Golive
            Optional<FinancingHdr> financingHdrO =  financingHdrRepository.findByFinancingHdrCode(agreement.getFinancingHdr().getFinancingHdrCode());
            if (financingHdrO.isPresent() && financingHdrO.get().getFinancingStatus().equalsIgnoreCase("INPROCESS") ) {
                FinancingHdr financingHdr = financingHdrO.get();
                financingHdr.setFinancingStatus("LIVE");
                financingHdr.setFinancingStep("GOLIVE");
                financingHdrRepository.save(financingHdr);
            }
        }
    }

    public void updateFinStatusLiveIfGoLive() {
        //cchek status fnance yang   financingHdr.setFinancingStatus("INPROCESS");
        //        financingHdr.setFinancingStep("SIGNED");//SIGNING
        List<FinancingHdr> financingHdrs = financingHdrRepository.findAllByFinancingStatusAndFinancingStep("INPROCESS","SIGNING");
        financingHdrs.forEach(financingHdr -> {


        });


        //lihat fi aggement statusnya (Ready Golive) bila iya, updat ejadio LIVE, GOLIVE
        List<Agreement> list = agreementRepository.viewApprovalStatusNoPending();
    }

}
