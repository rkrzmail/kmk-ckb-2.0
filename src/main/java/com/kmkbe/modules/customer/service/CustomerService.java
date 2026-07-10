package com.kmkbe.modules.customer.service;

import com.kmkbe.core.domain.constant.CustomerIdType;
import com.kmkbe.core.domain.constant.CustomerType;
import com.kmkbe.core.domain.dto.*;
import com.kmkbe.core.domain.entity.Customer;
import com.kmkbe.core.domain.entity.FinancingHdr;
import com.kmkbe.core.domain.model.PaginationResult;
import com.kmkbe.core.domain.repository.CustomerCompanyRepository;
import com.kmkbe.core.domain.repository.CustomerPersonalRepository;
import com.kmkbe.core.domain.repository.CustomerRepository;
import com.kmkbe.core.domain.repository.FinancingHdrRepository;
import com.kmkbe.core.domain.request.PaginationRequest;
import com.kmkbe.core.enums.ApprovalStatus;
import com.kmkbe.core.exception.CommonInvalidException;
import com.kmkbe.core.utils.DateTimeUtils;
import com.kmkbe.core.utils.FormatingUtils;
import com.kmkbe.helpers.CommonUtils;
import com.kmkbe.helpers.base.BaseResponse;
import com.kmkbe.helpers.base.EmptyResponse;
import com.kmkbe.modules.common.service.EmailService;
import com.kmkbe.modules.customer.request.ApprovalRequest;
import com.kmkbe.modules.customer.request.SignUpRequest;
import com.kmkbe.modules.customer.request.UpdateCustomerRequest;
import com.kmkbe.modules.customer.request.UpdateFapRequest;
import com.kmkbe.modules.customer.utils.CustomerUtils;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
    InquiryVendorRemoteDto vendor,
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
      customer.setIsEmailValid(false);
      customer.setBouwheer(request.getBouwheer());
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


  public PaginationResult<CustomerDto> pages(
    PaginationRequest request) {
    Page<Customer> page = customerRepository.findAll(PageRequest.of(request.getPageNo(), request.getPageSize()));
    List<CustomerDto> responses = page.getContent().stream().map(item -> {
      CustomerDto response = new CustomerDto();
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
      response.setBouwheer(item.getBouwheer());
      response.setApprovalStatus(item.getApprovalStatus());
      response.setApprovalNote(item.getApprovalNote());
      response.setApprovalBy(item.getApprovalBy());
      response.setApprovalAt(item.getApprovalAt());
      return response;
    }).toList();

    return PaginationResult.<CustomerDto>builder()
      .currentPage(+1)
      .totalData(page.getTotalElements())
      .totalPage(page.getTotalPages())
      .list(responses).build();

  }

  // ApprovalStatus
  public CustomerDto findByCustCode(String custCode) {
    Optional<Customer> customerOptional = customerRepository.findByCustCode(UUID.fromString(custCode));
    if (customerOptional.isEmpty()) {
      throw new IllegalArgumentException("Customer not ID found  " + custCode);
    }

    Customer customer = customerOptional.get();
    CustomerDto response = new CustomerDto();
    response.setCustCode(customer.getCustCode());
    response.setCustNo(customer.getCustNo());
    response.setCustName(customer.getCustName());
    response.setCustTypeCode(customer.getCustTypeCode());
    response.setCustIdNo(customer.getCustIdNo());
    response.setCustEmail(customer.getCustEmail());
    response.setIsEmailValid(customer.getIsEmailValid());
    response.setCustMobilePhone(customer.getCustMobilePhone());
    response.setIsPhoneValid(customer.getIsPhoneValid());
    response.setIsWaActive(customer.getIsWaActive());
    response.setAgreeTc(customer.getAgreeTc());
    response.setAgreeLegalShare(customer.getAgreeLegalShare());
    response.setCustExternalCode(customer.getCustExternalCode());
    response.setIsActive(customer.getIsActive());
    response.setDtmCrt(customer.getDtmCrt());
    response.setForceLogout(customer.getForceLogout());
    response.setVendorId(customer.getVendorId());
    response.setBouwheer(customer.getBouwheer());
    response.setApprovalStatus(customer.getApprovalStatus());
    response.setApprovalNote(customer.getApprovalNote());
    response.setApprovalBy(customer.getApprovalBy());
    response.setApprovalAt(customer.getApprovalAt());
    return response;
  }

  // ApprovalStatus
  public BaseResponse approval(ApprovalRequest request,Authentication authentication) throws MessagingException {
    Optional<Customer> customerOptional = customerRepository.findByCustCode(request.getCustCode());
    if (customerOptional.isEmpty()) {
      throw new IllegalArgumentException("Customer not ID found  " + request.getCustCode());
    }

    Customer customer = customerOptional.get();
    if (customer.getApprovalStatus().equals(ApprovalStatus.APPROVED) || customer.getApprovalStatus().equals("REJECTED")) {
      throw new IllegalArgumentException("Customer has been process approval status is " + request.getApprovalStatus());
    }

    customer.setApprovalStatus(request.getApprovalStatus());
    customer.setIsActive(request.getApprovalStatus().equals("APPROVED"));
    customer.setApprovalNote(request.getApprovalNote());
    customer.setApprovalBy(authentication.getName());
    customer.setApprovalAt(DateTimeUtils.now());
    customerRepository.save(customer);

    // Send mail
    emailService.customerVerification(customer.getCustEmail().toLowerCase(),customer.getCustName(),customer.getCustIdNo(), CommonUtils.generateOtp());
    return new EmptyResponse();
  }
}
