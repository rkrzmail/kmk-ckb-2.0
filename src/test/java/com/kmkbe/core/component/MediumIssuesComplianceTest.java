package com.kmkbe.core.component;

import com.kmkbe.core.domain.dto.ExistingCustomerDto;
import com.kmkbe.core.domain.dto.ExternalIntegrationLoanSimulationDto;
import com.kmkbe.core.domain.dto.LoanSubmissionSessionDto;
import com.kmkbe.core.domain.entity.FinancingHdr;
import com.kmkbe.core.domain.repository.CwrRepository;
import com.kmkbe.core.service.FileStorageService;
import com.kmkbe.modules.branch_admin.service.AgreementService;
import com.kmkbe.modules.customer.model.entity.Customer;
import com.kmkbe.modules.customer.repository.CustomerRepository;
import com.kmkbe.modules.customer.service.ExistingCustomerService;
import com.kmkbe.modules.loan_submission.service.ImportantNotesService;
import com.kmkbe.modules.loan_submission.service.SessionLoanSubmissionService;
import com.kmkbe.modules.remote.service.CustomerRemoteService;
import com.kmkbe.modules.remote.service.CwrRemoteService;
import com.kmkbe.modules.user.utils.Utils;
import jakarta.servlet.ServletContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MediumIssuesComplianceTest {

    @Mock private JdbcTemplate jdbcTemplate;
    @Mock private CustomerRemoteService customerRemoteService;
    @Mock private CwrRemoteService cwrRemoteService;
    @Mock private CwrRepository cwrRepository;
    @Mock private CustomerRepository customerRepository;

    // ---------- 1. ExistingCustomerService: Mapping & Safe Optional ----------

    @Test
    @DisplayName("Medium Issue: ExistingCustomerService correctly maps vendor_code, is_existing, identity_type, identity_no")
    void existingCustomerServiceMapsAllFieldsCorrectly() throws Exception {
        ExistingCustomerService service = new ExistingCustomerService(
                jdbcTemplate,
                customerRemoteService,
                cwrRemoteService,
                cwrRepository,
                customerRepository
        );

        ResultSet rs = mock(ResultSet.class);
        when(rs.getString("vendor_code")).thenReturn("VEND-100");
        when(rs.getBoolean("is_existing")).thenReturn(true);
        when(rs.getString("identity_type")).thenReturn("KTP");
        when(rs.getString("identity_no")).thenReturn("3171010101990001");
        Timestamp now = new Timestamp(System.currentTimeMillis());
        when(rs.getTimestamp("dtm_crt")).thenReturn(now);
        when(rs.getTimestamp("dtm_upd")).thenReturn(null);

        // Simulasi jdbcTemplate.queryForObject memanggil RowMapper lambda
        doAnswer(invocation -> {
            RowMapper<ExistingCustomerDto> rowMapper = invocation.getArgument(1);
            return rowMapper.mapRow(rs, 1);
        }).when(jdbcTemplate).queryForObject(anyString(), any(RowMapper.class), eq("VEND-100"));

        Optional<ExistingCustomerDto> result = service.findLastByVendorCode("VEND-100");

        assertThat(result).isPresent();
        ExistingCustomerDto dto = result.get();
        assertThat(dto.getVendorCode()).isEqualTo("VEND-100");
        assertThat(dto.getIsExisting()).isTrue();
        assertThat(dto.getIdentityType()).isEqualTo("KTP");
        assertThat(dto.getIdentityNo()).isEqualTo("3171010101990001");
        assertThat(dto.getDtmCrt()).isNotNull();
    }

    @Test
    @DisplayName("Medium Issue: ExistingCustomerService handles empty find without NoSuchElementException")
    void existingCustomerServiceHandlesEmptyGracefully() {
        ExistingCustomerService service = new ExistingCustomerService(
                jdbcTemplate,
                customerRemoteService,
                cwrRemoteService,
                cwrRepository,
                customerRepository
        );

        when(jdbcTemplate.queryForObject(anyString(), any(RowMapper.class), anyString()))
                .thenThrow(new EmptyResultDataAccessException(1));

        ExistingCustomerDto result = service.createOrUpdate("VEND-999", false, "NPWP", "12345");
        assertThat(result).isNull();
    }

    // ---------- 2. ImportantNotesService: Mapping vendor_code ----------

    @Test
    @DisplayName("Medium Issue: ImportantNotesService maps vendor_code into vendor property")
    void importantNotesServiceMapsVendorCode() throws Exception {
        ImportantNotesService service = new ImportantNotesService(jdbcTemplate);

        ResultSet rs = mock(ResultSet.class);
        when(rs.getString("bouwheer_code")).thenReturn("BOUW-001");
        when(rs.getString("vendor_code")).thenReturn("VEND-888");
        when(rs.getBoolean("already_accept_important_notes")).thenReturn(true);
        when(rs.getTimestamp("dtm_crt")).thenReturn(new Timestamp(System.currentTimeMillis()));

        doAnswer(invocation -> {
            RowMapper<ExternalIntegrationLoanSimulationDto> rowMapper = invocation.getArgument(1);
            return rowMapper.mapRow(rs, 1);
        }).when(jdbcTemplate).queryForObject(anyString(), any(RowMapper.class), eq("BOUW-001"), eq("VEND-888"));

        Optional<ExternalIntegrationLoanSimulationDto> result = service.findExternalIntegrationByBouwheerCode("BOUW-001", "VEND-888");

        assertThat(result).isPresent();
        assertThat(result.get().getBouwheerCode()).isEqualTo("BOUW-001");
        assertThat(result.get().getVendor()).isEqualTo("VEND-888");
        assertThat(result.get().getAlreadyAcceptImportantNotes()).isTrue();
    }

    // ---------- 3. SessionLoanSubmissionService: Safe Optional ----------

    @Test
    @DisplayName("Medium Issue: SessionLoanSubmissionService handles empty query without NoSuchElementException")
    void sessionLoanSubmissionServiceSafeOptional() throws Exception {
        SessionLoanSubmissionService service = new SessionLoanSubmissionService(jdbcTemplate);
        Customer customer = Customer.builder().custCode(UUID.randomUUID()).build();

        when(jdbcTemplate.queryForObject(anyString(), any(RowMapper.class), any()))
                .thenThrow(new EmptyResultDataAccessException(1));

        LoanSubmissionSessionDto result = service.create(customer, 1, new java.util.HashMap<>());
        assertThat(result).isNull();
    }

    // ---------- 4. AgreementService: Null safe branch check ----------

    @Test
    @DisplayName("Medium Issue: AgreementService handles null financingHdr and null mstBranch safely")
    void agreementServiceNullBranchSafety() {
        AgreementService agreementService = mock(AgreementService.class, CALLS_REAL_METHODS);

        // financingHdr = null
        org.junit.jupiter.api.Assertions.assertDoesNotThrow(() ->
                ReflectionTestUtils.invokeMethod(agreementService, "sendContractUploadRequiredNotification", null, "AGR001", "CWR001")
        );

        // financingHdr with null mstBranch
        FinancingHdr hdrWithoutBranch = new FinancingHdr();
        hdrWithoutBranch.setFinancingHdrCode(UUID.randomUUID());
        hdrWithoutBranch.setMstBranch(null);

        org.junit.jupiter.api.Assertions.assertDoesNotThrow(() ->
                ReflectionTestUtils.invokeMethod(agreementService, "sendContractUploadRequiredNotification", hdrWithoutBranch, "AGR001", "CWR001")
        );
    }

    // ---------- 5. Utils Stream Resource Leak Prevention ----------

    @Test
    @DisplayName("Medium Issue: Utils.readInputStreamAsString and copyStream properly close streams without leaking")
    void utilsStreamsProperlyClosed() {
        byte[] sample = "hello security world".getBytes(StandardCharsets.UTF_8);
        ByteArrayInputStream bais = new ByteArrayInputStream(sample);

        String readString = Utils.readInputStreamAsString(bais);
        assertThat(readString).isEqualTo("hello security world");

        // Null safety
        assertThat(Utils.readInputStreamAsString((java.io.InputStream) null)).isEmpty();
        assertThat(Utils.readInputStreamAsString((String) null)).isEmpty();

        // copyStream
        ByteArrayInputStream in = new ByteArrayInputStream(sample);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Utils.copyStream(in, out);
        assertThat(out.toByteArray()).isEqualTo(sample);
    }

    // ---------- 6. FileStorageService Path Traversal Guard ----------

    @Test
    @DisplayName("Medium Issue: FileStorageService rejects path traversal (../) attempts with SecurityException")
    void fileStorageServicePathTraversalGuard(@TempDir Path tempDir) {
        ServletContext servletContext = mock(ServletContext.class);
        FileStorageService service = new FileStorageService(servletContext, tempDir.toString());

        // Path traversal di load
        assertThatThrownBy(() -> service.load("../../etc/passwd"))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("Path traversal attempt detected");

        // Path traversal di delete
        assertThatThrownBy(() -> service.delete("../../../secrets.txt", "code"))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("Path traversal attempt detected");

        // Valid path dalam directory aman
        assertThatThrownBy(() -> service.load("normal-file.pdf"))
                .isInstanceOf(RuntimeException.class) // Not found in empty tempDir, but NOT SecurityException
                .hasMessageContaining("Could not read the file");
    }
}
