package com.kmkbe.modules.branch_admin.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kmkbe.core.domain.dto.BaseMstRemoteResponseDto;
import com.kmkbe.core.domain.dto.BaseSimpleRemoteResponseDto;
import com.kmkbe.core.domain.dto.InquiryAgreementCwrDto;
import com.kmkbe.core.domain.dto.InquiryAgreementDto;
import com.kmkbe.core.domain.entity.Agreement;
import com.kmkbe.core.domain.entity.AgreementFile;
import com.kmkbe.core.domain.entity.Cwr;
import com.kmkbe.core.domain.entity.FinancingDtl;
import com.kmkbe.core.domain.entity.FinancingHdr;
import com.kmkbe.core.domain.entity.GeneralSettingDtl;
import com.kmkbe.core.domain.entity.Invoice;
import com.kmkbe.core.domain.entity.MstFileType;
import com.kmkbe.core.domain.model.PaginationResult;
import com.kmkbe.core.domain.repository.AgreementFileRepository;
import com.kmkbe.core.domain.repository.AgreementRepository;
import com.kmkbe.core.domain.repository.CwrRepository;
import com.kmkbe.core.domain.repository.FinancingDtlRepository;
import com.kmkbe.core.domain.repository.FinancingHdrRepository;
import com.kmkbe.core.domain.repository.GeneralSettingDtlRepository;
import com.kmkbe.core.domain.repository.MstFileTypeRepository;
import com.kmkbe.core.domain.request.PaginationRequest;
import com.kmkbe.core.exception.CommonInvalidException;
import com.kmkbe.core.service.FileStorageService;
import com.kmkbe.modules.bouwheer.model.entity.Bouwheer;
import com.kmkbe.modules.branch_admin.request.CreateInquiryAgreementRequest;
import com.kmkbe.modules.common.service.EmailService;
import com.kmkbe.modules.customer.model.entity.Customer;
import com.kmkbe.modules.remote.request.FinancingSubmissionRequest;
import com.kmkbe.modules.remote.service.CwrRemoteService;
import com.kmkbe.modules.remote.service.FinancingRemoteService;
import com.kmkbe.modules.user.entity.MstUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AgreementServiceTest {

    private static final UUID FINANCING_HDR_CODE = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID CUSTOMER_CODE = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID BOUWHEER_CODE = UUID.fromString("33333333-3333-3333-3333-333333333333");

    @Mock private MstFileTypeRepository mstFileTypeRepository;
    @Mock private AgreementFileRepository agreementFileRepository;
    @Mock private AgreementRepository agreementRepository;
    @Mock private FinancingHdrRepository financingHdrRepository;
    @Mock private FinancingDtlRepository financingDtlRepository;
    @Mock private CwrRepository cwrRepository;
    @Mock private GeneralSettingDtlRepository generalSettingDtlRepository;
    @Mock private FinancingRemoteService financingRemoteService;
    @Mock private CwrRemoteService cwrRemoteService;
    @Mock private FileStorageService fileStorageService;
    @Mock private EmailService emailService;

    private ObjectMapper objectMapper;
    private AgreementService service;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        service = new AgreementService(
                mstFileTypeRepository,
                agreementFileRepository,
                agreementRepository,
                financingHdrRepository,
                financingDtlRepository,
                cwrRepository,
                generalSettingDtlRepository,
                financingRemoteService,
                cwrRemoteService,
                fileStorageService,
                objectMapper,
                emailService
        );
    }

    @Test
    void findByCodeReturnsAgreementOrNullAndRethrowsRepositoryError() {
        Agreement agreement = Agreement.builder().agreementCode("AGR001").build();
        when(agreementRepository.findById("AGR001")).thenReturn(Optional.of(agreement));
        when(agreementRepository.findById("AGR404")).thenReturn(Optional.empty());
        when(agreementRepository.findById("ERR")).thenThrow(new IllegalStateException("repo error"));

        assertThat(service.findByCode("AGR001")).isSameAs(agreement);
        assertThat(service.findByCode("AGR404")).isNull();
        assertThatThrownBy(() -> service.findByCode("ERR"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("repo error");
    }

    @Test
    void findByFinancingHdrReturnsAgreementOrNullAndRethrowsRepositoryError() {
        FinancingHdr financingHdr = financingHdr();
        Agreement agreement = Agreement.builder().agreementCode("AGR001").build();
        when(agreementRepository.findTopByFinancingHdr(financingHdr)).thenReturn(Optional.of(agreement), Optional.empty());

        assertThat(service.findByFinancingHdr(financingHdr)).isSameAs(agreement);
        assertThat(service.findByFinancingHdr(financingHdr)).isNull();
    }

    @Test
    void findByFinancingHdrRethrowsRepositoryError() {
        FinancingHdr financingHdr = financingHdr();
        when(agreementRepository.findTopByFinancingHdr(financingHdr)).thenThrow(new IllegalStateException("repo error"));

        assertThatThrownBy(() -> service.findByFinancingHdr(financingHdr))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("repo error");
    }

    @Test
    void listMapsRawAgreementRowsWithDefaultsForNullValues() throws Exception {
        stubBank();
        Map<String, Object> fullRow = new java.util.HashMap<>();
        fullRow.put("no", 1);
        fullRow.put("agreement_code", "AGR001");
        fullRow.put("financing_hdr_code", FINANCING_HDR_CODE.toString());
        fullRow.put("bouwheer_code", BOUWHEER_CODE.toString());
        fullRow.put("cust_code", CUSTOMER_CODE.toString());
        fullRow.put("bouwheer_name", "PT BOUWHEER");
        fullRow.put("cust_name", "Customer");
        fullRow.put("financing_amt", 1000.0);
        fullRow.put("disburse_date", "2026-08-12 10:00:00");
        fullRow.put("disburse_amt", 900.0);
        fullRow.put("currency", "IDR");
        Map<String, Object> nullRow = new java.util.HashMap<>();
        Page<Map<String, Object>> page = new PageImpl<>(List.of(fullRow, nullRow));
        when(agreementRepository.findAllListByCwrAndFinancingRaw(eq("CWR001"), eq(FINANCING_HDR_CODE.toString()), any()))
                .thenReturn(page);

        PaginationRequest request = new PaginationRequest();
        request.setPageNo(1);
        request.setPageSize(2);
        PaginationResult<?> result = service.list("CWR001", FINANCING_HDR_CODE.toString(), request);

        assertThat(result.getCurrentPage()).isEqualTo(1);
        assertThat(result.getTotalData()).isEqualTo(2);
        assertThat(result.getList()).hasSize(2);
    }

    @Test
    void listUsesDefaultPaginationWhenRequestValuesAreNullAndRethrowsError() throws Exception {
        stubBank();
        when(agreementRepository.findAllListByCwrAndFinancingRaw(eq("CWR001"), eq(FINANCING_HDR_CODE.toString()), any()))
                .thenReturn(new PageImpl<>(List.of()));
        PaginationRequest request = new PaginationRequest();

        PaginationResult<?> result = service.list("CWR001", FINANCING_HDR_CODE.toString(), request);

        assertThat(result.getCurrentPage()).isEqualTo(1);
        assertThat(result.getList()).isEmpty();
    }

    @Test
    void listRethrowsWhenBankSettingMissing() {
        when(agreementRepository.findAllListByCwrAndFinancingRaw(eq("CWR001"), eq(FINANCING_HDR_CODE.toString()), any()))
                .thenReturn(new PageImpl<>(List.of()));
        when(generalSettingDtlRepository.findTopByGsDtlCode("DTLBANK001")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.list("CWR001", FINANCING_HDR_CODE.toString(), new PaginationRequest()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Bank not found or not valid");
    }

    @Test
    void uploadCreatesNewAgreementFile() throws Exception {
        MstUser user = MstUser.builder().username("uploader").build();
        MultipartFile file = new MockMultipartFile("file", "agreement.pdf", "application/pdf", "pdf".getBytes());
        Agreement agreement = agreementWithFinancing();
        MstFileType fileType = MstFileType.builder().fileTypeCode("AGGREMENT01").build();
        when(mstFileTypeRepository.findByFileTypeCode("AGGREMENT01")).thenReturn(Optional.of(fileType));
        when(agreementRepository.findTopByAgreementCodeOrderByAgreementId("AGR001")).thenReturn(Optional.of(agreement));
        when(agreementFileRepository.findTopByAgreementOrderByAgreementFileId(agreement)).thenReturn(Optional.empty());
        when(fileStorageService.save(eq(file), eq(CUSTOMER_CODE + "/agreement"), eq("AGGREMENT01_agreement.pdf"), eq(null)))
                .thenReturn("/root/uploads/customer/agreement/AGGREMENT01_agreement.pdf");

        service.upload(user, file, "AGR001");

        ArgumentCaptor<AgreementFile> captor = ArgumentCaptor.forClass(AgreementFile.class);
        verify(agreementFileRepository).save(captor.capture());
        assertThat(captor.getValue().getAgreement()).isSameAs(agreement);
        assertThat(captor.getValue().getMstFileType()).isSameAs(fileType);
        assertThat(captor.getValue().getFileName()).isEqualTo("AGGREMENT01_agreement.pdf");
        assertThat(captor.getValue().getContentType()).isEqualTo("application/pdf");
        assertThat(captor.getValue().getUsrCrt()).isEqualTo("uploader");
    }

    @Test
    void uploadUpdatesExistingAgreementFile() throws Exception {
        MstUser user = MstUser.builder().username("uploader").build();
        MultipartFile file = new MockMultipartFile("file", "new.pdf", "application/pdf", "pdf".getBytes());
        Agreement agreement = agreementWithFinancing();
        MstFileType fileType = MstFileType.builder().fileTypeCode("AGGREMENT01").build();
        AgreementFile existing = AgreementFile.builder().agreementFileId(1L).fileName("old.pdf").build();
        when(mstFileTypeRepository.findByFileTypeCode("AGGREMENT01")).thenReturn(Optional.of(fileType));
        when(agreementRepository.findTopByAgreementCodeOrderByAgreementId("AGR001")).thenReturn(Optional.of(agreement));
        when(agreementFileRepository.findTopByAgreementOrderByAgreementFileId(agreement)).thenReturn(Optional.of(existing));
        when(fileStorageService.save(eq(file), eq(CUSTOMER_CODE + "/agreement"), eq("AGGREMENT01_new.pdf"), eq(null)))
                .thenReturn("/root/uploads/customer/agreement/AGGREMENT01_new.pdf");

        service.upload(user, file, "AGR001");

        verify(agreementFileRepository).save(existing);
        assertThat(existing.getFileName()).isEqualTo("AGGREMENT01_new.pdf");
        assertThat(existing.getUsrUpd()).isEqualTo("uploader");
        assertThat(existing.getDtmUpd()).isNotNull();
    }

    @Test
    void uploadThrowsForMissingFileTypeAgreementAndFinancing() {
        MultipartFile file = new MockMultipartFile("file", "agreement.pdf", "application/pdf", "pdf".getBytes());
        MstUser user = MstUser.builder().username("uploader").build();
        when(mstFileTypeRepository.findByFileTypeCode("AGGREMENT01")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.upload(user, file, "AGR001"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("File type not found");
    }

    @Test
    void uploadThrowsWhenAgreementMissingOrFinancingMissing() {
        MultipartFile file = new MockMultipartFile("file", "agreement.pdf", "application/pdf", "pdf".getBytes());
        MstUser user = MstUser.builder().username("uploader").build();
        MstFileType fileType = MstFileType.builder().fileTypeCode("AGGREMENT01").build();
        when(mstFileTypeRepository.findByFileTypeCode("AGGREMENT01")).thenReturn(Optional.of(fileType));
        when(agreementRepository.findTopByAgreementCodeOrderByAgreementId("AGR404")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.upload(user, file, "AGR404"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Agreement not found");

        Agreement agreementWithoutFinancing = Agreement.builder().agreementCode("AGRNOFIN").build();
        when(agreementRepository.findTopByAgreementCodeOrderByAgreementId("AGRNOFIN")).thenReturn(Optional.of(agreementWithoutFinancing));
        when(agreementFileRepository.findTopByAgreementOrderByAgreementFileId(agreementWithoutFinancing)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.upload(user, file, "AGRNOFIN"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Agreement Financing not found");
    }

    @Test
    void inquiryAgreementCwrReturnsDataWhenRemoteInquiryHasResult() throws Exception {
        stubBank();
        when(agreementRepository.findById("AGR001")).thenReturn(Optional.empty());
        BaseMstRemoteResponseDto<List<InquiryAgreementCwrDto>> response = new BaseMstRemoteResponseDto<>();
        response.setData(List.of(inquiryAgreement("CWR001", "AGR001")));
        when(cwrRemoteService.inquiryAgreementByNoAgreement(any())).thenReturn(response);

        InquiryAgreementDto result = service.inquiryAgreementCwr("CWR001", "AGR001");

        assertThat(result.getBankName()).isEqualTo("Bank Mandiri");
        assertThat(result.getRekeningNo()).isEqualTo("127");
        assertThat(result.getCurrency()).isEqualTo("IDR");
        assertThat(result.getDisburseAmt()).isEqualByComparingTo(BigDecimal.valueOf(1000.0));
    }

    @Test
    void inquiryAgreementCwrThrowsWhenAgreementExistsRemoteThrowsOrDataEmpty() throws Exception {
        when(agreementRepository.findById("AGR001")).thenReturn(Optional.of(Agreement.builder().build()));
        assertThatThrownBy(() -> service.inquiryAgreementCwr("CWR001", "AGR001"))
                .isInstanceOf(IllegalStateException.class);

        when(agreementRepository.findById("AGR002")).thenReturn(Optional.empty());
        doThrow(new RuntimeException("remote down")).when(cwrRemoteService).inquiryAgreementByNoAgreement(any());
        assertThatThrownBy(() -> service.inquiryAgreementCwr("CWR001", "AGR002"))
                .isInstanceOf(CommonInvalidException.class);

        BaseMstRemoteResponseDto<List<InquiryAgreementCwrDto>> emptyResponse = new BaseMstRemoteResponseDto<>();
        emptyResponse.setData(List.of());
        when(agreementRepository.findById("AGR003")).thenReturn(Optional.empty());
        doReturn(emptyResponse).when(cwrRemoteService).inquiryAgreementByNoAgreement(any());
        assertThatThrownBy(() -> service.inquiryAgreementCwr("CWR001", "AGR003"))
                .isInstanceOf(CommonInvalidException.class);

        BaseMstRemoteResponseDto<List<InquiryAgreementCwrDto>> nullResponse = new BaseMstRemoteResponseDto<>();
        nullResponse.setData(null);
        when(agreementRepository.findById("AGR004")).thenReturn(Optional.empty());
        doReturn(nullResponse).when(cwrRemoteService).inquiryAgreementByNoAgreement(any());
        assertThatThrownBy(() -> service.inquiryAgreementCwr("CWR001", "AGR004"))
                .isInstanceOf(CommonInvalidException.class);
    }

    @Test
    void createInquiryAgreementSavesAgreementAndProceedFinancingWithBypass() throws Exception {
        stubBank();
        MstUser user = MstUser.builder().usrCrt("creator").build();
        CreateInquiryAgreementRequest request = createRequest("AGR001", "CWR001");
        FinancingHdr financingHdr = financingHdr();
        financingHdr.setCustomer(customer());
        financingHdr.setBouwheer(bouwheer());
        when(agreementRepository.findById("AGR001")).thenReturn(Optional.empty());
        BaseMstRemoteResponseDto<List<InquiryAgreementCwrDto>> response = new BaseMstRemoteResponseDto<>();
        response.setData(List.of(inquiryAgreement("CWR001", "AGR001")));
        when(cwrRemoteService.inquiryAgreementByNoAgreement(any())).thenReturn(response);
        when(financingHdrRepository.findByFinancingHdrCode(FINANCING_HDR_CODE)).thenReturn(Optional.of(financingHdr));
        when(cwrRepository.findTopByCwrCode("CWR001")).thenReturn(Optional.of(Cwr.builder().cwrCode("CWR001").build()));
        when(financingDtlRepository.findAllByFinancingHdr(financingHdr)).thenReturn(Optional.of(List.of(financingDtl(true))));

        service.createInquiryAgreement(user, request);

        verify(agreementRepository).saveAll(any());
        assertThat(financingHdr.getFinancingStatus()).isEqualTo("INPROCESS");
        assertThat(financingHdr.getFinancingStep()).isEqualTo("INPROCESS");
        verify(financingHdrRepository).save(financingHdr);
        verify(emailService).sendNotificationBouwheerPayment(eq("pic@example.com"), any());
        verify(financingRemoteService, never()).postedSubmission(any());
    }

    @Test
    void createInquiryAgreementCoversRemotePostingWhenBypassIsFalse() throws Exception {
        AgreementService postingService = postingService(false);
        stubBank();
        MstUser user = MstUser.builder().usrCrt("creator").build();
        CreateInquiryAgreementRequest request = createRequest("AGR001", "CWR001");
        FinancingHdr financingHdr = financingHdr();
        financingHdr.setCustomer(customer());
        financingHdr.setBouwheer(bouwheer());
        BaseMstRemoteResponseDto<List<InquiryAgreementCwrDto>> response = new BaseMstRemoteResponseDto<>();
        response.setData(List.of());
        when(agreementRepository.findById("AGR001")).thenReturn(Optional.empty());
        when(cwrRemoteService.inquiryAgreementByNoAgreement(any())).thenReturn(response);
        when(financingHdrRepository.findByFinancingHdrCode(FINANCING_HDR_CODE)).thenReturn(Optional.of(financingHdr));
        when(financingDtlRepository.findAllByFinancingHdr(financingHdr)).thenReturn(Optional.of(List.of(financingDtl(true))));
        BaseSimpleRemoteResponseDto<Object> postedResponse = new BaseSimpleRemoteResponseDto<>();
        postedResponse.setData(Map.of("email_address", "remote@example.com"));
        when(financingRemoteService.postedSubmission(any(FinancingSubmissionRequest.class))).thenReturn(postedResponse);

        postingService.createInquiryAgreement(user, request);

        verify(financingRemoteService).postedSubmission(any(FinancingSubmissionRequest.class));
        verify(emailService).sendNotificationBouwheerPayment(eq("remote@example.com"), any());
    }

    @Test
    void createInquiryAgreementRemotePostingKeepsBouwheerEmailWhenRemoteBodyHasNoEmail() throws Exception {
        AgreementService postingService = postingService(false);
        stubBank();
        MstUser user = MstUser.builder().usrCrt("creator").build();
        CreateInquiryAgreementRequest request = createRequest("AGR005", "CWR001");
        FinancingHdr financingHdr = financingHdr();
        financingHdr.setCustomer(customer());
        financingHdr.setBouwheer(bouwheer());
        BaseMstRemoteResponseDto<List<InquiryAgreementCwrDto>> response = new BaseMstRemoteResponseDto<>();
        response.setData(List.of());
        when(agreementRepository.findById("AGR005")).thenReturn(Optional.empty());
        when(cwrRemoteService.inquiryAgreementByNoAgreement(any())).thenReturn(response);
        when(financingHdrRepository.findByFinancingHdrCode(FINANCING_HDR_CODE)).thenReturn(Optional.of(financingHdr));
        when(financingDtlRepository.findAllByFinancingHdr(financingHdr)).thenReturn(Optional.of(List.of(financingDtl(true))));
        BaseSimpleRemoteResponseDto<Object> postedResponse = new BaseSimpleRemoteResponseDto<>();
        postedResponse.setData(Map.of("other", "value"));
        when(financingRemoteService.postedSubmission(any(FinancingSubmissionRequest.class))).thenReturn(postedResponse);

        postingService.createInquiryAgreement(user, request);

        verify(emailService).sendNotificationBouwheerPayment(eq("pic@example.com"), any());
    }

    @Test
    void createInquiryAgreementThrowsForRemoteErrorMissingFinancingCustomerCwrMismatchAndInvalidInvoices() throws Exception {
        MstUser user = MstUser.builder().usrCrt("creator").build();
        when(agreementRepository.findById("REMOTE")).thenReturn(Optional.empty());
        doThrow(new RuntimeException("remote")).when(cwrRemoteService).inquiryAgreementByNoAgreement(any());
        assertThatThrownBy(() -> service.createInquiryAgreement(user, createRequest("REMOTE", "CWR001")))
                .isInstanceOf(CommonInvalidException.class);

        BaseMstRemoteResponseDto<List<InquiryAgreementCwrDto>> response = new BaseMstRemoteResponseDto<>();
        response.setData(List.of(inquiryAgreement("CWR001", "AGR404")));
        when(agreementRepository.findById("AGR404")).thenReturn(Optional.empty());
        doReturn(response).when(cwrRemoteService).inquiryAgreementByNoAgreement(any());
        when(financingHdrRepository.findByFinancingHdrCode(FINANCING_HDR_CODE)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.createInquiryAgreement(user, createRequest("AGR404", "CWR001")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Financing not found or not valid");

        FinancingHdr noCustomer = financingHdr();
        noCustomer.setCustomer(null);
        when(agreementRepository.findById("AGRCUST")).thenReturn(Optional.empty());
        when(financingHdrRepository.findByFinancingHdrCode(FINANCING_HDR_CODE)).thenReturn(Optional.of(noCustomer));
        assertThatThrownBy(() -> service.createInquiryAgreement(user, createRequest("AGRCUST", "CWR001")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Customer not found or not valid");
    }

    @Test
    void createInquiryAgreementThrowsWhenCwrMissingOrMismatchOrFinancingInvoicesInvalid() throws Exception {
        MstUser user = MstUser.builder().usrCrt("creator").build();
        BaseMstRemoteResponseDto<List<InquiryAgreementCwrDto>> response = new BaseMstRemoteResponseDto<>();
        response.setData(List.of(inquiryAgreement("CWR001", "AGRCWR")));
        when(agreementRepository.findById("AGRCWR")).thenReturn(Optional.empty());
        when(cwrRemoteService.inquiryAgreementByNoAgreement(any())).thenReturn(response);
        FinancingHdr financingHdr = financingHdr();
        financingHdr.setCustomer(customer());
        financingHdr.setBouwheer(bouwheer());
        when(financingHdrRepository.findByFinancingHdrCode(FINANCING_HDR_CODE)).thenReturn(Optional.of(financingHdr));
        when(cwrRepository.findTopByCwrCode("CWR001")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.createInquiryAgreement(user, createRequest("AGRCWR", "CWR001")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Nomor CWR tidak ditemukan, pastikan nomor CWR benar");

        response.setData(List.of(inquiryAgreement("CWR999", "AGRMISMATCH")));
        when(agreementRepository.findById("AGRMISMATCH")).thenReturn(Optional.empty());
        when(cwrRepository.findTopByCwrCode("CWR999")).thenReturn(Optional.of(Cwr.builder().cwrCode("CWR999").build()));
        assertThatThrownBy(() -> service.createInquiryAgreement(user, createRequest("AGRMISMATCH", "CWR001")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Nomor CWR tidak sesuai dengan Nomor Pencairan, pastikan Nomor Pencairan benar");

        response.setData(List.of());
        when(agreementRepository.findById("AGRNOINV")).thenReturn(Optional.empty());
        when(financingDtlRepository.findAllByFinancingHdr(financingHdr)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.createInquiryAgreement(user, createRequest("AGRNOINV", "CWR001")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Financing Invoice not found or not valid");

        when(agreementRepository.findById("AGRINVALIDINV")).thenReturn(Optional.empty());
        when(financingDtlRepository.findAllByFinancingHdr(financingHdr)).thenReturn(Optional.of(List.of(financingDtl(false))));
        assertThatThrownBy(() -> service.createInquiryAgreement(user, createRequest("AGRINVALIDINV", "CWR001")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("No Valid Invoice to submit Agreement");
    }

    @Test
    void privateHelpersAreCovered() throws Exception {
        stubBank();
        Object bank = ReflectionTestUtils.invokeMethod(service, "findCsulBank");
        assertThat(bank).isInstanceOf(Map.class);

        InquiryAgreementCwrDto sample = ReflectionTestUtils.invokeMethod(service, "sampleResponse");
        assertThat(sample.getAgrmntNo()).isEqualTo("41450241613");

        when(agreementRepository.findById("DUP")).thenReturn(Optional.of(Agreement.builder().build()));
        assertThatThrownBy(() -> ReflectionTestUtils.invokeMethod(service, "validateAgreement", "DUP"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Nomor Pencairan sudah di masukkan sebelumnya, silahkan masukkan Nomor Pencairan yg lain");
    }

    private AgreementService postingService(boolean bypass) {
        return new AgreementService(
                mstFileTypeRepository,
                agreementFileRepository,
                agreementRepository,
                financingHdrRepository,
                financingDtlRepository,
                cwrRepository,
                generalSettingDtlRepository,
                financingRemoteService,
                cwrRemoteService,
                fileStorageService,
                objectMapper,
                emailService
        ) {
            @Override
            boolean bypassRemotePosting() {
                return bypass;
            }
        };
    }

    private void stubBank() {
        GeneralSettingDtl bank = new GeneralSettingDtl();
        bank.setGsDtlValue("\"{\\\"bankName\\\":\\\"Bank Mandiri\\\",\\\"accountNo\\\":\\\"127\\\",\\\"accountName\\\":\\\"CHANDRA\\\",\\\"bankKey\\\":\\\"008\\\"}\"");
        when(generalSettingDtlRepository.findTopByGsDtlCode("DTLBANK001")).thenReturn(Optional.of(bank));
    }

    private static Agreement agreementWithFinancing() {
        return Agreement.builder()
                .agreementCode("AGR001")
                .financingHdr(financingHdr())
                .build();
    }

    private static FinancingHdr financingHdr() {
        FinancingHdr financingHdr = new FinancingHdr();
        financingHdr.setFinancingHdrCode(FINANCING_HDR_CODE);
        financingHdr.setCustomer(customer());
        financingHdr.setBouwheer(bouwheer());
        financingHdr.setFinancingAmt(1000D);
        financingHdr.setFinancingDate(LocalDateTime.of(2026, 8, 12, 10, 0));
        return financingHdr;
    }

    private static Customer customer() {
        Customer customer = new Customer();
        customer.setCustCode(CUSTOMER_CODE);
        customer.setCustName("Customer");
        customer.setCustExternalCode("VENDOR001");
        return customer;
    }

    private static Bouwheer bouwheer() {
        return Bouwheer.builder()
                .bouwheerCode(BOUWHEER_CODE)
                .bouwheerName("PT BOUWHEER")
                .picEmail("pic@example.com")
                .build();
    }

    private static InquiryAgreementCwrDto inquiryAgreement(String cwrCode, String agreementNo) {
        return InquiryAgreementCwrDto.builder()
                .cwrNo(cwrCode)
                .agrmntNo(agreementNo)
                .appNo("APP001")
                .facility("MODAL KERJA")
                .currency("IDR")
                .ntfAmt(1000D)
                .status("ACTIVE")
                .productOffering("PRODUCT")
                .build();
    }

    private static CreateInquiryAgreementRequest createRequest(String agreementNo, String cwrCode) {
        CreateInquiryAgreementRequest request = new CreateInquiryAgreementRequest();
        request.setAgreementNo(agreementNo);
        request.setCwrCode(cwrCode);
        request.setFinancingHdrCode(FINANCING_HDR_CODE.toString());
        return request;
    }

    private static FinancingDtl financingDtl(boolean validPo) {
        Invoice invoice = Invoice.builder()
                .invoiceAmt(1000D)
                .custInvNo("INV001")
                .bouwheerInvNo("BWH001")
                .invoiceDate(LocalDateTime.of(2026, 8, 1, 10, 0))
                .invoiceDueDate(LocalDateTime.of(2026, 9, 1, 10, 0))
                .poNumber(validPo ? "PO001" : null)
                .build();
        FinancingDtl detail = new FinancingDtl();
        detail.setInvoice(invoice);
        return detail;
    }
}
