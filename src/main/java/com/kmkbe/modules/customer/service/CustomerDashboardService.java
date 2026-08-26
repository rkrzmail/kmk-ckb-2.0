package com.kmkbe.modules.customer.service;

import com.kmkbe.core.domain.dto.*;
import com.kmkbe.core.domain.entity.*;
import com.kmkbe.core.domain.repository.*;
import com.kmkbe.core.security.CurrentUserService;
import com.kmkbe.core.utils.FormatingUtils;
import com.kmkbe.exception.BusinessException;
import com.kmkbe.helpers.base.BaseResponseBuilder;
import com.kmkbe.helpers.constant.AppConstants;
import com.kmkbe.helpers.constant.ErrorConstant;
import com.kmkbe.modules.customer.model.entity.Customer;
import com.kmkbe.modules.loan_submission.service.FinancingHdrService;
import com.kmkbe.helpers.utils.Utils;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.security.SignatureException;
import java.util.*;

@Service
@Slf4j
public class CustomerDashboardService {
  private final FinancingHdrService financingHdrService;
  private final CwrRepository cwrRepository;
  private final EntityManager entityManager;
  private final AgreementRepository agreementRepository;
  private final NotifDebtorRepository notifDebtorRepository;
  private final AgreementFileSigningRepository agreementFileSigningRepository;
  private final FinancingHdrRepository financingHdrRepository;
  private final CurrentUserService currentUserService;

  public CustomerDashboardService(FinancingHdrService financingHdrService,
                                  CwrRepository cwrRepository,
                                  EntityManager entityManager,
                                  AgreementRepository agreementRepository,
                                  NotifDebtorRepository notifDebtorRepository,
                                  AgreementFileSigningRepository agreementFileSigningRepository,
                                  FinancingHdrRepository financingHdrRepository,
                                  CurrentUserService currentUserService) {
    this.financingHdrService = financingHdrService;
    this.cwrRepository = cwrRepository;
    this.entityManager = entityManager;
    this.agreementRepository = agreementRepository;
    this.notifDebtorRepository = notifDebtorRepository;
    this.agreementFileSigningRepository = agreementFileSigningRepository;
    this.financingHdrRepository = financingHdrRepository;
    this.currentUserService = currentUserService;
  }

  /**
   *
   * @return
   * @throws SignatureException
   */
  public BaseResponseBuilder<CustomerPlafondDto> plafond() throws SignatureException {
    Customer customer = currentUserService.customer();
    Optional<FinancingHdr> financingHdrOptional = financingHdrRepository.findFirstByCustomerOrderByFinancingHdrIdDesc(customer);
    if (financingHdrOptional.isEmpty()) {
      return new BaseResponseBuilder<>(true, AppConstants.CODE_OK, AppConstants.PROCESS_SUCCESSFULLY, CustomerPlafondDto.builder()
        .custCode(customer.getCustCode())
        .custName(customer.getCustName())
        .custIdTypeCode(customer.getCustIdTypeCode())
        .custIdNo(customer.getCustIdNo())
        .email(customer.getCustEmail())
        .custTypeCode(customer.getCustTypeCode())
        .plafond(CustomerPlafondDto.PlafondDto.builder()
          .plafond(BigDecimal.ZERO)
          .totalPlafond(BigDecimal.ZERO)
          .availablePlafond(BigDecimal.ZERO)
          .jumlahInvoice(BigDecimal.ZERO)
          .build())
        .build());
    }
    return plafondByFinancingHdrCode(financingHdrOptional.get().getFinancingHdrCode().toString());
  }

  /**
   *
   * @param financingHdrCode
   * @return
   */
  public BaseResponseBuilder<CustomerPlafondDto> plafondByFinancingHdrCode(String financingHdrCode) {
    Optional<FinancingHdr> financingHdrOptional = financingHdrRepository.findByFinancingHdrCode(UUID.fromString(financingHdrCode));

    if (financingHdrOptional.isEmpty()) {
      log.info(ErrorConstant.ERROR_MESSAGE_81 + "{}", financingHdrCode);
      throw new BusinessException(HttpStatus.CONFLICT, ErrorConstant.ERROR_CODE_81, "Customer Financing HDR  not found");
    }

    FinancingHdr financingHdr = financingHdrOptional.get();
    final String address;
    final String phoneNo;
    if (financingHdr.getCustomer().getCustTypeCode().equalsIgnoreCase("company")) {
      address = financingHdr.getCustomer().getCompany() == null ? "" : String.valueOf(financingHdr.getCustomer().getCompany().getCompanyAddress());
      phoneNo = financingHdr.getCustomer().getCompany() == null ? "" : financingHdr.getCustomer().getCompany().getPhone();
    } else {
      address = financingHdr.getCustomer().getPersonal() == null ? "" : String.valueOf(financingHdr.getCustomer().getPersonal().getLegalAddress());
      phoneNo = financingHdr.getCustomer().getPersonal() == null ? "" : financingHdr.getCustomer().getPersonal().getPhone();
    }

    double plafond = 0;
    double availableplafond = 0;
    double plafondTotal = 0;
    String vaidateLimit = null;
    double jumlahivoice = 0;

    Page<Cwr> page = cwrRepository.findAllByCustomerOrderByDtmUpdDescUsrCrtDesc(
      financingHdr.getCustomer(),
      PageRequest.of(0, 10)
    );

    List<Cwr> cwrs = page.stream().toList();
    if (!cwrs.isEmpty()) {
      plafondTotal = cwrs.getFirst().getPlafondAmt();
      plafond = cwrs.getFirst().getRealisationAmt();
      availableplafond = plafondTotal - plafond;
      if (cwrs.getFirst().getCwrEndDate() != null) {
        vaidateLimit = String.valueOf(cwrs.getFirst().getCwrEndDate().toLocalDate());
      }
      List<Agreement> agreements = agreementRepository.findAllByCwr(cwrs.getFirst());
      for (Agreement agreement : agreements) {
        jumlahivoice = jumlahivoice + agreement.getFinancingAmt();
      }
    }

    return new BaseResponseBuilder<>(true, AppConstants.CODE_OK, AppConstants.PROCESS_SUCCESSFULLY,CustomerPlafondDto.builder()
      .financingHdrCode(financingHdr.getFinancingHdrCode())
      .bouwheerCode(financingHdr.getBouwheer().getBouwheerCode())
      .bouwheerName(financingHdr.getBouwheer().getBouwheerName())
      .custCode(financingHdr.getCustomer().getCustCode())
      .custName(financingHdr.getCustomer().getCustName())
      .custIdTypeCode(financingHdr.getCustomer().getCustIdTypeCode())
      .custIdNo(financingHdr.getCustomer().getCustIdNo())
      .email(financingHdr.getCustomer().getCustEmail())
      .custTypeCode(financingHdr.getCustomer().getCustTypeCode())
      .address(address)
      .phoneNo(phoneNo)
      .plafond(CustomerPlafondDto.PlafondDto.builder()
        .plafond(BigDecimal.valueOf(plafond))
        .totalPlafond(BigDecimal.valueOf(plafondTotal))
        .availablePlafond(BigDecimal.valueOf(availableplafond))
        .validityLimitData(String.valueOf(vaidateLimit))
        .jumlahInvoice(BigDecimal.valueOf(jumlahivoice))
        .build())
      .build());
  }

  /**
   *
   * @return
   * @throws SignatureException
   */
  public BaseResponseBuilder<CustomerDashboardDto> mainDashboard() throws SignatureException {
     Customer authenticatedCustomer = currentUserService.customer();
      Optional<Cwr> lastOptional = cwrRepository.findTopByCustomerOrderByDtmUpdDescUsrCrtDesc(authenticatedCustomer);
      if (lastOptional.isEmpty()) {
        return new BaseResponseBuilder<>(true, AppConstants.CODE_OK, AppConstants.PROCESS_SUCCESSFULLY,new CustomerDashboardDto());
      }

      Cwr lastCwr = lastOptional.get();
      final FormatingUtils.CurrencyFormatter plafond = new FormatingUtils.CurrencyFormatter(lastCwr.getPlafondAmt());
      final FormatingUtils.CurrencyFormatter used = new FormatingUtils.CurrencyFormatter(lastCwr.getRealisationAmt());
      final FormatingUtils.CurrencyFormatter available = new FormatingUtils.CurrencyFormatter((lastCwr.getPlafondAmt() - lastCwr.getRealisationAmt()));

      return new BaseResponseBuilder<>(true, AppConstants.CODE_OK, AppConstants.PROCESS_SUCCESSFULLY,CustomerDashboardDto.builder()
        .totalPlafond(plafond.getValue())
        .totalPlafondUnit(plafond.getUnit())
        .totalPlafondUsed(used.getValue())
        .totalPlafondUsedUnit(used.getUnit())
        .totalAvailablePlafond(available.getValue())
        .totalAvailablePlafondUnit(available.getUnit())
        .validityLimitDate(Utils.fromInstant(lastCwr.getCwrEndDate()))
        .totalInvoiceFounded(agreementRepository.countInvoiceFundedByCustCode(authenticatedCustomer.getCustCode()))
        .build());
  }


  public CustomerDashboardDto.Agreement agreementDashboard() throws SignatureException {
    try {

      return new CustomerDashboardDto.Agreement();
    } catch (Exception e) {
      log.error("agreementDashboard: error {}", e.getMessage());
      throw e;
    }
  }

  public CustomerPerjanjianDto perjanjian(String financingHdrCode) {
    try {
      FinancingHdr financingHdr = financingHdrService.findByCode(financingHdrCode);

      UUID uuid = UUID.fromString(financingHdrCode);
      Agreement agreement = agreementRepository.findAgreement(uuid)
        .orElseThrow(() -> new RuntimeException("Agreement not found"));

      final String address, phoneNo;
      if (financingHdr.getCustomer().getCustTypeCode().equalsIgnoreCase("company")) {
        address = financingHdr.getCustomer().getCompany() == null ? "" : String.valueOf(financingHdr.getCustomer().getCompany().getCompanyAddress());
        phoneNo = financingHdr.getCustomer().getCompany() == null ? "" : financingHdr.getCustomer().getCompany().getPhone();
      } else {
        address = financingHdr.getCustomer().getPersonal() == null ? "" : String.valueOf(financingHdr.getCustomer().getPersonal().getLegalAddress());
        phoneNo = financingHdr.getCustomer().getPersonal() == null ? "" : financingHdr.getCustomer().getPersonal().getPhone();
      }
      List<String> signerNames = financingHdrRepository.findSignerNameByFinancingHdrCode(UUID.fromString(financingHdrCode));

      long total = signerNames.stream()
        .mapToLong(signer -> agreementFileSigningRepository.countBySigner(signer))
        .sum();
      Long totalBerjalan = financingHdrRepository.countSigningAndSigned(financingHdrCode);
      Long totalBerakhir = financingHdrRepository.countCompleted(financingHdrCode);

      return CustomerPerjanjianDto.builder()
        .financingHdrCode(financingHdr.getFinancingHdrCode())
        .bouwheerCode(financingHdr.getBouwheer().getBouwheerCode())
        .bouwheerName(financingHdr.getBouwheer().getBouwheerName())
        .custCode(financingHdr.getCustomer().getCustCode())
        .custName(financingHdr.getCustomer().getCustName())
        .custIdTypeCode(financingHdr.getCustomer().getCustIdTypeCode())
        .custIdNo(financingHdr.getCustomer().getCustIdNo())
        .email(financingHdr.getCustomer().getCustEmail())
        .custTypeCode(financingHdr.getCustomer().getCustTypeCode())
        .address(address)
        .phoneNo(phoneNo)
        .agreementCode(agreement.getAgreementCode())
        .perjanjian(CustomerPerjanjianDto.PerjanjianDto.builder()
          .perjanjianBerjalan(totalBerjalan != null ? totalBerjalan.intValue() : 0)
          .perjanjianBerakhir(totalBerakhir != null ? totalBerakhir.intValue() : 0)
          .totalPerjanjian((int) total)
          .build())

        .build();
    } catch (Exception e) {
      log.error("detailSubmissionDistribution: error {}", e.getMessage());
      throw e;
    }
  }

  public List<NotifDebtor> getNotifDebtors(String custCode) {
    return notifDebtorRepository.findByCustCode(custCode);
  }

  @Transactional
  public void deleteAllNotifDebtors(String custCode) {
    notifDebtorRepository.deleteByCustCode(custCode);
  }
}
