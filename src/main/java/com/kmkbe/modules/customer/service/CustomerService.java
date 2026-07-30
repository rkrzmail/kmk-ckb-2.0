package com.kmkbe.modules.customer.service;

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
import com.kmkbe.core.domain.repository.CustomerCompanyRepository;
import com.kmkbe.core.domain.repository.CustomerPersonalRepository;
import com.kmkbe.modules.customer.model.response.CustomerResponse;
import com.kmkbe.modules.customer.model.response.PageCustomerResponse;
import com.kmkbe.modules.customer.repository.CustomerRepository;
import com.kmkbe.core.domain.repository.FinancingHdrRepository;
import com.kmkbe.core.domain.request.PaginationRequest;
import com.kmkbe.core.enums.ApprovalStatus;
import com.kmkbe.core.exception.CommonInvalidException;
import com.kmkbe.core.utils.DateTimeUtils;
import com.kmkbe.core.utils.FormatingUtils;
import com.kmkbe.helpers.utils.CommonUtils;
import com.kmkbe.helpers.base.BaseResponse;
import com.kmkbe.modules.common.service.EmailService;
import com.kmkbe.modules.customer.model.request.ApprovalRequest;
import com.kmkbe.modules.customer.model.request.SignUpRequest;
import com.kmkbe.modules.customer.model.request.UpdateCustomerRequest;
import com.kmkbe.modules.customer.model.request.UpdateFapRequest;
import com.kmkbe.modules.customer.utils.CustomerUtils;
import jakarta.mail.MessagingException;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SignatureException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerService {
  private final CustomerRepository customerRepository;
  private final CustomerPersonalRepository customerPersonalRepository;
  private final CustomerCompanyRepository customerCompanyRepository;
  private final BCryptPasswordEncoder bcryptEncoder;
  private final JdbcTemplate jdbcTemplate;
  private final FinancingHdrRepository financingHdrRepository;
  private final EmailService emailService;

  public Customer create(
    SignUpRequest request,
    CustomerType type
  ) throws CommonInvalidException {
    try {
      if (!request.getIsAgreeTc()) {
        throw new Exception("Setujui Syarat dan Ketentuan for sign up");
      }

      final Optional<Customer> find = customerRepository
        .findByCustEmail(request.getEmail());

      if (find.isPresent()) {
        if (find.get().getIsActive()) {
          throw CommonInvalidException.alreadyRegistered();
        } else {
          CustomerUtils.clearCustomerInactiveData(jdbcTemplate, find.get());
        }
      }

      final String encodePin = bcryptEncoder.encode(request.getPin());

      final Customer customer = new Customer();
      customer.setCustCode(UUID.randomUUID());
      customer.setCustName(request.getName());
      customer.setCustEmail(request.getEmail().toLowerCase());

      if (request.getCustomerNo() != null && !request.getCustomerNo().isEmpty()) {
        customer.setCustNo(request.getCustomerNo());
      }

      if (type == CustomerType.Company) {
        customer.setCustIdTypeCode(CustomerIdType.NPWP.name());
        if (request.getCustomerIdNo() != null && request.getCustomerIdNo().length() < 16) {
          //throw new Exception("NPWP minimal 16 Karakter");
        }
      } else {
        customer.setCustIdTypeCode(CustomerIdType.KTP.name());
        if (
          request.getCustomerIdNo() != null
            && request.getCustomerIdNo().length() != 16
        ) {
          throw new Exception("KTP minimal dan maksimal 16 Karakter");
        }
      }

      customer.setCustTypeCode(type.name());
      customer.setCustIdNo(request.getCustomerIdNo());
      customer.setCustMobilePhone(FormatingUtils.formatOnlyNumber(request.getMobilePhone()));
      customer.setAgreeTc(request.getIsAgreeTc());
      customer.setCustPin(encodePin);
//      customer.setIsEmailValid(false);
//      customer.setBouwheerCode(request.getBouwheer);
      customer.setVendorId(request.getVendorId());
      customer.setApprovalStatus(String.valueOf(ApprovalStatus.OPEN));
      customer.setIsActive(false);

      if (request.getVendorCode() != null && !request.getVendorCode().isEmpty()) {
        customer.setCustExternalCode(request.getVendorCode());
      }

      customer.setUsrCrt(customer.getCustName());
      customer.setDtmCrt(DateTimeUtils.now());
      return customerRepository.save(customer);
    } catch (CommonInvalidException e) {
      log.error("create, error {}", e.getMessage());
      throw e;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public void activated(Customer customer) {
    customer.setIsEmailValid(true);
    customer.setIsActive(true);
    customer.setUsrUpd(customer.getCustName());
    customer.setDtmUpd(DateTimeUtils.now());
    customerRepository.save(customer);
  }

  public Customer update(
    Authentication authentication,
    UpdateCustomerRequest request

  ) throws SignatureException {
    try {
      Customer customer = CustomerUtils.authenticateCustomer(authentication);
      boolean emailChanged = false;

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

  public ProfileFapDto prolifeFAP(Authentication authentication, HttpServletRequest request) {
    String financingHdrCode = request.getParameter("financingHdrCode");


    return null;
  }

  public ProfileSITDto prolifeSIT(Authentication authentication, HttpServletRequest request) {
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
    String sortBy = request.getSortBy() != null && !request.getSortBy().isEmpty() ? request.getSortBy() : "dtm_crt";
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
      response.setIsActive(item.getIsActive());
      response.setDtmCrt(item.getDtmCrt());
      response.setForceLogout(item.getForceLogout());
      response.setVendorId(item.getVendorId());
      response.setBouwheerCode(item.getBouwheerCode());
      response.setBouwheerName(Optional.ofNullable(item.getBouwheerDetail())
        .map(Bouwheer::getBouwheerName)
        .orElse(null));
      response.setApprovalStatus(item.getApprovalStatus());
      response.setApprovalNote(item.getApprovalNote());
      response.setApprovalBy(item.getApprovalBy());
      response.setApprovalAt(item.getApprovalAt());
      return response;
    }).toList();

    return new BaseResponseBuilder<>(true, AppConstants.CODE_OK, AppConstants.PROCESS_SUCCESSFULLY,PageCustomerResponse.builder()
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
      throw new BusinessException(HttpStatus.CONFLICT, ErrorConstant.ERROR_CODE_01, ErrorConstant.ERROR_MESSAGE_81);
    }

    Customer customer = customerOptional.get();
    CustomerResponse response = new CustomerResponse();
    BeanUtils.copyProperties(customer,response);
    response.setBouwheerName(Optional.ofNullable(customer.getBouwheerDetail())
      .map(Bouwheer::getBouwheerName)
      .orElse(null));

    return new BaseResponseBuilder<>(true, AppConstants.CODE_OK, AppConstants.PROCESS_SUCCESSFULLY,response);
  }

  /**
   *
   * @param request
   * @param authentication
   * @return
   * @throws MessagingException
   */
  public BaseResponse approval(ApprovalRequest request,Authentication authentication){
    Optional<Customer> customerOptional = customerRepository.findByCustCode(request.getCustCode());
    if (customerOptional.isEmpty()) {
      throw new IllegalArgumentException("Customer not ID found  " + request.getCustCode());
    }

    Customer customer = customerOptional.get();
    if (customer.getApprovalStatus().equals(ApprovalStatus.APPROVED) || customer.getApprovalStatus().equals("REJECTED")) {
      throw new IllegalArgumentException("Customer has been process approval status is " + request.getApprovalStatus());
    }

    customer.setApprovalStatus(request.getApprovalStatus().toUpperCase().trim());
    customer.setIsActive(request.getApprovalStatus().equals("APPROVED"));
    customer.setApprovalNote(request.getApprovalNote());
    customer.setApprovalBy(authentication.getName());
    customer.setApprovalAt(DateTimeUtils.now());
    customerRepository.save(customer);

    // Send mail
    emailService.customerVerification(customer.getCustEmail().toLowerCase(),customer.getCustName(),customer.getCustIdNo(), CommonUtils.generateOtp());

    return new BaseResponseBuilder<>(true, AppConstants.CODE_OK, AppConstants.PROCESS_SUCCESSFULLY);
  }
}
