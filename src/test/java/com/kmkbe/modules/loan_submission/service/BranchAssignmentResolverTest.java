package com.kmkbe.modules.loan_submission.service;

import com.kmkbe.core.domain.entity.BranchAreaMapping;
import com.kmkbe.core.domain.entity.CustomerCompany;
import com.kmkbe.core.domain.entity.CustomerPersonal;
import com.kmkbe.core.domain.repository.BranchAreaMappingRepository;
import com.kmkbe.modules.customer.model.entity.Customer;
import com.kmkbe.modules.user.entity.MstBranch;
import com.kmkbe.modules.user.repository.MstBranchRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BranchAssignmentResolverTest {

  @Mock private BranchAreaMappingRepository branchAreaMappingRepository;
  @Mock private MstBranchRepository mstBranchRepository;

  private BranchAssignmentResolver resolver;

  @BeforeEach
  void setUp() {
    resolver = new BranchAssignmentResolver(branchAreaMappingRepository, mstBranchRepository);
  }

  @Test
  void resolvesCompanyBranchFromActiveAreaMapping() {
    MstBranch branch = MstBranch.builder().branchCode("JKT01").build();
    CustomerCompany company = CustomerCompany.builder()
      .area(" 01 ")
      .city(" Jakarta Selatan ")
      .province(" DKI Jakarta ")
      .kelurahan("Kuningan")
      .kecamatan("Setiabudi")
      .build();
    Customer customer = Customer.builder().custTypeCode("COMPANY").company(company).build();
    when(branchAreaMappingRepository
      .findFirstByAreaIgnoreCaseAndCityIgnoreCaseAndProvinceIgnoreCaseAndIsActiveTrue(
        "01", "Jakarta Selatan", "DKI Jakarta"
      ))
      .thenReturn(Optional.of(BranchAreaMapping.builder().mstBranch(branch).build()));

    Optional<MstBranch> result = resolver.resolve(customer);

    assertThat(result).containsSame(branch);
    verify(mstBranchRepository, never()).findTopLikeBranchNameRawQuery(
      "Jakarta Selatan", "Kuningan", "Setiabudi"
    );
  }

  @Test
  void resolvesPersonalBranchFromActiveAreaMapping() {
    MstBranch branch = MstBranch.builder().branchCode("SBY01").build();
    CustomerPersonal personal = CustomerPersonal.builder()
      .area("02")
      .city("Surabaya")
      .province("Jawa Timur")
      .build();
    Customer customer = Customer.builder().custTypeCode("PERSONAL").personal(personal).build();
    when(branchAreaMappingRepository
      .findFirstByAreaIgnoreCaseAndCityIgnoreCaseAndProvinceIgnoreCaseAndIsActiveTrue(
        "02", "Surabaya", "Jawa Timur"
      ))
      .thenReturn(Optional.of(BranchAreaMapping.builder().mstBranch(branch).build()));

    assertThat(resolver.resolve(customer)).containsSame(branch);
  }

  @Test
  void usesLegacyAddressLookupWhenNoConfiguredMappingMatches() {
    MstBranch fallbackBranch = MstBranch.builder().branchCode("BDG01").build();
    CustomerCompany company = CustomerCompany.builder()
      .area("03")
      .city("Bandung")
      .province("Jawa Barat")
      .kelurahan("Citarum")
      .kecamatan("Bandung Wetan")
      .build();
    Customer customer = Customer.builder().custTypeCode("company").company(company).build();
    when(branchAreaMappingRepository
      .findFirstByAreaIgnoreCaseAndCityIgnoreCaseAndProvinceIgnoreCaseAndIsActiveTrue(
        "03", "Bandung", "Jawa Barat"
      ))
      .thenReturn(Optional.empty());
    when(mstBranchRepository.findTopLikeBranchNameRawQuery(
      "Bandung", "Citarum", "Bandung Wetan"
    )).thenReturn(Optional.of(fallbackBranch));

    assertThat(resolver.resolve(customer)).containsSame(fallbackBranch);
  }

  @Test
  void usesLegacyLookupWhenMappingHasNoBranch() {
    MstBranch fallbackBranch = MstBranch.builder().branchCode("MDN01").build();
    CustomerPersonal personal = CustomerPersonal.builder()
      .area("04")
      .city("Medan")
      .province("Sumatera Utara")
      .build();
    Customer customer = Customer.builder().custTypeCode("personal").personal(personal).build();
    when(branchAreaMappingRepository
      .findFirstByAreaIgnoreCaseAndCityIgnoreCaseAndProvinceIgnoreCaseAndIsActiveTrue(
        "04", "Medan", "Sumatera Utara"
      ))
      .thenReturn(Optional.of(BranchAreaMapping.builder().mstBranch(null).build()));
    when(mstBranchRepository.findTopLikeBranchNameRawQuery("Medan", "", ""))
      .thenReturn(Optional.of(fallbackBranch));

    assertThat(resolver.resolve(customer)).containsSame(fallbackBranch);
  }

  @Test
  void returnsEmptyForMissingCustomerOrAddress() {
    assertThat(resolver.resolve(null)).isEmpty();
    assertThat(resolver.resolve(Customer.builder().custTypeCode("company").build())).isEmpty();
    assertThat(resolver.resolve(Customer.builder().custTypeCode("personal").build())).isEmpty();

    verifyNoInteractions(branchAreaMappingRepository, mstBranchRepository);
  }

  @Test
  void resolvesActiveCityMappingWhenItsFullKeyIsIncomplete() {
    MstBranch branch = MstBranch.builder().branchCode("JKT02").build();
    CustomerCompany company = CustomerCompany.builder()
      .area("01")
      .city("Jakarta")
      .province(" ")
      .build();
    Customer customer = Customer.builder().custTypeCode("company").company(company).build();
    when(branchAreaMappingRepository.findFirstByCityIgnoreCaseAndIsActiveTrue("Jakarta"))
      .thenReturn(Optional.of(BranchAreaMapping.builder().mstBranch(branch).build()));

    assertThat(resolver.resolve(customer)).containsSame(branch);
    verify(mstBranchRepository, never()).findTopLikeBranchNameRawQuery("Jakarta", "", "");
  }
}
