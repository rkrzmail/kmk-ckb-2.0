package com.kmkbe.modules.branch_admin.service;

import com.kmkbe.core.domain.dto.BaseLdapRemoteResponseDto;
import com.kmkbe.core.domain.dto.SitDto;
import com.kmkbe.core.domain.entity.Agreement;
import com.kmkbe.core.domain.entity.Debtor;
import com.kmkbe.core.domain.entity.FinancingHdr;
import com.kmkbe.core.domain.model.CommonResult;
import com.kmkbe.core.domain.repository.AgreementRepository;
import com.kmkbe.core.domain.repository.DebtorRepository;
import com.kmkbe.core.domain.repository.FinancingHdrRepository;
import com.kmkbe.modules.bouwheer.model.entity.Bouwheer;
import com.kmkbe.modules.customer.model.entity.Customer;
import com.kmkbe.modules.remote.service.AuthRemoteService;
import com.kmkbe.modules.remote.service.EmailAo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AgreementCodeServiceTest {

    private static final UUID FINANCING_HDR_CODE = UUID.fromString("11111111-1111-1111-1111-111111111111");

    @Mock
    private AgreementRepository agreementRepository;

    @Mock
    private FinancingHdrRepository financingHdrRepository;

    @Mock
    private EmailAo emailAo;

    @Mock
    private DebtorRepository debtorRepository;

    @Mock
    private AuthRemoteService authRemoteService;

    private AgreementCodeService service;

    @BeforeEach
    void setUp() {
        service = new AgreementCodeService();
        ReflectionTestUtils.setField(service, "agreementRepository", agreementRepository);
        ReflectionTestUtils.setField(service, "financingHdrRepository", financingHdrRepository);
        ReflectionTestUtils.setField(service, "emailAo", emailAo);
        ReflectionTestUtils.setField(service, "debtorRepository", debtorRepository);
        ReflectionTestUtils.setField(service, "authRemoteService", authRemoteService);
        BaseLdapRemoteResponseDto<String> jwtResponse = new BaseLdapRemoteResponseDto<>();
        jwtResponse.setData("jwt-token");
        when(authRemoteService.fetchAuthJwt()).thenReturn(jwtResponse);
    }

    @Test
    void getAgreementsByFinancingHdrCodeReturnsFailWhenAgreementAndFinancingHdrDoNotExist() {
        when(agreementRepository.findByFinancingHdr_FinancingHdrCode(FINANCING_HDR_CODE)).thenReturn(List.of());
        when(financingHdrRepository.findByFinancingHdrCode(FINANCING_HDR_CODE)).thenReturn(Optional.empty());

        CommonResult<SitDto> result = service.getAgreementsByFinancingHdrCode(FINANCING_HDR_CODE);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getCode()).isEqualTo(404);
        assertThat(result.getMessage()).isEqualTo("No data found for financingHdrCode: " + FINANCING_HDR_CODE);
    }

    @Test
    void getAgreementsByFinancingHdrCodeBuildsDefaultSitWhenNoAgreementExists() {
        FinancingHdr financingHdr = financingHdr(customer("Customer Name"), bouwheer());
        when(agreementRepository.findByFinancingHdr_FinancingHdrCode(FINANCING_HDR_CODE)).thenReturn(List.of());
        when(financingHdrRepository.findByFinancingHdrCode(FINANCING_HDR_CODE)).thenReturn(Optional.of(financingHdr));
        when(financingHdrRepository.findDebtorNameByFinancingHdrCode(FINANCING_HDR_CODE)).thenReturn("Debtor Name");
        when(debtorRepository.findActiveSignerByDebtorName("Debtor Name")).thenReturn(List.of());
        when(financingHdrRepository.findBranchCodeByFinancingHdrCode(FINANCING_HDR_CODE)).thenReturn("JKT");
        when(emailAo.getEmailByPosition("JKT", "RM", "jwt-token")).thenReturn(List.of());

        CommonResult<SitDto> result = service.getAgreementsByFinancingHdrCode(FINANCING_HDR_CODE);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getData().getAgreementCode()).isEqualTo("-");
        assertThat(result.getData().getCustName()).isEqualTo("Customer Name");
        assertThat(result.getData().getDirectorName()).isEqualTo("-");
        assertThat(result.getData().getJabatan()).isEqualTo("-");
        assertThat(result.getData().getEmployeeName()).isEqualTo("N/A");
        assertThat(result.getData().getBouwheerName()).isEqualTo("PT BOUWHEER");
    }

    @Test
    void getAgreementsByFinancingHdrCodeBuildsDefaultSitWithSignerAndEmployeeWhenNoAgreementExists() {
        FinancingHdr financingHdr = financingHdr(customer("Customer Name"), bouwheer());
        when(agreementRepository.findByFinancingHdr_FinancingHdrCode(FINANCING_HDR_CODE)).thenReturn(List.of());
        when(financingHdrRepository.findByFinancingHdrCode(FINANCING_HDR_CODE)).thenReturn(Optional.of(financingHdr));
        when(financingHdrRepository.findDebtorNameByFinancingHdrCode(FINANCING_HDR_CODE)).thenReturn("Debtor Name");
        when(debtorRepository.findActiveSignerByDebtorName("Debtor Name"))
                .thenReturn(List.of(Debtor.builder().karyawanName("Default Signer").jabatan("Manager").build()));
        when(financingHdrRepository.findBranchCodeByFinancingHdrCode(FINANCING_HDR_CODE)).thenReturn("JKT");
        when(emailAo.getEmailByPosition("JKT", "RM", "jwt-token"))
                .thenReturn(List.of(Map.of("employeeName", "jane DOE")));

        CommonResult<SitDto> result = service.getAgreementsByFinancingHdrCode(FINANCING_HDR_CODE);

        assertThat(result.getData().getDirectorName()).isEqualTo("Default Signer");
        assertThat(result.getData().getJabatan()).isEqualTo("Manager");
        assertThat(result.getData().getEmployeeName()).isEqualTo("Jane Doe");
    }

    @Test
    void getAgreementsByFinancingHdrCodeBuildsSitFromExistingAgreement() {
        FinancingHdr financingHdr = financingHdr(customer("Agreement Customer"), bouwheer());
        Agreement agreement = Agreement.builder()
                .agreementCode("AGR001")
                .financingHdr(financingHdr)
                .build();
        when(agreementRepository.findByFinancingHdr_FinancingHdrCode(FINANCING_HDR_CODE)).thenReturn(List.of(agreement));
        when(financingHdrRepository.findBranchCodeByFinancingHdrCode(FINANCING_HDR_CODE)).thenReturn("BDG");
        when(emailAo.getEmailByPosition("BDG", "RM", "jwt-token"))
                .thenReturn(List.of(Map.of("employeeName", "john  DOE")));
        when(financingHdrRepository.findDebtorNameByFinancingHdrCode(FINANCING_HDR_CODE)).thenReturn("Debtor Name");
        when(debtorRepository.findActiveSignerByDebtorName("Debtor Name"))
                .thenReturn(List.of(Debtor.builder().karyawanName("Signer Name").jabatan("Director").build()));

        CommonResult<SitDto> result = service.getAgreementsByFinancingHdrCode(FINANCING_HDR_CODE);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getData().getAgreementCode()).isEqualTo("AGR001");
        assertThat(result.getData().getBouwheerName()).isEqualTo("PT BOUWHEER");
        assertThat(result.getData().getCustName()).isEqualTo("Agreement Customer");
        assertThat(result.getData().getDirectorName()).isEqualTo("Signer Name");
        assertThat(result.getData().getJabatan()).isEqualTo("Director");
        assertThat(result.getData().getEmployeeName()).isEqualTo("John Doe");
        assertThat(result.getData().getBankName()).isEqualTo("Bank Mandiri");
    }

    @Test
    void getAgreementsByFinancingHdrCodeUsesNotAvailableEmployeeNameWhenExistingAgreementEmployeeListEmpty() {
        FinancingHdr financingHdr = financingHdr(customer("Agreement Customer"), bouwheer());
        Agreement agreement = Agreement.builder().agreementCode("AGR001A").financingHdr(financingHdr).build();
        when(agreementRepository.findByFinancingHdr_FinancingHdrCode(FINANCING_HDR_CODE)).thenReturn(List.of(agreement));
        when(financingHdrRepository.findBranchCodeByFinancingHdrCode(FINANCING_HDR_CODE)).thenReturn("BDG");
        when(emailAo.getEmailByPosition("BDG", "RM", "jwt-token")).thenReturn(List.of());
        when(financingHdrRepository.findDebtorNameByFinancingHdrCode(FINANCING_HDR_CODE)).thenReturn("Debtor Name");
        when(debtorRepository.findActiveSignerByDebtorName("Debtor Name")).thenReturn(List.of());

        CommonResult<SitDto> result = service.getAgreementsByFinancingHdrCode(FINANCING_HDR_CODE);

        assertThat(result.getData().getEmployeeName()).isEqualTo("N/A");
    }

    @Test
    @SuppressWarnings("unchecked")
    void getAgreementsByFinancingHdrCodeReturnsFailWhenAgreementListIsReportedNonEmptyButStreamsNoData() {
        List<Agreement> agreements = mock(List.class);
        when(agreements.isEmpty()).thenReturn(false);
        when(agreements.stream()).thenReturn(Stream.empty());
        when(agreementRepository.findByFinancingHdr_FinancingHdrCode(FINANCING_HDR_CODE)).thenReturn(agreements);

        CommonResult<SitDto> result = service.getAgreementsByFinancingHdrCode(FINANCING_HDR_CODE);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getCode()).isEqualTo(404);
        assertThat(result.getMessage()).isEqualTo("No valid SitDto found.");
    }

    @Test
    void getAgreementsByFinancingHdrCodeKeepsEmptyEmployeeNameWhenRemoteNameIsEmpty() {
        FinancingHdr financingHdr = financingHdr(customer("Agreement Customer"), bouwheer());
        Agreement agreement = Agreement.builder().agreementCode("AGR002").financingHdr(financingHdr).build();
        when(agreementRepository.findByFinancingHdr_FinancingHdrCode(FINANCING_HDR_CODE)).thenReturn(List.of(agreement));
        when(financingHdrRepository.findBranchCodeByFinancingHdrCode(FINANCING_HDR_CODE)).thenReturn("BDG");
        when(emailAo.getEmailByPosition("BDG", "RM", "jwt-token"))
                .thenReturn(List.of(Map.of("employeeName", "")));
        when(financingHdrRepository.findDebtorNameByFinancingHdrCode(FINANCING_HDR_CODE)).thenReturn("Debtor Name");
        when(debtorRepository.findActiveSignerByDebtorName("Debtor Name")).thenReturn(List.of());

        CommonResult<SitDto> result = service.getAgreementsByFinancingHdrCode(FINANCING_HDR_CODE);

        assertThat(result.getData().getEmployeeName()).isEmpty();
        assertThat(result.getData().getDirectorName()).isEqualTo("-");
        assertThat(result.getData().getJabatan()).isEqualTo("-");
    }

    @Test
    void getAgreementsByFinancingHdrCodeKeepsNullEmployeeNameWhenRemoteNameIsNull() {
        FinancingHdr financingHdr = financingHdr(customer("Agreement Customer"), bouwheer());
        Agreement agreement = Agreement.builder().agreementCode("AGR003").financingHdr(financingHdr).build();
        when(agreementRepository.findByFinancingHdr_FinancingHdrCode(FINANCING_HDR_CODE)).thenReturn(List.of(agreement));
        when(financingHdrRepository.findBranchCodeByFinancingHdrCode(FINANCING_HDR_CODE)).thenReturn("BDG");
        when(emailAo.getEmailByPosition("BDG", "RM", "jwt-token"))
                .thenReturn(List.of(new java.util.HashMap<>() {{
                    put("employeeName", null);
                }}));
        when(financingHdrRepository.findDebtorNameByFinancingHdrCode(FINANCING_HDR_CODE)).thenReturn("Debtor Name");
        when(debtorRepository.findActiveSignerByDebtorName("Debtor Name")).thenReturn(List.of());

        CommonResult<SitDto> result = service.getAgreementsByFinancingHdrCode(FINANCING_HDR_CODE);

        assertThat(result.getData().getEmployeeName()).isNull();
    }

    private static Customer customer(String name) {
        Customer customer = new Customer();
        customer.setCustName(name);
        return customer;
    }

    private static Bouwheer bouwheer() {
        return Bouwheer.builder()
                .bouwheerName("PT BOUWHEER")
                .legalAddress("Legal Address")
                .rt("001")
                .rw("002")
                .kelurahan("Kelurahan")
                .kecamatan("Kecamatan")
                .city("Jakarta")
                .province("DKI Jakarta")
                .zipcode("12345")
                .area("JKT")
                .picName("PIC")
                .build();
    }

    private static FinancingHdr financingHdr(Customer customer, Bouwheer bouwheer) {
        FinancingHdr financingHdr = new FinancingHdr();
        financingHdr.setFinancingHdrCode(FINANCING_HDR_CODE);
        financingHdr.setCustomer(customer);
        financingHdr.setBouwheer(bouwheer);
        financingHdr.setFapDate(LocalDateTime.of(2026, 8, 12, 10, 0));
        financingHdr.setFapStatus("APPROVED");
        financingHdr.setTotalInvoiceAmt(1_000_000D);
        financingHdr.setFinancingDueDate(LocalDateTime.of(2026, 9, 12, 10, 0));
        return financingHdr;
    }
}
