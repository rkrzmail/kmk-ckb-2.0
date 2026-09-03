package com.kmkbe.modules.loan_submission.service;

import com.kmkbe.core.domain.entity.BranchAreaMapping;
import com.kmkbe.core.domain.repository.BranchAreaMappingRepository;
import com.kmkbe.modules.customer.model.entity.Customer;
import com.kmkbe.modules.user.entity.MstBranch;
import com.kmkbe.modules.user.repository.MstBranchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BranchAssignmentResolver {

  private final BranchAreaMappingRepository branchAreaMappingRepository;
  private final MstBranchRepository mstBranchRepository;

  public Optional<MstBranch> resolve(Customer customer) {
    AddressParts address = addressOf(customer);

    if (address.hasCompleteMappingKey()) {
      Optional<MstBranch> mappedBranch = branchAreaMappingRepository
        .findFirstByAreaIgnoreCaseAndCityIgnoreCaseAndProvinceIgnoreCaseAndIsActiveTrue(
          address.area(),
          address.city(),
          address.province()
        )
        .map(BranchAreaMapping::getMstBranch);

      if (mappedBranch.isPresent()) {
        return mappedBranch;
      }
    }

    if (!address.city().isBlank()) {
      Optional<MstBranch> mappedBranch = branchAreaMappingRepository
        .findFirstByCityIgnoreCaseAndIsActiveTrue(address.city())
        .map(BranchAreaMapping::getMstBranch);

      if (mappedBranch.isPresent()) {
        return mappedBranch;
      }
    }

    if (!address.hasBranchSearchKey()) {
      return Optional.empty();
    }

    return mstBranchRepository.findTopLikeBranchNameRawQuery(
      address.city(),
      address.kelurahan(),
      address.kecamatan()
    );
  }

  private AddressParts addressOf(Customer customer) {
    if (customer == null) {
      return AddressParts.empty();
    }

    if ("company".equalsIgnoreCase(customer.getCustTypeCode()) && customer.getCompany() != null) {
      return new AddressParts(
        clean(customer.getCompany().getArea()),
        clean(customer.getCompany().getCity()),
        clean(customer.getCompany().getProvince()),
        clean(customer.getCompany().getKelurahan()),
        clean(customer.getCompany().getKecamatan())
      );
    }

    if (customer.getPersonal() != null) {
      return new AddressParts(
        clean(customer.getPersonal().getArea()),
        clean(customer.getPersonal().getCity()),
        clean(customer.getPersonal().getProvince()),
        clean(customer.getPersonal().getKelurahan()),
        clean(customer.getPersonal().getKecamatan())
      );
    }

    return AddressParts.empty();
  }

  private String clean(String value) {
    return value == null ? "" : value.trim();
  }

  private record AddressParts(
    String area,
    String city,
    String province,
    String kelurahan,
    String kecamatan
  ) {
    private static AddressParts empty() {
      return new AddressParts("", "", "", "", "");
    }

    private boolean hasCompleteMappingKey() {
      return !area.isBlank() && !city.isBlank() && !province.isBlank();
    }

    private boolean hasBranchSearchKey() {
      return !city.isBlank() || !kelurahan.isBlank() || !kecamatan.isBlank();
    }
  }
}
