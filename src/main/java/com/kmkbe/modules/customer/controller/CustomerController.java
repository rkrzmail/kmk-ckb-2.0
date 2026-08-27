package com.kmkbe.modules.customer.controller;

import com.kmkbe.core.domain.dto.*;
import com.kmkbe.core.domain.entity.*;
import com.kmkbe.core.domain.model.CommonResult;
import com.kmkbe.core.domain.constant.CustomerType;
import com.kmkbe.core.domain.mapper.CustomerMapper;
import com.kmkbe.core.domain.model.PaginationResult;
import com.kmkbe.core.security.CurrentUserService;
import com.kmkbe.helpers.base.BasePaginationRequest;
import com.kmkbe.modules.bouwheer.model.entity.Bouwheer;
import com.kmkbe.modules.bouwheer.repository.BouwheerRepository;
import com.kmkbe.modules.customer.model.dto.CustomerDto;
import com.kmkbe.modules.customer.repository.CustomerRepository;
import com.kmkbe.core.domain.repository.FinancingHdrRepository;
import com.kmkbe.core.domain.request.PaginationRequest;
import com.kmkbe.helpers.base.BaseResponse;
import com.kmkbe.modules.branch_admin.service.AgreementService;
import com.kmkbe.modules.customer.model.entity.Customer;
import com.kmkbe.modules.customer.model.request.ApprovalRequest;
import com.kmkbe.modules.customer.model.request.UpdateCustomerRequest;
import com.kmkbe.modules.customer.model.request.UpdateFapRequest;
import com.kmkbe.modules.customer.service.*;
import com.kmkbe.modules.loan_submission.service.DocumentService;
import com.kmkbe.modules.loan_submission.service.InvoiceService;
import com.kmkbe.modules.user.utils.Utils;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

import java.security.SignatureException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/customer")
@Tag(
  name = "Customer",
  description = "Customer Endpoints"
)
@RequiredArgsConstructor
public class CustomerController {
  private final CustomerService customerService;
  private final CustomerCompanyService customerCompanyService;
  private final CustomerPersonalService customerPersonalService;
  private final CustomerDashboardService customerDashboardService;
  private final InvoiceService invoiceService;
  private final CustomerDashboardListService customerDashboardListService;
  private final DocumentService documentService;
  private final AgreementService agreementService;
  private final FinancingHdrRepository financingHdrRepository;
  private final CustomerRepository customerRepository;
  private final CurrentUserService currentUserService;
  private final BouwheerRepository bouwheerRepository;


  @GetMapping
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

    result.setBouwheerName(bouwheerRepository.findByBouwheerCode(customer.getBouwheer() != null? UUID.fromString(customer.getBouwheer()) :null)
      .map(Bouwheer::getBouwheerName)
      .orElse(null));

    return new CommonResult<CustomerDto>().success(result);
  }


  private void setMessageIfError(String data, String message) {
    if (Utils.isEmptyOrNull(data)) {
      throw new IllegalArgumentException(message);
    }
  }

  @PutMapping
  @Transactional
  public CommonResult<CustomerDto> updateCustomer(
    @Valid @RequestBody UpdateCustomerRequest request
  ) throws Exception {
    Customer customer = customerService.update(currentUserService.customer(), request);
    CustomerDto result = CustomerMapper.INSTANCE.custDtoFromEntity(customer);
    if (customer.getCustTypeCode().equals(CustomerType.Company.name())) {
      if (request.getCompany() == null) {
        throw new IllegalArgumentException("Company cannot be null");
      }


      //mandaroty
      setMessageIfError(request.getCompany().getIdentityNo(), "All field  cannot be null");
      setMessageIfError(request.getCompany().getCompanyAddress(), "All field cannot be null");
      setMessageIfError(request.getAddress().getZipCode(), "All field cannot be null");
      setMessageIfError(String.valueOf(request.getCompany().getIdentityIssuedDate()), "All field cannot be null");
      setMessageIfError(String.valueOf(request.getCompany().getStaySince()), "All field cannot be null");
      setMessageIfError(String.valueOf(request.getCompany().getOwnershipStatus()), "All field cannot be null");
      setMessageIfError(String.valueOf(request.getCompany().getCompanyModel()), "All field cannot be null");
      setMessageIfError(String.valueOf(request.getCompany().getCustCompanyType()), "All field cannot be null");
      //tanggal seejak
      if (request.getCompany().getStaySince().getTime() > Utils.NowDate().getTime()) {
        setMessageIfError(String.valueOf(request.getCompany().getCustCompanyType()), "Tanggal Sejak tidak boleh lebih dari hari ini");
      }


      CustomerCompany company = customerCompanyService.update(
        customer,
        request.getCompany(),
        request.getAddress()
      );
      result.setAddress(CustomerMapper.addressDtoFromCompany(company));
      result.setCompany(CustomerMapper.INSTANCE.companyDtoFromEntity(company));
      result.setForceLogout(customer.getForceLogout());
      result.setNpwp(customer.getNpwp());

    } else if (customer.getCustTypeCode().equals(CustomerType.Personal.name())) {
      if (request.getPersonal() == null) {
        throw new IllegalArgumentException("Personal cannot be null");
      }

      CustomerPersonal personal = customerPersonalService.update(
        customer,
        request.getPersonal(),
        request.getAddress()
      );
      result.setAddress(CustomerMapper.addressDtoFromPersonal(personal));
      result.setPersonal(CustomerMapper.INSTANCE.personalDtoFromEntity(personal));
      result.setForceLogout(customer.getForceLogout());
    }
    return new CommonResult<CustomerDto>().success(result);
  }

  @GetMapping("/profilefap")
  public CommonResult<ProfileFapDto> getProfileFap(
    HttpServletRequest request
  ) {
    return new CommonResult<ProfileFapDto>().success(
      customerService.prolifeFAP(request)
    );
  }

  @GetMapping("/profile/sip")
  public CommonResult<ProfileSITDto> getProfileSit(
    HttpServletRequest request
  ) {
    return new CommonResult<ProfileSITDto>().success(
      customerService.prolifeSIT(request)
    );
  }

  @GetMapping("/invoices")
  public CommonResult<PaginationResult<PostedInvoiceDto>> getPostedInvoices(
    PaginationRequest request
  ) {
    return new CommonResult<PaginationResult<PostedInvoiceDto>>().success(
      invoiceService.customerActiveInvoices(request)
    );
  }

  @GetMapping("/invoices/due-date")
  public CommonResult<PaginationResult<CustomerCreditFacilityDueDateDto>> getPostedInvoicesDue(
    PaginationRequest request
  ) throws SignatureException {
    return new CommonResult<PaginationResult<CustomerCreditFacilityDueDateDto>>().success(
      customerDashboardListService.listinvoicesduedate(currentUserService.customer(), request)
    );

  }

  @GetMapping("/credit-facilities")
  public CommonResult<PaginationResult<CustomerCreditFacilityNewDto>> getActiveCreditFacilities(
    PaginationRequest request
  ) throws SignatureException {
    //invoiceService.customerCreditFacilities(authentication, request)

    return new CommonResult<PaginationResult<CustomerCreditFacilityNewDto>>().success(
      customerDashboardListService.listcreditfacilities(currentUserService.customer(), request)
    );
  }

  @GetMapping("/plafond/{financingHdrCode}")
  public BaseResponse getPlafond(
    @PathVariable String financingHdrCode) {
    return customerDashboardService.plafondByFinancingHdrCode(financingHdrCode);
  }

  @GetMapping("/plafondcustomer")
  public BaseResponse getPlafondCustomer() throws SignatureException {
    return customerDashboardService.plafond();
  }

  @GetMapping("/documents/{custCode}")
  public CommonResult<PaginationResult<LegalFileDto>> getDocuments(
    @PathVariable String custCode,
    PaginationRequest request,
    HttpServletRequest httpServletRequest
  ) {
    return new CommonResult<PaginationResult<LegalFileDto>>().success(
      documentService.uploadedCustomerDoc(
        custCode,
        httpServletRequest,
        request
      )
    );
  }

  @GetMapping("/dashboard")
  public BaseResponse getDashboard() throws SignatureException {
    return customerDashboardService.mainDashboard();
  }

  @PutMapping("/updateFapData")
  public ResponseEntity<String> updateFapData(@RequestBody UpdateFapRequest request) {
    try {
      customerService.updateFapData(request);
      return ResponseEntity.ok("Fap data updated successfully");
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }
  }

  @GetMapping("/perjanjian/{financingHdrCode}")
  public CommonResult<CustomerPerjanjianDto> getPerjanjian(
    @PathVariable String financingHdrCode

  ) throws SignatureException {
    currentUserService.authenticatedInternalUser();
    return new CommonResult<CustomerPerjanjianDto>().success(
      customerDashboardService.perjanjian(financingHdrCode)
    );
  }

  @GetMapping("/notif/{custCode}")
  public CommonResult<List<NotifDebtor>> getAllNotifDebtors(
    @PathVariable String custCode
  ) {
    List<NotifDebtor> data = customerDashboardService.getNotifDebtors(custCode);
    return new CommonResult<List<NotifDebtor>>().success(data);
  }

  @DeleteMapping("/notif/{custCode}")
  public CommonResult<String> deleteAllNotifDebtors(
    @PathVariable String custCode
  ) {
    customerDashboardService.deleteAllNotifDebtors(custCode);
    return new CommonResult<String>().success("All Notification records have been deleted.");
  }


  @GetMapping(value = "/pages", produces = MediaType.APPLICATION_JSON_VALUE)
  public BaseResponse getAllCustomers(@Valid BasePaginationRequest request) {
    return customerService.pages(request);
  }

  @GetMapping(value = "/{custCode}", produces = MediaType.APPLICATION_JSON_VALUE)
  public BaseResponse getCustomerCode(@PathVariable String custCode) {
    return customerService.findByCustomerCode(custCode);
  }

  @PutMapping(value = "/approval")
  public BaseResponse approvalCustomer(@RequestBody @Valid ApprovalRequest request) throws SignatureException {
    return customerService.approval(request, currentUserService.internalUsername());
  }
}

