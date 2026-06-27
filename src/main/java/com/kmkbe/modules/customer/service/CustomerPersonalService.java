package com.kmkbe.modules.customer.service;

import com.kmkbe.core.domain.entity.Customer;
import com.kmkbe.core.domain.entity.CustomerPersonal;
import com.kmkbe.core.domain.repository.CustomerPersonalRepository;
import com.kmkbe.core.utils.DateTimeUtils;
import com.kmkbe.modules.customer.request.SignUpRequest;
import com.kmkbe.modules.customer.request.UpdateCustomerRequest;
import com.kmkbe.modules.customer.utils.CustomerUtils;
import com.kmkbe.nikita.utils.Utils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class CustomerPersonalService {
  private final CustomerPersonalRepository customerPersonalRepository;

  public void get() {
    try {

    } catch (Exception e) {
      log.error("get: {}", e.getMessage());
    }
  }

  public void create(Customer cust, SignUpRequest.Personal personalReq) {
    final boolean userExists = customerPersonalRepository
      .findByCustomer(cust)
      .isPresent();

    if (userExists) {
      throw new IllegalStateException("User already exists");
    }

    final CustomerPersonal personal = new CustomerPersonal();
    {
      personal.setCustPersonalCode(UUID.randomUUID());
      personal.setCustomer(cust);
      personal.setBirthPlace(personalReq.getBirthPlace());
      personal.setBirthDate(personalReq.getBirthDate());
      personal.setGender(personalReq.getGender());
      personal.setIdentityType(personalReq.getIdentityType());
      personal.setIdentityNo(personalReq.getIdentityNo());
      personal.setExpiredDate(personalReq.getExpiredDate());
      personal.setMotherMaidenName(personalReq.getMotherMaidenName());
      personal.setMaritalStatus(personalReq.getMaritalStatus());
      personal.setCustModel(personalReq.getCustomerModel().name());
      personal.setLegalAddress(personalReq.getLegalAddress());
      personal.setRt(personalReq.getRt());
      personal.setRw(personalReq.getRw());
      personal.setKelurahan(personalReq.getKelurahan());
      personal.setKecamatan(personalReq.getKecamatan());
      personal.setCity(personalReq.getCity());
      personal.setProvince(personalReq.getProvince());
      personal.setZipCode(personalReq.getZipCode());
      personal.setArea(personalReq.getArea());
      personal.setPhone(personalReq.getPhone());
      personal.setOwnershipStatus(personalReq.getOwnershipStatus());
      personal.setStaySince(personalReq.getStaySince());
      personal.setStayLength(CustomerUtils.calculateStayLength(personalReq.getStaySince()));
    }

    customerPersonalRepository.save(personal);
  }

  public CustomerPersonal update(
    Customer customer,
    UpdateCustomerRequest.UpdatePersonalRequest request,
    UpdateCustomerRequest.UpdateAddressRequest addressRequest
  ) {
    try {
      final Optional<CustomerPersonal> find = customerPersonalRepository.findByCustomer(customer);
      final CustomerPersonal personal = find.orElseGet(CustomerPersonal::new);
      if (find.isEmpty()) {
        personal.setCustPersonalCode(UUID.randomUUID());
        personal.setUsrCrt(customer.getCustName());
        personal.setDtmCrt(DateTimeUtils.now());
      } else {
        personal.setUsrUpd(customer.getCustName());
        personal.setDtmUpd(DateTimeUtils.now());
      }

      personal.setCustomer(customer);
      personal.setBirthPlace(request.getBirthPlace());
      personal.setBirthDate(Utils.toInstant(request.getBirthDate()));
      personal.setBirthDate(Utils.toInstant(request.getBirthDate()));
      personal.setGender(request.getGender());
      personal.setIdentityType(request.getIdentityType());
      personal.setIdentityNo(request.getIdentityNo());
      personal.setExpiredDate(request.getExpiredDate() != null ? Utils.toInstant(request.getExpiredDate()) : null);
      personal.setExpiredDate(request.getExpiredDate() != null ? Utils.toInstant(request.getExpiredDate()) : null);
      personal.setMotherMaidenName(request.getMotherMaidenName());
      personal.setMaritalStatus(request.getMaritalStatus());
      personal.setCustModel(request.getCustModel());
      personal.setLegalAddress(request.getLegalAddress());
      {
        personal.setRt(addressRequest.getRt());
        personal.setRw(addressRequest.getRw());
        personal.setKelurahan(addressRequest.getKelurahan());
        personal.setKecamatan(addressRequest.getKecamatan());
        personal.setCity(addressRequest.getCity());
        personal.setProvince(addressRequest.getProvince());
        personal.setZipCode(addressRequest.getZipCode());
        personal.setArea(addressRequest.getArea());
      }
      personal.setPhone(request.getPhone());
      personal.setOwnershipStatus(request.getOwnershipStatus());
      personal.setStaySince(Utils.toInstant(request.getStaySince()));
      personal.setStayLength(CustomerUtils.calculateStayLength(Utils.toInstant(request.getStaySince())));
      return customerPersonalRepository.save(personal);
    } catch (Exception e) {
      log.error("update: {}", e.getMessage());
      throw e;
    }
  }
}
