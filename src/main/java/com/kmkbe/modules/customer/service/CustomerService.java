package com.kmkbe.modules.customer.service;

import com.kmkbe.core.domain.constant.AuditAction;
import com.kmkbe.core.domain.constant.CustomerIdType;
import com.kmkbe.core.domain.constant.CustomerType;
import com.kmkbe.core.domain.dto.*;
import com.kmkbe.exception.BusinessException;
import com.kmkbe.helpers.base.BasePaginationRequest;
import com.kmkbe.helpers.base.BaseResponseBuilder;
import com.kmkbe.helpers.constant.AppConstants;
import com.kmkbe.helpers.constant.ErrorConstant;
import com.kmkbe.helpers.utils.PageableUtil;
import com.kmkbe.modules.bouwheer.model.entity.Bouwheer;
import com.kmkbe.modules.customer.model.entity.Customer;
import com.kmkbe.core.domain.entity.FinancingHdr;
import com.kmkbe.modules.customer.model.response.CustomerResponse;
import com.kmkbe.modules.customer.model.response.PageCustomerResponse;
import com.kmkbe.modules.customer.repository.CustomerRepository;
import com.kmkbe.core.domain.repository.FinancingHdrRepository;
import com.kmkbe.core.enums.ApprovalStatus;
import com.kmkbe.core.utils.DateTimeUtils;
import com.kmkbe.core.utils.FormatingUtils;
import com.kmkbe.helpers.utils.CommonUtils;
import com.kmkbe.helpers.base.BaseResponse;
import com.kmkbe.modules.common.service.EmailService;
import com.kmkbe.modules.common.service.AuditTrailService;
import com.kmkbe.modules.customer.model.request.ApprovalRequest;
import com.kmkbe.modules.customer.model.request.SignUpRequest;
import com.kmkbe.modules.customer.model.request.UpdateCustomerRequest;
import com.kmkbe.modules.customer.model.request.UpdateFapRequest;
import jakarta.mail.MessagingException;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SignatureException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class CustomerService {
  private final CustomerRepository customerRepository;
  private final BCryptPasswordEncoder bcryptEncoder;
  private final JdbcTemplate jdbcTemplate;
  private final FinancingHdrRepository financingHdrRepository;
  private final EmailService emailService;
  private final AuditTrailService auditTrailService;

  public CustomerService(CustomerRepository customerRepository,
                         BCryptPasswordEncoder bcryptEncoder,
                         JdbcTemplate jdbcTemplate,
                         FinancingHdrRepository financingHdrRepository,
                         EmailService emailService,
                         AuditTrailService auditTrailService) {
    this.customerRepository = customerRepository;
    this.bcryptEncoder = bcryptEncoder;
    this.jdbcTemplate = jdbcTemplate;
    this.financingHdrRepository = financingHdrRepository;
    this.emailService = emailService;
    this.auditTrailService = auditTrailService;
  }

  public Customer create(SignUpRequest request, CustomerType type) {

    // Validate duplicate email ID
    Optional<Customer> customerOptional = customerRepository.findByCustEmail(request.getEmail());
    if (customerOptional.isPresent() && customerOptional.get().isActive()) {
      log.info(ErrorConstant.ERROR_MESSAGE_84 + "{}", request.getVendorId());
      throw new BusinessException(HttpStatus.CONFLICT, ErrorConstant.ERROR_CODE_84, ErrorConstant.ERROR_MESSAGE_84 + "Email vendor has been register! "+customerOptional.get().getCustName());
    }

    // Validate duplicate vendor ID
    if (customerOptional.isPresent() && customerOptional.get().getCustExternalCode().equals(request.getVendorCode())) {
      log.info(ErrorConstant.ERROR_MESSAGE_84 + "{}", request.getVendorId());
      throw new BusinessException(HttpStatus.CONFLICT, ErrorConstant.ERROR_CODE_84, ErrorConstant.ERROR_MESSAGE_84 + "Vendor ID has been register! "+customerOptional.get().getCustName());
    }


    if (!request.isAgreeTc()) {
      log.info(ErrorConstant.ERROR_MESSAGE_80 + "{}", false);
      throw new BusinessException(HttpStatus.CONFLICT, ErrorConstant.ERROR_CODE_80, "Setujui Syarat dan Ketentuan for sign up");
    }


//  CustomerUtils.clearCustomerInactiveData(jdbcTemplate, find.get());
    final String encodePin = bcryptEncoder.encode(request.getPin());

    Customer customer = new Customer();
    CustomerAuditData before = null;
    if(customerOptional.isPresent() && !customerOptional.get().isActive()){
      log.info(ErrorConstant.ERROR_MESSAGE_80 + "{} Update Customer ", request.getVendorId());
      customer = customerOptional.get();
      before = toAuditData(customer);
    }else{
      log.info(ErrorConstant.ERROR_MESSAGE_80 + "{} Create Customer ", request.getVendorId());
      customer.setCustCode(UUID.randomUUID());
    }
    customer.setCustName(request.getName());
    customer.setCustEmail(request.getEmail().toLowerCase());

    if (request.getCustomerNo() != null && !request.getCustomerNo().isEmpty()) {
      customer.setCustNo(request.getCustomerNo());
    }

    boolean isCompany = (type == CustomerType.Company);

// 1. Set ID Type Code cleanly using a ternary operator
    customer.setCustIdTypeCode(isCompany ? CustomerIdType.NPWP.name() : CustomerIdType.KTP.name());

// 2. Single KTP length check block (removed empty/commented code)
    if (!isCompany && request.getCustomerIdNo() != null && request.getCustomerIdNo().length() != 16) {
      log.info(ErrorConstant.ERROR_MESSAGE_80 + "{}", request.getCustomerIdNo());
      throw new BusinessException(HttpStatus.CONFLICT, ErrorConstant.ERROR_CODE_80, "KTP minimal dan maksimal 16 Karakter");
    }

    customer.setCustTypeCode(type.name());
    customer.setCustIdNo(request.getCustomerIdNo());
    customer.setCustMobilePhone(FormatingUtils.formatOnlyNumber(request.getMobilePhone()));
    customer.setAgreeTc(request.isAgreeTc());
    customer.setCustPin(encodePin);
    customer.setIsEmailValid(false);
    customer.setBouwheer(request.getBouwheerCode());
    customer.setApprovalStatus(String.valueOf(ApprovalStatus.OPEN));
    customer.setActive(false);
    if (request.getVendorCode() != null && !request.getVendorCode().isEmpty()) {
      customer.setCustExternalCode(request.getVendorCode());
    }
    customer.setUsrCrt(customer.getCustName());
    customer.setDtmCrt(DateTimeUtils.now());
    Customer saved = customerRepository.save(customer);
    auditTrailService.record(
      "CUSTOMER",
      before == null ? AuditAction.CREATE : AuditAction.UPDATE,
      "Customer",
      saved.getCustCode(),
      before,
      toAuditData(saved)
    );
    return saved;
  }

  public void activated(Customer customer) {
    CustomerAuditData before = toAuditData(customer);
    customer.setIsEmailValid(true);
    customer.setActive(true);
    customer.setUsrUpd(customer.getCustName());
    customer.setDtmUpd(DateTimeUtils.now());
    Customer saved = customerRepository.save(customer);
    auditTrailService.record("CUSTOMER", AuditAction.UPDATE, "Customer", saved.getCustCode(), before, toAuditData(saved));
  }

  public Customer update(
    Customer customer,
    UpdateCustomerRequest request
  ) throws SignatureException {
    try {
      boolean emailChanged = false;
      CustomerAuditData before = toAuditData(customer);

      String oldEmail = customer.getCustEmail();
      String newEmail = request.getCustEmail();

      if (newEmail != null && !oldEmail.equalsIgnoreCase(newEmail)) {
        boolean emailExists = customerRepository.existsByCustEmailIgnoreCaseAndCustIdNoNot(
          newEmail, customer.getCustIdNo()
        );
        if (emailExists) {
          throw new IllegalArgumentException("Email already exists, please use another one");
        }
        customer.setCustEmail(newEmail);
        emailChanged = true;
      }

      customer.setCustName(request.getCustName());
      //customer.setCustTypeCode(request.getCustTypeCode());
      customer.setCustIdNo(request.getCustIdNo());
      customer.setNpwp(request.getNpwp());
      try {
        customer = customerRepository.save(customer);
        auditTrailService.record("CUSTOMER", AuditAction.UPDATE, "Customer", customer.getCustCode(), before, toAuditData(customer));
      } catch (DataIntegrityViolationException e) {
        throw new IllegalArgumentException("Email already exists, please use another one");
      }
      customer.setForceLogout(emailChanged);

      return customer;
    } catch (Exception e) {
      log.error("update, error {}", e.getMessage());
      throw e;
    }
  }

  public ProfileFapDto prolifeFAP(HttpServletRequest request) {
    String financingHdrCode = request.getParameter("financingHdrCode");


    return null;
  }

  public ProfileSITDto prolifeSIT(HttpServletRequest request) {
    String financingHdrCode = request.getParameter("financingHdrCode");

    Optional<FinancingHdr> financingHdr = financingHdrRepository.findByFinancingHdrCode(UUID.fromString(financingHdrCode));
    if (financingHdr.isPresent()) {
      FinancingHdr hdr = financingHdr.get();
      hdr.getCustomer().getCustName();
      if (hdr.getCustomer().getCustTypeCode().equalsIgnoreCase("")) {
        hdr.getCustomer().getCompany().getDirectorName();
      }

    }
    return ProfileSITDto.builder()
      .rt("")
      .rt("")
      .namaBank("")
      .build();
  }

  public void updateFapData(UpdateFapRequest request) {
    String email = request.getEmail();

    Optional<Customer> customerOptional = customerRepository.findByCustEmail(email);
    if (customerOptional.isPresent()) {
      Customer customer = customerOptional.get();

      UUID custCode = customer.getCustCode();

      Pageable pageable = PageRequest.of(0, 1);
      List<FinancingHdr> financingHdrList = financingHdrRepository.findLatestFinancingHdrByCustCode(custCode, pageable);

      if (!financingHdrList.isEmpty()) {
        FinancingHdr financingHdr = financingHdrList.get(0);

        financingHdr.setFapDate(request.getFapDate());
        financingHdr.setFapStatus(request.getFapStatus());

        financingHdrRepository.save(financingHdr);
      } else {
        throw new IllegalArgumentException("No FinancingHdr found for custCode: " + custCode);
      }
    } else {
      throw new IllegalArgumentException("Customer with email " + email + " not found");
    }
  }


  /**
   *
   * @param request
   * @return
   */
  public BaseResponseBuilder<PageCustomerResponse> pages(
    BasePaginationRequest request) {
    String sortBy = request.getSortBy() != null && !request.getSortBy().isEmpty() ? request.getSortBy() : "custName";
    Pageable pageable = PageableUtil.createPageRequest(request, request.getPageSize(), request.getPageNo(),
      sortBy, request.getSortType());

    Page<Customer> page = customerRepository.findAll((Root<Customer> root, CriteriaQuery<?> query, CriteriaBuilder builder) ->
      builder.and(builder.like(root.get(request.getSearchBy()), '%' + request.getSearchValue() + '%')), pageable);

    List<CustomerResponse> responses = page.getContent().stream().map(item -> {
      CustomerResponse response = new CustomerResponse();
      response.setCustCode(item.getCustCode());
      response.setCustNo(item.getCustNo());
      response.setCustName(item.getCustName());
      response.setCustTypeCode(item.getCustTypeCode());
      response.setCustIdNo(item.getCustIdNo());
      response.setCustEmail(item.getCustEmail());
      response.setIsEmailValid(item.getIsEmailValid());
      response.setCustMobilePhone(item.getCustMobilePhone());
      response.setIsPhoneValid(item.getIsPhoneValid());
      response.setIsWaActive(item.getIsWaActive());
      response.setAgreeTc(item.getAgreeTc());
      response.setAgreeLegalShare(item.getAgreeLegalShare());
      response.setCustExternalCode(item.getCustExternalCode());
      response.setIsActive(item.isActive());
      response.setDtmCrt(item.getDtmCrt());
      response.setForceLogout(item.getForceLogout());
      response.setBouwheerCode(String.valueOf(item.getBouwheer()));
//      response.setBouwheerName(Optional.ofNullable(item.getBouwheerDetail())
//        .map(Bouwheer::getBouwheerName)
//        .orElse(null));
      response.setApprovalStatus(item.getApprovalStatus());
      response.setApprovalNote(item.getApprovalNote());
      response.setApprovalBy(item.getApprovalBy());
      response.setApprovalAt(item.getApprovalAt());
      return response;
    }).toList();

    return new BaseResponseBuilder<>(true, AppConstants.CODE_OK, AppConstants.PROCESS_SUCCESSFULLY, PageCustomerResponse.builder()
      .content(responses)
      .pagination(PageableUtil.pageToPagination(page))
      .build());
  }

  /**
   *
   * @param custCode
   * @return
   */
  public BaseResponseBuilder<CustomerResponse> findByCustomerCode(String custCode) {
    Optional<Customer> customerOptional = customerRepository.findByCustCode(UUID.fromString(custCode));
    if (customerOptional.isEmpty()) {
      log.info(ErrorConstant.ERROR_MESSAGE_81 + "{}", custCode);
      throw new BusinessException(HttpStatus.CONFLICT, ErrorConstant.ERROR_CODE_81, ErrorConstant.ERROR_MESSAGE_81);
    }

    Customer customer = customerOptional.get();
    CustomerResponse response = new CustomerResponse();
    BeanUtils.copyProperties(customer, response);
//    response.setBouwheerName(Optional.ofNullable(customer.getBouwheerDetail())
//      .map(Bouwheer::getBouwheerName)
//      .orElse(null));

    return new BaseResponseBuilder<>(true, AppConstants.CODE_OK, AppConstants.PROCESS_SUCCESSFULLY, response);
  }

  /**
   *
   * @param request
   * @return
   * @throws MessagingException
   */
  public BaseResponse approval(ApprovalRequest request, String username) {
    Optional<Customer> customerOptional = customerRepository.findByCustCode(request.getCustCode());
    if (customerOptional.isEmpty()) {
      log.info(ErrorConstant.ERROR_MESSAGE_81 + "{}", request.getCustCode());
      throw new BusinessException(HttpStatus.CONFLICT, ErrorConstant.ERROR_CODE_81, "Customer not found");
    }

    Customer customer = customerOptional.get();
    if (!customer.getApprovalStatus().equals("OPEN")) {
      log.info(ErrorConstant.ERROR_MESSAGE_80 + "{}", request.getApprovalStatus());
      throw new BusinessException(HttpStatus.CONFLICT, ErrorConstant.ERROR_CODE_80, "Customer has been process with status is ");
    }

    CustomerAuditData before = toAuditData(customer);
    String approvalStatus = request.getApprovalStatus().toUpperCase().trim();
    customer.setApprovalStatus(approvalStatus);
    customer.setActive("APPROVED".equals(approvalStatus));
    customer.setApprovalNote(request.getApprovalNote());
    customer.setApprovalBy(username);
    customer.setApprovalAt(DateTimeUtils.now());
    Customer saved = customerRepository.save(customer);
    auditTrailService.record(
      "CUSTOMER",
      "APPROVED".equals(approvalStatus) ? AuditAction.APPROVE : AuditAction.REJECT,
      "Customer",
      saved.getCustCode(),
      before,
      toAuditData(saved)
    );

    // Send mail
    emailService.customerVerification(customer.getCustEmail().toLowerCase(), customer.getCustName(), customer.getCustIdNo(), CommonUtils.generateOtp());

    return new BaseResponseBuilder<>(true, AppConstants.CODE_OK, AppConstants.PROCESS_SUCCESSFULLY);
  }

  private CustomerAuditData toAuditData(Customer customer) {
    if (customer == null) {
      return null;
    }

    return new CustomerAuditData(
      customer.getCustCode(),
      customer.getCustNo(),
      customer.getCustName(),
      customer.getCustTypeCode(),
      customer.getCustIdTypeCode(),
      customer.getCustIdNo(),
      customer.getCustEmail(),
      customer.getCustMobilePhone(),
      customer.getIsEmailValid(),
      customer.getIsPhoneValid(),
      customer.getIsWaActive(),
      customer.getAgreeTc(),
      customer.getAgreeLegalShare(),
      customer.getCustExternalCode(),
      customer.isActive(),
      customer.getBouwheer(),
      customer.getApprovalStatus(),
      customer.getApprovalNote(),
      customer.getApprovalBy(),
      customer.getApprovalAt(),
      customer.getNpwp()
    );
  }

  private record CustomerAuditData(
    UUID custCode,
    String custNo,
    String custName,
    String custTypeCode,
    String custIdTypeCode,
    String custIdNo,
    String custEmail,
    String custMobilePhone,
    Boolean emailValid,
    Boolean phoneValid,
    Boolean waActive,
    Boolean agreeTc,
    Boolean agreeLegalShare,
    String custExternalCode,
    boolean active,
    String bouwheer,
    String approvalStatus,
    String approvalNote,
    String approvalBy,
    LocalDateTime approvalAt,
    String npwp
  ) {
  }
}
