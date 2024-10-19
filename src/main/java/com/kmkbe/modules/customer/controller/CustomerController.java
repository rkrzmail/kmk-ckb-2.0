package com.kmkbe.modules.customer.controller;

import com.kmkbe.core.domain.dto.*;
import com.kmkbe.core.domain.model.CommonResult;
import com.kmkbe.core.domain.constant.CustomerType;
import com.kmkbe.core.domain.entity.Customer;
import com.kmkbe.core.domain.entity.CustomerCompany;
import com.kmkbe.core.domain.entity.CustomerPersonal;
import com.kmkbe.core.domain.mapper.CustomerMapper;
import com.kmkbe.core.domain.model.PaginationResult;
import com.kmkbe.core.domain.request.PaginationRequest;
import com.kmkbe.modules.branch_admin.service.AgreementService;
import com.kmkbe.modules.customer.request.UpdateCustomerRequest;
import com.kmkbe.modules.customer.service.*;
import com.kmkbe.modules.customer.utils.CustomerUtils;
import com.kmkbe.modules.loan_submission.service.DocumentService;
import com.kmkbe.modules.loan_submission.service.InvoiceService;
import com.kmkbe.modules.user.utils.Utils;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.security.SignatureException;
import java.time.Duration;

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

    @GetMapping
    public CommonResult<CustomerDto> profile(
            Authentication authentication
    ) throws SignatureException, BadCredentialsException, IllegalStateException, IllegalAccessException {
        Customer customer = CustomerUtils.authenticateCustomer(authentication);
        CustomerDto result = CustomerMapper.INSTANCE.custDtoFromEntity(customer);
        if (customer.getCompany() != null) {
            result.setAddress(CustomerMapper.addressDtoFromCompany(customer.getCompany()));
            result.setCompany(CustomerMapper.INSTANCE.companyDtoFromEntity(customer.getCompany()));
        } else if (customer.getPersonal() != null) {
            result.setAddress(CustomerMapper.addressDtoFromPersonal(customer.getPersonal()));
            result.setPersonal(CustomerMapper.INSTANCE.personalDtoFromEntity(customer.getPersonal()));
        }
        return new CommonResult<CustomerDto>().success(result);
    }

    private void setMessageIfError(String data, String message){
        if (Utils.isEmptyOrNull(data)) {
            throw new IllegalArgumentException(message);
        }
    }

    @PutMapping
    @Transactional
    public CommonResult<CustomerDto> updateCustomer(
            Authentication authentication,
            @Valid @RequestBody UpdateCustomerRequest request
    ) throws Exception {
        Customer customer = customerService.update(authentication, request);
        CustomerDto result = CustomerMapper.INSTANCE.custDtoFromEntity(customer);
        if (customer.getCustTypeCode().equals(CustomerType.Company.name())) {
            if (request.getCompany() == null) {
                throw new IllegalArgumentException("Company cannot be null");
            }


            //mandaroty
            setMessageIfError(request.getCompany().getIdentityNo(),"All field  cannot be null");
            setMessageIfError(request.getCompany().getCompanyAddress(), "All field cannot be null");
            setMessageIfError(request.getAddress().getZipCode(), "All field cannot be null");
            setMessageIfError(String.valueOf(request.getCompany().getIdentityIssuedDate()),"All field cannot be null");
            setMessageIfError(String.valueOf(request.getCompany().getStaySince()),"All field cannot be null");
            setMessageIfError(String.valueOf(request.getCompany().getOwnershipStatus()),"All field cannot be null");
            setMessageIfError(String.valueOf(request.getCompany().getCompanyModel()),"All field cannot be null");
            setMessageIfError(String.valueOf(request.getCompany().getCustCompanyType()),"All field cannot be null");
            //tanggal seejak
            if (request.getCompany().getStaySince().getTime()> Utils.NowDate().getTime()){
                setMessageIfError(String.valueOf(request.getCompany().getCustCompanyType()),"Tanggal Sejak tidak boleh lebih dari hari ini");
            }




            CustomerCompany company = customerCompanyService.update(
                    customer,
                    request.getCompany(),
                    request.getAddress()
            );
            result.setAddress(CustomerMapper.addressDtoFromCompany(company));
            result.setCompany(CustomerMapper.INSTANCE.companyDtoFromEntity(company));

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
        }

        return new CommonResult<CustomerDto>().success(result);
    }


    @GetMapping("/invoices")
    public CommonResult<PaginationResult<PostedInvoiceDto>> getPostedInvoices(
            Authentication authentication,
            PaginationRequest request
    ) throws SignatureException {
        return new CommonResult<PaginationResult<PostedInvoiceDto>>().success(
                invoiceService.customerActiveInvoices(authentication, request)
        );
    }

    //PostedInvoiceDto
   /* @GetMapping("/invoices/due-date")
    public CommonResult<PaginationResult<CustomerCreditFacilityDto>> getPostedInvoicesDue(
            Authentication authentication,
            PaginationRequest request
    ) throws SignatureException {
        return new CommonResult<PaginationResult<CustomerCreditFacilityDto>>().success(
                invoiceService.customerDueDateInvoices(authentication, request)
        );
    }*/

   /* @GetMapping("/credit-facilities")
    public CommonResult<PaginationResult<CustomerCreditFacilityDto>> getActiveCreditFacilities(
            Authentication authentication,
            PaginationRequest request
    ) throws SignatureException {
        return new CommonResult<PaginationResult<CustomerCreditFacilityDto>>().success(
                invoiceService.customerCreditFacilities(authentication, request)
        );
    }*/

    @GetMapping("/invoices/due-date")
    public CommonResult<PaginationResult<CustomerCreditFacilityDueDateDto>> getPostedInvoicesDue(
            Authentication authentication,
            PaginationRequest request
    ) throws SignatureException {
        return new CommonResult<PaginationResult<CustomerCreditFacilityDueDateDto>>().success(
                customerDashboardListService.listinvoicesduedate(authentication, request)
        );

    }

    @GetMapping("/credit-facilities")
    public CommonResult<PaginationResult<CustomerCreditFacilityNewDto>> getActiveCreditFacilities(
            Authentication authentication,
            PaginationRequest request
    ) throws SignatureException {
        //invoiceService.customerCreditFacilities(authentication, request)

        return new CommonResult<PaginationResult<CustomerCreditFacilityNewDto>>().success(
                customerDashboardListService.listcreditfacilities(authentication, request)
        );
    }

    @GetMapping("/plafond/{financingHdrCode}")
    public CommonResult<CustomerPlafondDto> getPlafond(
            @PathVariable String financingHdrCode
    ) {
        return new CommonResult<CustomerPlafondDto>().success(
                customerDashboardService.plafond(financingHdrCode)
        );
    }

    @GetMapping("/documents/{custCode}")
    public CommonResult<PaginationResult<LegalFileDto>> getDocuments(
            @PathVariable String custCode,
            PaginationRequest request,
            HttpServletRequest httpServletRequest
    ) throws SignatureException {
        return new CommonResult<PaginationResult<LegalFileDto>>().success(
                documentService.uploadedCustomerDoc(
                        custCode,
                        httpServletRequest,
                        request
                )
        );
    }

    @GetMapping("/dashboard")
    public CommonResult<CustomerDashboardDto> getDashboard(
            Authentication authentication
    ) throws SignatureException {
        return new CommonResult<CustomerDashboardDto>().success(
                customerDashboardService.mainDashboard(authentication)
        );
    }
}
