package com.kmkbe.modules.loan_submission.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.kmkbe.core.domain.dto.*;
import com.kmkbe.core.domain.entity.*;
import com.kmkbe.core.domain.repository.*;
import com.kmkbe.core.security.CurrentUserService;
import com.kmkbe.core.utils.DateTimeUtils;
import com.kmkbe.helpers.constant.AppConstants;
import com.kmkbe.modules.remote.request.InquiryAgreementRemoteRequest;
import com.kmkbe.modules.remote.request.InquiryCwrRemoteRequest;
import com.kmkbe.modules.remote.service.CwrRemoteService;
import com.kmkbe.helpers.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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
  private final CurrentUserService currentUserService;

  public FinancingService(AgreementRepository agreementRepository,
                          FinancingHdrRepository financingHdrRepository,
                          InquiryDisburseService inquiryDisburseService,
                          DisbursementLogRepository disbursementLogRepository,
                          CwrRemoteService cwrRemoteService,
                          CwrRepository cwrRepository, CurrentUserService currentUserService) {
    this.agreementRepository = agreementRepository;
    this.financingHdrRepository = financingHdrRepository;
    this.inquiryDisburseService = inquiryDisburseService;
    this.disbursementLogRepository = disbursementLogRepository;
    this.cwrRemoteService = cwrRemoteService;
    this.cwrRepository = cwrRepository;
    this.currentUserService = currentUserService;
  }

  public void recallApprovalStatus() {
    log.info("Find all agreement from confins");
    List<Agreement> list = agreementRepository.viewApprovalStatusNoPending();
    if (list != null && !list.isEmpty()) {
      for (Agreement agreement : list) {
        try {
          log.info("Process update agreement {} ", agreement);
          agreement = updateFromConfin(agreement);

          log.info("Process update agreement Golive {} ", agreement);
          updateFinStatusLiveIfGoLive(agreement);

          //stop bila saudha disbur(di log disb ada)
          log.info("Process update agreement {} ", agreement);
          List<DisbursementLog> disbursementLogs = disbursementLogRepository.findAllByAgreement(agreement);
          if (disbursementLogs.isEmpty()) {
            log.info("Inquiry disbursement log agreement {} ", agreement);
            inquiryDisburseService.inquiryDisburseAuto(agreement);
          }
          log.info("Update agreement {} ", agreement);
          agreementRepository.save(agreement);
        } catch (Exception ignored) {
          log.info("Error process updatebn to confins {} ", agreement);
          ignored.printStackTrace();
        }
      }
    }
  }

  public Agreement updateFromConfin(Agreement agreement) throws JsonProcessingException {
    log.info("Process inquiry agreement to confins {} ", agreement);
    final List<InquiryAgreementCwrDto> data;
    BaseMstRemoteResponseDto<List<InquiryAgreementCwrDto>> response = cwrRemoteService.inquiryAgreementByNoAgreement(
      InquiryAgreementRemoteRequest.builder()
        .agreementNo(agreement.getAgreementCode())
        .build()
    );

    data = response.getData();
    log.info("Process inquiry agreement to confins response {} ", data);

    for (InquiryAgreementCwrDto inquiryAgreement : data) {
      agreement.setStatus(inquiryAgreement.getStatus());
      agreement.setCurrency(inquiryAgreement.getCurrency());
      agreement.setFinancingAmt(inquiryAgreement.getNtfAmt());
      agreement.setProductOffering(inquiryAgreement.getProductOffering());
      agreement.setFacility(inquiryAgreement.getFacility());
      agreement.setDtmUpd(DateTimeUtils.now());
      agreement.setUsrUpd(currentUserService.usernameOrDefault(AppConstants.CREATOR_CONFINS));
    }
    return agreement;
  }

  public void updateFinStatusLiveIfGoLive(Agreement agreement) {
    log.info("Process update status if Golive agreement from confins {} ", agreement);
    if (agreement.getStatus().equalsIgnoreCase("Live")) {
      Optional<FinancingHdr> financingHdrO = financingHdrRepository.findByFinancingHdrCode(agreement.getFinancingHdr().getFinancingHdrCode());
      if (financingHdrO.isPresent() && financingHdrO.get().getFinancingStatus().equalsIgnoreCase("INPROCESS") || financingHdrO.isPresent() && financingHdrO.get().getFinancingStatus().equalsIgnoreCase("LIVE")) {
        FinancingHdr financingHdr = financingHdrO.get();
        financingHdr.setFinancingStatus("LIVE");
        financingHdr.setFinancingStep(!Objects.equals(financingHdr.getFinancingStep(), "PAID") ?"GOLIVE":financingHdr.getFinancingStep());
        financingHdr.setDtmUpd(LocalDateTime.now());
        financingHdr.setUsrUpd(currentUserService.usernameOrDefault(AppConstants.CREATOR_CONFINS));
        financingHdrRepository.save(financingHdr);

        log.info("Process done status if Golive agreement from confins {} ", agreement);
      }
    }
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
            cwr.setUsrUpd(currentUserService.usernameOrDefault(AppConstants.CREATOR_CONFINS));
            cwr.setDtmUpd(LocalDateTime.now());
            cwrRepository.save(cwr);
          }
        } catch (Exception ignored) {
        }
      }
    }
  }
}
