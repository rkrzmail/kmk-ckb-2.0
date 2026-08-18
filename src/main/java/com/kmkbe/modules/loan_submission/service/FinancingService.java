package com.kmkbe.modules.loan_submission.service;

import com.kmkbe.core.domain.dto.*;
import com.kmkbe.core.domain.entity.*;
import com.kmkbe.core.domain.repository.*;
import com.kmkbe.core.utils.DateTimeUtils;
import com.kmkbe.modules.remote.request.InquiryAgreementRemoteRequest;
import com.kmkbe.modules.remote.request.InquiryCwrRemoteRequest;
import com.kmkbe.modules.remote.request.UpdateFinancingStatusRequest;
import com.kmkbe.modules.remote.service.CwrRemoteService;
import com.kmkbe.helpers.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class FinancingService {
  private final AgreementRepository agreementRepository;
  private final FinancingHdrRepository financingHdrRepository;
  private final InquiryDisburseService inquiryDisburseService;
  private final DisbursementLogRepository disbursementLogRepository;
  private final CwrRemoteService cwrRemoteService;
  private final CwrRepository cwrRepository;

  public FinancingService(AgreementRepository agreementRepository,
                          FinancingHdrRepository financingHdrRepository,
                          InquiryDisburseService inquiryDisburseService,
                          DisbursementLogRepository disbursementLogRepository,
                          CwrRemoteService cwrRemoteService,
                          CwrRepository cwrRepository) {
    this.agreementRepository = agreementRepository;
    this.financingHdrRepository = financingHdrRepository;
    this.inquiryDisburseService = inquiryDisburseService;
    this.disbursementLogRepository = disbursementLogRepository;
    this.cwrRemoteService = cwrRemoteService;
    this.cwrRepository = cwrRepository;
  }

  public void recallApprovalStatus() {
    //find all aggremmnet with flag false or null
    List<Agreement> list = agreementRepository.viewApprovalStatusNoPending();//viewApprovalStatusPending();
    if (list != null && !list.isEmpty()) {
      for (Agreement agreement : list) {
        UpdateFinancingStatusRequest updateFinancingStatusRequest = null;
        try {
          updateFinancingStatusRequest = UpdateFinancingStatusRequest.builder()
            .vendorCode(agreement.getFinancingHdr().getCustomer().getCustExternalCode())
            .financingCode(agreement.getFinancingHdr().getFinancingHdrCode().toString())
            .status(UpdateFinancingStatusRequest.Status.Approved)
            .build();
        } catch (Exception ignored) {
        }
        try {
          //stop bila sudah 200
          //financingRemoteService.updateFinancingStatus(updateFinancingStatusRequest);
          //agreement.setApprovalFlag("true");
        } catch (Exception ignored) {
        }

        try {
          agreement = updateFromConfin(agreement);
        } catch (Exception ignored) {
        }

        try {
          updateFinStatusLiveIfGoLive(agreement);
        } catch (Exception ignored) {
        }

        try {
          //stop bila saudha disbur(di log disb ada)
          List<DisbursementLog> disbursementLogs = disbursementLogRepository.findAllByAgreement(agreement);
          if (disbursementLogs.isEmpty()) {
            inquiryDisburseService.inquiryDisburseAuto(agreement);
            //call api sbu inquiryDisburse


          }
        } catch (Exception ignored) {
        }


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
    } catch (Exception e) {
    }

    return agreement;
  }

  public void updateFinStatusLiveIfGoLive(Agreement agreement) {
    if (agreement.getStatus().equalsIgnoreCase("Live")) {//Ready Golive
      Optional<FinancingHdr> financingHdrO = financingHdrRepository.findByFinancingHdrCode(agreement.getFinancingHdr().getFinancingHdrCode());
      if (financingHdrO.isPresent() && financingHdrO.get().getFinancingStatus().equalsIgnoreCase("INPROCESS")) {
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
    List<FinancingHdr> financingHdrs = financingHdrRepository.findAllByFinancingStatusAndFinancingStep("INPROCESS", "SIGNING");
    financingHdrs.forEach(financingHdr -> {


    });


    //lihat fi aggement statusnya (Ready Golive) bila iya, updat ejadio LIVE, GOLIVE
    List<Agreement> list = agreementRepository.viewApprovalStatusNoPending();
  }

  public void recallCWRStatus() {
    //find all cwr with flag false or null
    List<String> cwrIn = new ArrayList<>();
    cwrIn.add("ACTIVE");
    cwrIn.add("NEW");
    List<Cwr> list = cwrRepository.findAllByStatusIsIn(cwrIn);//REJECT, CANCEL atau EXPIRED

    if (list != null && !list.isEmpty()) {
      for (Cwr cwr : list) {
        try {
          BaseMstRemoteResponseDto<List<InquiryCwrRemoteDto>> response = cwrRemoteService.inquiryCwr(
            InquiryCwrRemoteRequest.builder()
              .cwrNo(cwr.getCwrCode())
              .build()
          );
          final List<InquiryCwrRemoteDto> data = response.getData();
          if (data != null && !data.isEmpty()) {
            InquiryCwrDto cwrNo = InquiryCwrDto.builder()
              .cwrStartDate(DateTimeUtils.cSharpTimeStampToDate(data.getFirst().getStartDt()))
              .cwrEndDate(DateTimeUtils.cSharpTimeStampToDate(data.getFirst().getEndDt()))
              .cwrCode(cwr.getCwrCode())
              .loanAmt(BigDecimal.valueOf(data.getFirst().getRealisationAmt()))
              .plafondAmt(BigDecimal.valueOf(data.getFirst().getPlafondAmt()))
              .currency(data.getFirst().getCurrency())


              .realisationAmt(BigDecimal.valueOf(data.getFirst().getRealisationAmt()))
              .status(data.getFirst().getCwrStatDescr())
              .build();

            //StartDt, EndDt, CurrStep, PlafondAmt, CwrStat dan RealisationAmt
            cwr.setCwrStartDate(Utils.toInstant(cwrNo.getCwrStartDate()));
            cwr.setCwrEndDate(Utils.toInstant(cwrNo.getCwrEndDate()));
            cwr.setPlafondAmt(cwrNo.getPlafondAmt().doubleValue());

            cwr.setRealisationAmt(cwrNo.getRealisationAmt().doubleValue());
            cwr.setStatus(cwrNo.getStatus());
            cwrRepository.save(cwr);
          }
        } catch (Exception ignored) {
        }
      }
    }
  }
}
