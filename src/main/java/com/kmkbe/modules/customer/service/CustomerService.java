package com.kmkbe.modules.customer.service;

import com.kmkbe.core.domain.constant.AuditAction;
import com.kmkbe.core.domain.constant.CustomerIdType;
import com.kmkbe.core.domain.constant.CustomerType;
import com.kmkbe.core.domain.dto.*;
import com.kmkbe.core.domain.mapper.CustomerMapper;
import com.kmkbe.core.domain.model.CommonResult;
import com.kmkbe.core.security.CurrentUserService;
import com.kmkbe.core.utils.FormatingUtils;
import com.kmkbe.exception.BusinessException;
import com.kmkbe.helpers.base.BasePaginationRequest;
import com.kmkbe.helpers.base.BaseResponseBuilder;
import com.kmkbe.helpers.constant.AppConstants;
import com.kmkbe.helpers.constant.ErrorConstant;
import com.kmkbe.helpers.utils.PageableUtil;
import com.kmkbe.modules.bouwheer.model.entity.Bouwheer;
import com.kmkbe.modules.bouwheer.repository.BouwheerRepository;
import com.kmkbe.modules.customer.model.dto.CustomerDto;
import com.kmkbe.modules.customer.model.entity.Customer;
import com.kmkbe.core.domain.entity.FinancingHdr;
import com.kmkbe.modules.customer.model.request.SignUpRequest;
import com.kmkbe.modules.customer.model.response.CustomerResponse;
import com.kmkbe.modules.customer.model.response.PageCustomerResponse;
import com.kmkbe.modules.customer.repository.CustomerRepository;
import com.kmkbe.core.domain.repository.FinancingHdrRepository;
import com.kmkbe.core.enums.ApprovalStatus;
import com.kmkbe.core.utils.DateTimeUtils;
import com.kmkbe.helpers.base.BaseResponse;
import com.kmkbe.modules.common.service.EmailService;
import com.kmkbe.modules.common.service.AuditTrailService;
import com.kmkbe.modules.customer.model.request.ApprovalRequest;
import com.kmkbe.modules.customer.model.request.UpdateCustomerRequest;
import com.kmkbe.modules.customer.model.request.UpdateFapRequest;
import com.kmkbe.modules.user.entity.MstEmployee;
import com.kmkbe.modules.user.repository.MstEmployeeRepository;
import jakarta.mail.MessagingException;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Root;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SignatureException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class CustomerService {
  private final CustomerRepository customerRepository;
  private final FinancingHdrRepository financingHdrRepository;
  private final EmailService emailService;
  private final AuditTrailService auditTrailService;
  private final BouwheerRepository bouwheerRepository;
  private final CurrentUserService currentUserService;
  private final MstEmployeeRepository mstEmployeeRepository;
  private final BCryptPasswordEncoder bCryptPasswordEncoderl;

  public CustomerService(CustomerRepository customerRepository,
                         FinancingHdrRepository financingHdrRepository,
                         EmailService emailService,
                         AuditTrailService auditTrailService,
                         BouwheerRepository bouwheerRepository,
                         CurrentUserService currentUserService,
                         MstEmployeeRepository mstEmployeeRepository,
                         BCryptPasswordEncoder bCryptPasswordEncoderl) {
    this.customerRepository = customerRepository;
    this.financingHdrRepository = financingHdrRepository;
    this.emailService = emailService;
    this.auditTrailService = auditTrailService;
    this.bouwheerRepository = bouwheerRepository;
    this.currentUserService = currentUserService;
    this.mstEmployeeRepository = mstEmployeeRepository;
    this.bCryptPasswordEncoderl = bCryptPasswordEncoderl;
  }

  /**
   * Get Customer detail
   *
   * @param request
   * @return
   * @throws SignatureException
   * @throws BadCredentialsException
   * @throws IllegalStateException
   * @throws IllegalAccessException
   */
  public CommonResult<CustomerDto> profile(
    HttpServletRequest request
  ) throws SignatureException, BadCredentialsException, IllegalStateException, IllegalAccessException {
    Customer customer;
    String custCode = String.valueOf(request.getParameter("custCode"));
    if (custCode.equalsIgnoreCase("null") || custCode.equalsIgnoreCase("")) {
      customer = currentUserService.customer();
    } else {
      Optional<Customer> customerOptional = customerRepository.findByCustCode(UUID.fromString(custCode));
      if (customerOptional.isPresent()) {
        customer = customerOptional.get();
      } else {
        throw new SignatureException("You are not authorized to access this resource");
      }
    }


    CustomerDto result = CustomerMapper.INSTANCE.custDtoFromEntity(customer);
    result.setNpwp(customer.getNpwp());

    if (customer.getCompany() != null) {
      result.setAddress(CustomerMapper.addressDtoFromCompany(customer.getCompany()));
      result.setCompany(CustomerMapper.INSTANCE.companyDtoFromEntity(customer.getCompany()));
      result.getAddress().setArea(customer.getCompany().getArea());
    } else if (customer.getPersonal() != null) {
      result.setAddress(CustomerMapper.addressDtoFromPersonal(customer.getPersonal()));
      result.setPersonal(CustomerMapper.INSTANCE.personalDtoFromEntity(customer.getPersonal()));
    }

    if (result.getAddress() != null && result.getAddress().getArea() == null) {
      result.getAddress().setArea("");
    }

    if (result.getCompany() != null && result.getCompany().getDirectorName() == null) {
      result.getCompany().setDirectorName("");
    }

    result.setBouwheerName(bouwheerRepository.findByBouwheerCode(customer.getBouwheer() != null ? UUID.fromString(customer.getBouwheer()) : null)
      .map(Bouwheer::getBouwheerName)
      .orElse(null));

    return new CommonResult<CustomerDto>().success(result);
  }

  @Transactional
  public Customer create(SignUpRequest request, CustomerType type) {

    /**
     * Validate Agree TC
     */
    if (!request.isAgreeTc()) {
      log.info(ErrorConstant.ERROR_MESSAGE_80 + "{}", false);
      throw new BusinessException(HttpStatus.CONFLICT, ErrorConstant.ERROR_CODE_80, "Setujui Syarat dan Ketentuan for sign up");
    }

    String inputEmail = request.getEmail().toLowerCase();
    String inputVendorCode = request.getVendorCode();

    /**
     * Check Email and vendor Code
     */
    Optional<Customer> customerByVendor = customerRepository.findFirstByCustExternalCode(inputVendorCode);
    Optional<Customer> customerByEmail = customerRepository.findByCustEmail(inputEmail);

    /**
     * Validate Email unique
     */
    if (customerByEmail.isPresent() && customerByEmail.get().isActive()) {
      Customer existingEmailOwner = customerByEmail.get();
      if (!existingEmailOwner.getCustExternalCode().equals(inputVendorCode)) {
        log.info("Email {} sudah digunakan oleh vendor lain: {}", inputEmail, existingEmailOwner.getCustExternalCode());
        throw new BusinessException(HttpStatus.CONFLICT, ErrorConstant.ERROR_CODE_84, "Email sudah terdaftar dengan vendor lain!");
      }
    }

    /**
     * Validate Email active
     */
    if (customerByVendor.isPresent()) {
      Customer existingVendor = customerByVendor.get();
      if (!existingVendor.getCustEmail().equals(inputEmail) && Boolean.TRUE.equals(existingVendor.getIsEmailValid())) {
        log.info("Vendor {} gagal update email karena email lama sudah terverifikasi valid", inputVendorCode);
        throw new BusinessException(HttpStatus.CONFLICT, ErrorConstant.ERROR_CODE_84, "Tidak bisa mengubah email yang sudah terverifikasi!");
      }
    }

    /**
     * Update Customer
     */
    final String encodePin = bCryptPasswordEncoderl.encode(request.getPin());
    Customer customer = new Customer();
    CustomerAuditData before = null;

    if (customerByVendor.isPresent()) {
      log.info(ErrorConstant.ERROR_MESSAGE_80 + "{} Update Customer ", request.getVendorCode());
      customer = customerByVendor.get();
      before = toAuditData(customer);
      customer.setIsEmailValid(false);
      customer.setApprovalStatus(String.valueOf(ApprovalStatus.OPEN));
      customer.setActive(false);
      customer.setApprovalNote(null);
      customer.setApprovalBy(null);
      customer.setApprovalAt(null);
      customer.setCustEmail(Boolean.TRUE.equals(customer.getIsEmailValid()) ? customer.getCustEmail() : request.getEmail().toLowerCase());
    } else {
      // CREATE
      log.info(ErrorConstant.ERROR_MESSAGE_80 + "{} Create Customer ", request.getVendorCode());
      customer.setCustCode(UUID.randomUUID());
      customer.setIsEmailValid(false);
      customer.setApprovalStatus(String.valueOf(ApprovalStatus.OPEN));
      customer.setActive(false);
      if (request.getVendorCode() != null && !request.getVendorCode().isEmpty()) {
        customer.setCustExternalCode(request.getVendorCode());
      }
      customer.setCustEmail(request.getEmail().toLowerCase());
    }
    /**
     * Set update data
     */
    customer.setCustName(request.getName());
    boolean isCompany = (type == CustomerType.Company);
    customer.setCustIdTypeCode(isCompany ? CustomerIdType.NPWP.name() : CustomerIdType.KTP.name());

    if (!isCompany && request.getCustomerIdNo() != null && request.getCustomerIdNo().length() != 16) {
      log.info(ErrorConstant.ERROR_MESSAGE_80 + "{}", request.getCustomerIdNo());
      throw new BusinessException(HttpStatus.CONFLICT, ErrorConstant.ERROR_CODE_80, "KTP minimal dan maksimal 16 Karakter");
    }

    customer.setCustTypeCode(type.name());
    customer.setCustIdNo(request.getCustomerIdNo());
    customer.setCustMobilePhone(FormatingUtils.formatOnlyNumber(request.getMobilePhone()));
    customer.setAgreeTc(request.isAgreeTc());
    customer.setCustPin(encodePin);
    customer.setBouwheer(request.getBouwheerCode());
    customer.setUsrCrt(customer.getCustName());
    customer.setDtmCrt(DateTimeUtils.now());
    Customer newCustomer = customerRepository.save(customer);

    // Send email to major account
    List<MstEmployee> mstEmployees = mstEmployeeRepository.findListEmployeesByRoleCode("mjr_account");
    mstEmployees.forEach(employee -> emailService.sendRegistrationUser(newCustomer, employee.getEmail()));

    auditTrailService.record(
      "CUSTOMER",
      before == null ? AuditAction.CREATE : AuditAction.UPDATE,
      "Customer",
      newCustomer.getCustCode(),
      before,
      toAuditData(newCustomer)
    );
    return newCustomer;
  }

  public void verifyEmail(Customer customer) {
    CustomerAuditData before = toAuditData(customer);
    boolean isRejectedReRegistration = ApprovalStatus.REJECTED.name().equals(customer.getApprovalStatus())
      && Boolean.FALSE.equals(customer.getIsEmailValid());

    customer.setIsEmailValid(true);
    customer.setActive(false);
    if (isRejectedReRegistration) {
      customer.setApprovalStatus(ApprovalStatus.OPEN.name());
      customer.setApprovalNote(null);
      customer.setApprovalBy(null);
      customer.setApprovalAt(null);
    }
    customer.setUsrUpd(customer.getCustName());
    customer.setDtmUpd(DateTimeUtils.now());
    Customer saved = customerRepository.save(customer);
    auditTrailService.record("CUSTOMER", AuditAction.UPDATE, "Customer", saved.getCustCode(), before, toAuditData(saved));
  }

  @Transactional
  public Customer update(Customer customer, UpdateCustomerRequest request) {
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
    customer.setCustIdNo(request.getCustIdNo());
    customer.setNpwp(request.getNpwp());
    customer.setUsrUpd(currentUserService.usernameOrDefault(AppConstants.CREATOR));
    customer.setDtmUpd(LocalDateTime.now());
    customer = customerRepository.save(customer);

    /**
     * Insert Audit trail
     */
    auditTrailService.record("CUSTOMER", AuditAction.UPDATE, "Customer", customer.getCustCode(), before, toAuditData(customer));

    /**
     * Force logout
     */
    customer.setForceLogout(emailChanged);

    return customer;
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


  @Transactional
  public void updateFapData(Customer customer, UpdateFapRequest request) {
    if (customer == null) {
      throw new IllegalArgumentException("Authenticated customer is required");
    }
    if (request.getFapStatus() == null || request.getFapStatus().isBlank()) {
      throw new IllegalArgumentException("FAP status is required");
    }

    FinancingHdr financingHdr = resolveFapFinancing(customer, request.getFinancingHdrCode());
    FapAuditData before = toFapAuditData(financingHdr);

    financingHdr.setFapDate(request.getFapDate() != null ? request.getFapDate() : DateTimeUtils.now());
    financingHdr.setFapStatus(request.getFapStatus().trim());
    financingHdr.setUsrUpd(customer.getCustName());
    financingHdr.setDtmUpd(DateTimeUtils.now());

    FinancingHdr saved = financingHdrRepository.save(financingHdr);
    auditTrailService.record(
      "LOAN_SUBMISSION",
      AuditAction.UPDATE,
      "FinancingHdr",
      saved.getFinancingHdrCode(),
      before,
      toFapAuditData(saved)
    );
  }

  private FinancingHdr resolveFapFinancing(Customer customer, UUID financingHdrCode) {
    FinancingHdr financingHdr;
    if (financingHdrCode != null) {
      financingHdr = financingHdrRepository.findByFinancingHdrCode(financingHdrCode)
        .orElseThrow(() -> new IllegalArgumentException("FinancingHdr not found: " + financingHdrCode));
    } else {
      financingHdr = financingHdrRepository.findFirstByCustomerOrderByFinancingHdrIdDesc(customer)
        .orElseThrow(() -> new IllegalArgumentException(
          "No FinancingHdr found for custCode: " + customer.getCustCode()
        ));
    }

    if (financingHdr.getCustomer() == null
      || !customer.getCustCode().equals(financingHdr.getCustomer().getCustCode())) {
      throw new IllegalArgumentException("FinancingHdr does not belong to authenticated customer");
    }
    return financingHdr;
  }

  private FapAuditData toFapAuditData(FinancingHdr financingHdr) {
    return new FapAuditData(financingHdr.getFapDate(), financingHdr.getFapStatus());
  }

  private record FapAuditData(LocalDateTime fapDate, String fapStatus) {
  }


  /**
   *
   * @param request
   * @return
   */
  public BaseResponseBuilder<PageCustomerResponse> pages(
    BasePaginationRequest request) {
    String searchValue = request.getSearchValue();
    String searchBy = request.getSearchBy();

    List<String> bouwheerCodes = new ArrayList<>();

    if ("bouwheerName".equals(request.getSearchBy()) && request.getSearchValue() != null) {
      List<Bouwheer> bouwheerList = bouwheerRepository.findByBouwheerNameContainingIgnoreCase(request.getSearchValue());

      if (bouwheerList.isEmpty()) {
        log.info(ErrorConstant.ERROR_MESSAGE_81 + "{}", request.getSearchValue());
        throw new BusinessException(HttpStatus.CONFLICT, ErrorConstant.ERROR_CODE_81, ErrorConstant.ERROR_MESSAGE_81 + "Bouwheer Name " + request.getSearchValue());
      }

      bouwheerCodes = bouwheerList.stream()
        .map(b -> String.valueOf(b.getBouwheerCode()))
        .toList();
      searchBy = "bouwheer";
    } else {
      searchValue = request.getSearchValue();
    }

    String sortBy = request.getSortBy();

    if (sortBy == null || sortBy.isEmpty()) {
      sortBy = "custName";
    } else if ("bouwheerName".equals(sortBy)) {
      sortBy = "bouwheer";
    }

    Pageable pageable = PageableUtil.createPageRequest(request, request.getPageSize(), request.getPageNo(),
      sortBy, request.getSortType());

    String finalSearchValue = searchValue;
    String finalSearchBy = searchBy;
    List<String> finalBouwheerCodes = bouwheerCodes;

    Page<Customer> page = customerRepository.findAll((Root<Customer> root, CriteriaQuery<?> query, CriteriaBuilder builder) -> {
      if ("bouwheer".equals(finalSearchBy)) {
        CriteriaBuilder.In<String> inClause = builder.in(root.get("bouwheer").as(String.class));

        for (String code : finalBouwheerCodes) {
          inClause.value(code);
        }
        return builder.and(inClause);
      }

      if (finalSearchBy != null && finalSearchValue != null) {
        Expression<String> lowerColumn = builder.lower(root.get(finalSearchBy).as(String.class));
        String searchPattern = "%" + finalSearchValue.toLowerCase() + "%";
        return builder.and(builder.like(lowerColumn, searchPattern));
      }
      return builder.conjunction();
    }, pageable);

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
      response.setBouwheerName(bouwheerRepository.findByBouwheerCode(item.getBouwheer() != null ? UUID.fromString(item.getBouwheer()) : null)
        .map(Bouwheer::getBouwheerName)
        .orElse(null));
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
    response.setBouwheerName(bouwheerRepository.findByBouwheerCode(customer.getBouwheer() != null ? UUID.fromString(customer.getBouwheer()) : null)
      .map(Bouwheer::getBouwheerName)
      .orElse(null));

    return new BaseResponseBuilder<>(true, AppConstants.CODE_OK, AppConstants.PROCESS_SUCCESSFULLY, response);
  }

  /**
   *
   * @param request
   * @return
   * @throws MessagingException
   */
  @Transactional
  public BaseResponse approval(ApprovalRequest request, String username) {
    Optional<Customer> customerOptional = customerRepository.findByCustCode(request.getCustCode());
    if (customerOptional.isEmpty()) {
      log.info(ErrorConstant.ERROR_MESSAGE_81 + "{}", request.getCustCode());
      throw new BusinessException(HttpStatus.CONFLICT, ErrorConstant.ERROR_CODE_81, "Customer tidak ditemukan");
    }

    Customer customer = customerOptional.get();
    if (!customer.getApprovalStatus().equals("OPEN")) {
      log.info(ErrorConstant.ERROR_MESSAGE_80 + "{}", customer.getCustName());
      throw new BusinessException(HttpStatus.CONFLICT, ErrorConstant.ERROR_CODE_80, "Customer ini sudah pernh diproses " + customer.getApprovalStatus());
    }

    if (!customer.getIsEmailValid()) {
      log.info(ErrorConstant.ERROR_MESSAGE_80 + "{}", customer.getCustName());
      throw new BusinessException(HttpStatus.CONFLICT, ErrorConstant.ERROR_CODE_80, "Customer ini belum melakukan verifikasi email ");
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

    if (ApprovalStatus.APPROVED.name().equals(approvalStatus)) {
      emailService.sendNotificationActive(saved, request.getApprovalNote());
    } else {
      emailService.sendNotificationRejected(saved, request.getApprovalNote());
    }

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
