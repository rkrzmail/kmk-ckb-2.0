package com.kmkbe.modules.customer.service;

import com.kmkbe.core.security.CurrentUserService;
import com.kmkbe.exception.BusinessException;
import com.kmkbe.helpers.constant.AppConstants;
import com.kmkbe.helpers.constant.ErrorConstant;
import com.kmkbe.modules.customer.model.entity.Customer;
import com.kmkbe.core.domain.entity.CustomerCompany;
import com.kmkbe.core.domain.repository.CustomerCompanyRepository;
import com.kmkbe.core.utils.DateTimeUtils;
import com.kmkbe.modules.customer.model.request.SignUpRequest;
import com.kmkbe.modules.customer.model.request.UpdateCustomerRequest;
import com.kmkbe.modules.customer.utils.CustomerUtils;
import com.kmkbe.helpers.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class CustomerCompanyService {
  private final CustomerCompanyRepository customerCompanyRepository;
  private final CurrentUserService currentUserService;

  public CustomerCompanyService(CustomerCompanyRepository customerCompanyRepository,
                                CurrentUserService currentUserService) {
    this.customerCompanyRepository = customerCompanyRepository;
    this.currentUserService = currentUserService;
  }


  @Transactional
  public void create(Customer customer, SignUpRequest.Company companyReq) {
    Optional<CustomerCompany>  customerOptional= customerCompanyRepository.findByCustomer(customer);
    CustomerCompany company;
    if (customerOptional.isPresent()) {
      log.info(ErrorConstant.ERROR_MESSAGE_84 + "{}", customer.getCustName());
      company =customerOptional.get();
      company.setCustomer(customer);
      company.setUsrCrt(customer.getCustName());
      company.setDtmCrt(DateTimeUtils.now());
    }else{
      company = new CustomerCompany();
      company.setCustCompanyCode(UUID.randomUUID());
      company.setCustomer(customer);
      company.setCustCompanyType(companyReq.getCompanyType());
      company.setCompanyModel(companyReq.getCompanyModel());
      company.setIdentityType(companyReq.getIdentityType());
      company.setIdentityNo(companyReq.getIdentityNo());
      company.setIdentityIssuedDate(companyReq.getIdentityIssuedDate());
      company.setIdentityExpiredDate(companyReq.getIdentityExpiredDate());
      company.setCompanyAddress(companyReq.getCompanyAddress());
      company.setRt(companyReq.getRt());
      company.setRw(companyReq.getRw());
      company.setKelurahan(companyReq.getKelurahan());
      company.setKecamatan(companyReq.getKecamatan());
      company.setCity(companyReq.getCity());
      company.setProvince(companyReq.getProvince());
      company.setZipCode(companyReq.getZipCode());
      company.setArea(companyReq.getArea());
      company.setPhone(companyReq.getPhone());
      company.setOwnershipStatus(companyReq.getOwnershipStatus());
      company.setStaySince(companyReq.getStaySince());
      company.setStayLength(CustomerUtils.calculateStayLength(companyReq.getStaySince()));
      company.setUsrCrt(customer.getCustName());
      company.setDtmCrt(DateTimeUtils.now());
    }
    customerCompanyRepository.save(company);
  }

  @Transactional
  public CustomerCompany update(
    Customer customer,
    UpdateCustomerRequest.UpdateCompanyRequest request,
    UpdateCustomerRequest.UpdateAddressRequest addressRequest
  ) {

    final Optional<CustomerCompany> find = customerCompanyRepository.findByCustomer(customer);
    final CustomerCompany company;
    if (find.isEmpty()) {
      company = new CustomerCompany();
      company.setCustCompanyCode(UUID.randomUUID());
      company.setUsrCrt(customer.getCustName());
      company.setDtmCrt(DateTimeUtils.now());
    } else {
      company = find.get();
      company.setUsrUpd(customer.getCustName());
      company.setDtmUpd(DateTimeUtils.now());
    }

    company.setCustomer(customer);
    company.setCustCompanyType(request.getCustCompanyType());
    company.setIdentityType(request.getIdentityType());
    company.setIdentityNo(request.getIdentityNo());
    company.setIdentityIssuedDate(Utils.toInstant(request.getIdentityIssuedDate()));
    company.setIdentityExpiredDate(Utils.toInstant(request.getIdentityExpiredDate()));
    company.setCompanyAddress(request.getCompanyAddress());
    company.setKelurahan(addressRequest.getKelurahan());
    company.setKecamatan(addressRequest.getKecamatan());
    company.setProvince(addressRequest.getProvince());//ketinggalan
    company.setCity(addressRequest.getCity());
    company.setZipCode(addressRequest.getZipCode());
    company.setArea(addressRequest.getArea());
    company.setRt(addressRequest.getRt());
    company.setRw(addressRequest.getRw());
    company.setProvince(addressRequest.getProvince());
    company.setPhone(request.getPhone());
    company.setOwnershipStatus(request.getOwnershipStatus());
    company.setStaySince(Utils.toInstant(request.getStaySince()));
    company.setStayLength(CustomerUtils.calculateStayLength(Utils.toInstant(request.getStaySince())));
    company.setDirectorName(request.getDirectorName());
    company.setDirectorPhone(request.getDirectorPhone());
    company.setDtmUpd(LocalDateTime.now());
    company.setUsrUpd(currentUserService.usernameOrDefault(AppConstants.CREATOR));
    return customerCompanyRepository.save(company);
  }
}
