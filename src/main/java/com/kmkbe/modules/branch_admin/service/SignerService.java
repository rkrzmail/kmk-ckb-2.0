package com.kmkbe.modules.branch_admin.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kmkbe.core.domain.dto.*;
import com.kmkbe.core.domain.entity.*;
import com.kmkbe.core.domain.mapper.DebtorMapper;
import com.kmkbe.core.domain.model.CommonResult;
import com.kmkbe.core.domain.model.MappedFinancingStatus;
import com.kmkbe.core.domain.model.PaginationResult;
import com.kmkbe.core.domain.repository.*;
import com.kmkbe.core.domain.request.PaginationRequest;
import com.kmkbe.core.service.FileStorageService;
import com.kmkbe.core.utils.UriUtils;
import com.kmkbe.modules.branch_admin.request.FileUploadRequest;
import com.kmkbe.modules.common.service.EmailService;
import com.kmkbe.modules.user.entity.MstAppRoleFormUser;
import com.kmkbe.modules.user.entity.MstUser;
import com.kmkbe.modules.user.repository.MstAppRoleFormUserRepository;
import com.kmkbe.modules.user.repository.MstUserRepository;
import com.kmkbe.modules.user.utils.UserInternalUtils;
import com.kmkbe.nikita.utils.SpecPagination;
import com.kmkbe.nikita.utils.Utils;
import io.micrometer.common.util.StringUtils;
import io.netty.util.internal.StringUtil;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.math.BigDecimal;
import java.security.SignatureException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SignerService {
    private final FinancingHdrRepository financingHdrRepository;
    private final MstUserRepository mstUserRepository;
    private final RestTemplate restTemplate;
    private final DebtorRepository debtorRepository;
    private final DebtorMapper debtorMapper = DebtorMapper.INSTANCE;
    private final EmailService emailService;
    private final AgreementRepository agreementRepository;
    private final AgreementFileSigningRepository agreementFileSigningRepository;
    private final MstAppRoleFormUserRepository mstAppRoleFormUserRepository;
    private final AgreementFileRepository agreementFileRepository;
    private final FileStorageService fileStorageService;
    private final NotifDebtorRepository notifDebtorRepository;
    private final CwrRepository cwrRepository;

    private final String apiKey = "YiByHB@CSUL_DEV";
    private final String callerId = "USER@AD-INS.COM";
    private final String registerUrl = "https://gdkwebserver.ad-ins.com/adimobile/demo/esign/services/external/user/checkRegistration";
    private final String generateLinkUrl = "https://gdkwebserver.ad-ins.com/adimobile/demo/esign/services/external/user/generateInvLink";
    private final String confinsUrl = "http://confins.csulfinance.com/api/mou/v1/CwrSigner/GetListCwrSignerForUpdatebyCustNoAndCwrNo";
    private final String sendDoc = "https://gdkwebserver.ad-ins.com/adimobile/demo/esign/services/external/document/sendDocumentSigning";
    private final String downloadDoc = "https://gdkwebserver.ad-ins.com/adimobile/demo/esign/services/external/document/downloadDocument";


    @Value("${csul.confins.adinskey}")
    private String adInsKey;

    public PaginationResult<AssignmentDto> assignmentList(
            HttpServletRequest httpServletRequest,
            Authentication authentication,
            PaginationRequest request
    ) throws SignatureException {
        try {
            int pageNo = 0, pageSize = 10;

            if (request.getPageNo() != null) {
                pageNo = request.getPageNo();
            }

            if (request.getPageSize() != null) {
                pageSize = request.getPageSize();
            }

            if (pageNo > 0) {
                pageNo = pageNo - 1;
            }


            MstUser authenticateUser = UserInternalUtils.authenticateUser(authentication);
            MstUser user = mstUserRepository.findById(authenticateUser.getUserCode()).orElseThrow();

            String financingStatusFilter = null,
                    custNameFilter = null,
                    bouwheerNameFilter = null;

            Optional<MstAppRoleFormUser> findPermission = mstAppRoleFormUserRepository
                    .findTopByUserOrderByAppRoleFormUserId(user);
            MstAppRoleFormUser permission = findPermission
                    .orElseGet(() -> MstAppRoleFormUser.builder().build());
            String roleCode =  permission
                    .getAppRoleForm()
                    .getApplicationRole()
                    .getRoleCode()
                    .getRoleCode();



            if (
                    !StringUtil.isNullOrEmpty(request.getSearchBy())
                            && !StringUtil.isNullOrEmpty(request.getSearchValue())
            ) {
                switch (request.getSearchBy().toLowerCase()) {
                    case "status":
                        financingStatusFilter = request.getSearchValue();
                        break;
                    case "namadebitur":
                        custNameFilter = request.getSearchValue();
                        break;
                    case "pemberikerja":
                        bouwheerNameFilter = request.getSearchValue();
                        break;
                    case "cabang":
                        break;
                }
            }

            Page<FinancingHdr> financingHdrPage = financingHdrRepository.findAllAssignmentFinancingRaw(
                    user.getEmployee().getBranch().getBranchCode(),
                    financingStatusFilter,
                    custNameFilter,
                    bouwheerNameFilter,
                    PageRequest.of(pageNo, pageSize)
            );


            return SpecPagination.paginationData(new SpecPagination<FinancingHdr, AssignmentDto>(financingHdrPage.stream().toList(), request)
            {
                @Override
                public FinancingHdr search(FinancingHdr data) {

                    if (isSearchBy("financingHdrCode") && equal(data.getFinancingHdrCode().toString())  ){
                        return data;
                    }else if (isSearchBy("custName") && like(data.getCustomer().getCustName())  ){
                        return data;
                    }else if (isSearchBy("bouwheerName") && like(data.getBouwheer().getBouwheerName())  ){
                        return data;
                    }

                    return null;
                }

                @Override
                public AssignmentDto eval(FinancingHdr e) {
                    if (e.getCustomer() == null || e.getBouwheer() == null) {
                        return null;
                    }

                    boolean isNewCust = financingHdrRepository
                            .countByCustomerAndFinancingStatus(
                                    e.getCustomer(),
                                    "PAID"
                            ) == 0;


                    MappedFinancingStatus financingStatus;
                    if (roleCode.equalsIgnoreCase("account_officer")){
                        financingStatus = new MappedFinancingStatus(
                                e,
                                MappedFinancingStatus.Type.AccountOfficer
                        );

                    }else{
                        financingStatus = new MappedFinancingStatus(
                                e,
                                MappedFinancingStatus.Type.BranchAdmin
                        );
                        if (financingStatus.getStatus().equalsIgnoreCase("NEW")){
                            return null;
                        }
                    }



                    Agreement agreement = agreementRepository.findTopByFinancingHdr(e).orElse(null);
                    AgreementFile agreementFile = null;

                    String agreementDoc = null, agreementCode = null;
                    if (agreement != null) {
                        agreementCode = agreement.getAgreementCode();
                        agreementFile = agreementFileRepository.findTopByAgreementOrderByAgreementFileId(
                                agreement
                        ).orElse(null);
                    }

                    if (agreementFile != null) {
                        agreementDoc = UriUtils.fileUlr(
                                httpServletRequest,
                                Math.toIntExact(agreementFile.getAgreementFileId()),
                                UriUtils.DocType.agreement
                        );
                    }

                    return AssignmentDto.builder()
                            .financingHdrCode(e.getFinancingHdrCode())
                            .agreementCode(agreementCode)
                            .custCode(e.getCustomer().getCustCode())
                            .custName(e.getCustomer().getCustName())
                            .bouwheerName(e.getBouwheer().getBouwheerName())
                            .verifDate(null)
                            .dueDate(Utils.fromInstant(e.getFinancingDueDate()))
                            .financingAmount(BigDecimal.valueOf(e.getFinancingAmt()))
                            .custStatus(isNewCust ? "New Customer" : "Existing Customer")
                            .status(financingStatus.getStatus())
                            .statusLabel(financingStatus.getLabel())
                            .agreementDoc(agreementDoc)
                            .build();

                }
            });
        } catch (Exception e) {
            log.error("assignmentList: error {}", e.getMessage());
            throw e;
        }
    }

    public List<DebtorDto> signerPersonList(String financingHdrCode) {
        List<Debtor> debtors = debtorRepository.findByFinancingHdrCode(financingHdrCode);

        List<CompletableFuture<DebtorDto>> futures = debtors.stream()
                .map(this::processDebtorAsync)
                .collect(Collectors.toList());

        CompletableFuture<Void> allOf = CompletableFuture.allOf(
                futures.toArray(new CompletableFuture[0])
        );

        try {
            allOf.join();

            return futures.stream()
                    .map(CompletableFuture::join)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            throw new RuntimeException("Error processing debtors", e);
        }
    }

    @Async("taskExecutor")
    public CompletableFuture<DebtorDto> processDebtorAsync(Debtor signer) {
        DebtorDto debtorDto = mapDebtorToDto(signer);
        checkRegistrationStatus(debtorDto, signer.getIdentityNo());
        return CompletableFuture.completedFuture(debtorDto);
    }

    private DebtorDto mapDebtorToDto(Debtor signer) {
        DebtorDto debtorDto = new DebtorDto();
        debtorDto.setDebtorId(signer.getDebtorId());
        debtorDto.setDebtorName(signer.getDebtorName());
        debtorDto.setKaryawanName(signer.getKaryawanName());
        debtorDto.setJabatan(signer.getJabatan());
        debtorDto.setIdentityNo(signer.getIdentityNo());
        debtorDto.setEmail(signer.getEmail());
        debtorDto.setNoTelp(signer.getNoTelp());
        debtorDto.setTempatLahir(signer.getTempatLahir());
        debtorDto.setTanggalLahir(signer.getTanggalLahir());
        debtorDto.setJenisKelamin(signer.getJenisKelamin());
        debtorDto.setAlamat(signer.getAlamat());
        debtorDto.setRt(signer.getRt());
        debtorDto.setRw(signer.getRw());
        debtorDto.setKodePos(signer.getKodePos());
        debtorDto.setKelurahan(signer.getKelurahan());
        debtorDto.setKecamatan(signer.getKecamatan());
        debtorDto.setKota(signer.getKota());
        debtorDto.setIsActive(signer.getIsActive());
        debtorDto.setEmailDebtor(signer.getEmailDebtor());
        debtorDto.setFinancingHdrCode(signer.getFinancingHdrCode());

        return debtorDto;
    }

    private void checkRegistrationStatus(DebtorDto debtorDto, String identityNo) {
        try {
            Map<String, Object> requestBody = new HashMap<>();
            Map<String, String> audit = new HashMap<>();
            audit.put("callerId", callerId);
            requestBody.put("audit", audit);
            requestBody.put("dataType", "NIK");
            requestBody.put("userData", identityNo);

            HttpHeaders headers = new HttpHeaders();
            headers.set("x-api-key", apiKey);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    registerUrl,
                    HttpMethod.POST,
                    entity,
                    Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> responseBody = response.getBody();

                if (responseBody != null && responseBody.containsKey("status")) {
                    Map<String, Object> status = (Map<String, Object>) responseBody.get("status");
                    Integer code = (Integer) status.get("code");

                    if (code != null && code == 0 && responseBody.containsKey("registrationData")) {
                        List<Map<String, Object>> registrationData = (List<Map<String, Object>>) responseBody.get("registrationData");
                        if (registrationData != null && !registrationData.isEmpty()) {
                            // Cari data untuk vendor Vida
                            Map<String, Object> vidaRegistration = registrationData.stream()
                                    .filter(data -> "Vida".equals(data.get("vendor")))
                                    .findFirst()
                                    .orElse(null);

                            if (vidaRegistration != null) {
                                String registrationStatus = vidaRegistration.get("registrationStatus").toString();

                                // Aturan status berdasarkan klarifikasi Anda
                                switch (registrationStatus) {
                                    case "0":
                                        debtorDto.setSignerStatus("not active");
                                        debtorDto.setSignhubStatus("not register");
                                        break;
                                    case "1":
                                        debtorDto.setSignerStatus("not active");
                                        debtorDto.setSignhubStatus("registered");
                                        break;
                                    case "2":
                                        debtorDto.setSignerStatus("active");
                                        debtorDto.setSignhubStatus("registered");
                                        break;
                                    default: // Untuk status tidak dikenal
                                        debtorDto.setSignerStatus("not active");
                                        debtorDto.setSignhubStatus("not register");
                                }
                                return;
                            }
                        }
                    }
                }
            }

            // Default jika tidak memenuhi kondisi di atas
            debtorDto.setSignerStatus("not active");
            debtorDto.setSignhubStatus("not register");

        } catch (Exception e) {
            debtorDto.setSignerStatus("not active");
            debtorDto.setSignhubStatus("not register");
            System.err.println("Error checking registration for NIK: " + identityNo);
            e.printStackTrace();
        }
    }

    public CommonResult<DebtorDto> detailSigner(Long id) {
        Optional<Debtor> personDetail = debtorRepository.findById(id);

        if (personDetail.isPresent()) {
            Debtor debtor = personDetail.get();
            DebtorDto debtorDto = new DebtorDto();
            debtorDto.setDebtorId(debtor.getDebtorId());
            debtorDto.setDebtorName(debtor.getDebtorName());
            debtorDto.setKaryawanName(debtor.getKaryawanName());
            debtorDto.setJabatan(debtor.getJabatan());
            debtorDto.setIdentityNo(debtor.getIdentityNo());
            debtorDto.setEmail(debtor.getEmail());
            debtorDto.setNoTelp(debtor.getNoTelp());
            debtorDto.setTempatLahir(debtor.getTempatLahir());
            debtorDto.setTanggalLahir(debtor.getTanggalLahir());
            debtorDto.setJenisKelamin(debtor.getJenisKelamin());
            debtorDto.setAlamat(debtor.getAlamat());
            debtorDto.setRt(debtor.getRt());
            debtorDto.setRw(debtor.getRw());
            debtorDto.setKodePos(debtor.getKodePos());
            debtorDto.setKelurahan(debtor.getKelurahan());
            debtorDto.setKecamatan(debtor.getKecamatan());
            debtorDto.setKota(debtor.getKota());
            debtorDto.setIsActive(debtor.getIsActive());
            debtorDto.setSignerStatus(debtor.getSignerStatus());
            debtorDto.setSignhubStatus(debtor.getSignhubStatus());
            debtorDto.setEmailDebtor(debtor.getEmailDebtor());
            debtorDto.setFinancingHdrCode(debtor.getFinancingHdrCode());

            return new CommonResult<DebtorDto>().success(debtorDto);
        } else {
            return new CommonResult<DebtorDto>().fail(400,"Signer tidak ditemukan dengan ID: " + id);
        }
    }

    @Transactional
    public DebtorDto createDebtor(DebtorDto debtorDto, Authentication authentication) {
        try {
            log.info("createDebtor: {}", debtorDto);

            String username = authentication != null ?
                    authentication.getName() :
                    "SYSTEM";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-api-key", apiKey);

            log.info("Calling Registration API for debtor with identityNo: {}", debtorDto.getIdentityNo());
            Map<String, Object> registerResponse = callRegistrationApi(debtorDto, headers);
            log.info("Register API Response: {}", registerResponse);

            // Cek status registrasi
            String registrationStatus = "0";

            if (registerResponse.containsKey("registrationData")) {
                List<Map<String, Object>> registrationData = (List<Map<String, Object>>) registerResponse.get("registrationData");
                if (registrationData != null && !registrationData.isEmpty()) {
                    registrationStatus = (String) registrationData.get(0).get("registrationStatus");
                }
            }

            DebtorDto savedDebtor = saveDebtor(debtorDto, username);

            switch (registrationStatus) {
                case "0":
                    // Lanjut ke proses invitation
                    log.info("Calling Invitation API for debtor: {}", debtorDto.getDebtorName());
                    Map<String, Object> inviteResponse = callInvitationApi(debtorDto, headers, username);
                    String invitationLink = (String) inviteResponse.get("link");
                    if (invitationLink == null) {
                        throw new RuntimeException("Gagal generate link undangan");
                    }

                    log.info("Sending invitation email to: {}", debtorDto.getEmailDebtor());
                    emailService.sendInvitationLinkEmail(
                            debtorDto.getEmailDebtor(),
                            invitationLink,
                            debtorDto.getDebtorName()
                    );

                    savedDebtor.setRegistrationMessage("Registrasi berhasil dan undangan telah dikirim");
                    break;
                case "1":
                    savedDebtor.setRegistrationMessage("Akun sudah registrasi, namun belum di aktivasi");
                    break;
                case "2":
                    savedDebtor.setRegistrationMessage("Signer person sudah register dan aktivasi");
                    break;
                default:
                    throw new RuntimeException("Status registrasi tidak dikenali: " + registrationStatus);
            }

            return savedDebtor;

        } catch (Exception e) {
            log.error("Error: {}", e.getMessage());
            throw new RuntimeException("Gagal membuat debtor: " + e.getMessage());
        }
    }

    private Map<String, Object> callRegistrationApi(DebtorDto debtorDto, HttpHeaders headers) {
        Map<String, Object> requestBody = Map.of(
                "audit", Map.of("callerId", callerId),
                "dataType", "NIK",
                "userData", debtorDto.getIdentityNo()
        );

        ResponseEntity<Map> response = restTemplate.postForEntity(
                registerUrl,
                new HttpEntity<>(requestBody, headers),
                Map.class
        );

        Map<String, Object> responseBody = response.getBody();
        log.info("Registration API Response Body: {}", responseBody);
        if (responseBody == null) {
            throw new RuntimeException("API registrasi tidak memberikan response");
        }

        // Handle response format baru dengan registrationData
        if (responseBody.containsKey("registrationData")) {
            return responseBody;
        }

        // Handle response format lama dengan status code 8165
        if (responseBody.containsKey("status")) {
            Map<String, Object> status = (Map<String, Object>) responseBody.get("status");
            if (!status.get("code").equals(8165)) {
                throw new RuntimeException((String) status.get("message"));
            }
        }

        return responseBody;
    }

    private Map<String, Object> callInvitationApi(DebtorDto debtorDto, HttpHeaders headers, String username) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("provinsi", "DKI JAKARTA");
        requestBody.put("kota", debtorDto.getKota());
        requestBody.put("kelurahan", debtorDto.getKelurahan());
        requestBody.put("tmpLahir", debtorDto.getTempatLahir());
        requestBody.put("alamat", debtorDto.getAlamat());
        requestBody.put("tglLahir", debtorDto.getTanggalLahir());
        requestBody.put("nama", debtorDto.getDebtorName());
        requestBody.put("kecamatan", debtorDto.getKecamatan());
        requestBody.put("tlp", debtorDto.getNoTelp());
        requestBody.put("jenisKelamin", debtorDto.getJenisKelamin());
        requestBody.put("idKtp", debtorDto.getIdentityNo());
        requestBody.put("kodePos", debtorDto.getKodePos());
        requestBody.put("email", debtorDto.getEmail());
        requestBody.put("type", "EMPLOYEE");
        requestBody.put("audit", Map.of("callerId", username));

        ResponseEntity<Map> response = restTemplate.postForEntity(
                generateLinkUrl,
                new HttpEntity<>(requestBody, headers),
                Map.class
        );
        log.info("Invitation API Request Body: {}", requestBody);

        Map<String, Object> responseBody = response.getBody();
        log.info("Invitation API Response Body: {}", responseBody);
        if (responseBody == null) {
            throw new RuntimeException("API undangan tidak memberikan response");
        }

        if (responseBody.containsKey("status")) {
            Map<String, Object> status = (Map<String, Object>) responseBody.get("status");
            if (!status.get("code").equals(0)) {
                throw new RuntimeException((String) status.get("message"));
            }
        }

        return responseBody;
    }

    private DebtorDto saveDebtor(DebtorDto debtorDto, String username) {
        log.info("Saving debtor to database: {}", debtorDto);
        Debtor debtor = Debtor.builder()
                .debtorName(debtorDto.getDebtorName())
                .karyawanName(debtorDto.getKaryawanName())
                .jabatan(debtorDto.getJabatan())
                .identityNo(debtorDto.getIdentityNo())
                .email(debtorDto.getEmail())
                .noTelp(debtorDto.getNoTelp())
                .tempatLahir(debtorDto.getTempatLahir())
                .tanggalLahir(debtorDto.getTanggalLahir())
                .jenisKelamin(debtorDto.getJenisKelamin())
                .alamat(debtorDto.getAlamat())
                .rt(debtorDto.getRt())
                .rw(debtorDto.getRw())
                .kodePos(debtorDto.getKodePos())
                .kelurahan(debtorDto.getKelurahan())
                .kecamatan(debtorDto.getKecamatan())
                .kota(debtorDto.getKota())
                .isActive(debtorDto.getIsActive())
                .signerStatus("not active")
                .signhubStatus("not register")
                .emailDebtor(debtorDto.getEmailDebtor())
                .financingHdrCode(debtorDto.getFinancingHdrCode())
                .usrCrt(username)
                .dtmCrt(LocalDateTime.now())
                .build();

        Debtor savedDebtor = debtorRepository.save(debtor);

        // Simpan ke notif_debtor
        notifDebtorRepository.save(NotifDebtor.builder()
                .notification("Terdapat Perubahan Signer Person")
                .description("Signer Person telah berubah. Pastikan signer yang didaftarkan sesuai dan berwenang menandatangani dokumen perjanjian.")
                .financingHdrCode(debtorDto.getFinancingHdrCode())
                .usrCrt(username)
                .dtmCrt(LocalDateTime.now())
                .build());

        return debtorMapper.entityToDto(savedDebtor);
    }

    // GET DARI CONFINS

    public PersonDto getSignersFromExternalApi(String financingHdrCode) {
        boolean useHardcode = false; // Ganti nilai ini untuk switch mode

        try {
            String custNo;
            String cwrNo;

            if (useHardcode) {
                custNo = "41000001137";
                cwrNo = "41350CWR2024454";
                log.info("Menggunakan data hardcode - custNo: {}, cwrNo: {}", custNo, cwrNo);
            } else {
                UUID uuid = UUID.fromString(financingHdrCode);
                Agreement agreement = agreementRepository.findByFinancingHdr_FinancingHdrCode2(uuid)
                        .orElseThrow(() -> new RuntimeException("Agreement not found"));

                custNo = agreement.getCwr().getCustomer().getCustNo();
                cwrNo = agreement.getCwr().getCwrCode();
                log.info("Menggunakan data database - custNo: {}, cwrNo: {}", custNo, cwrNo);
            }

            SignerRequestDto request = new SignerRequestDto(custNo, cwrNo, LocalDate.now().toString());
            ExternalApiResponse response = callExternalApi(request);
            return mapToPersonDto(response);

        } catch (Exception e) {
            PersonDto error = new PersonDto();
            error.setStatusCode("500");
            error.setMessage("Error: " + e.getMessage());
            return error;
        }
    }

    // HARDCODE RESPONSE

//    public PersonDto getSignersFromExternalApi(String financingHdrCode) {
//        boolean useHardcode = true;
//
//        try {
//            String custNo;
//            String cwrNo;
//
//            if (useHardcode) {
//                custNo = "41000001137";
//                cwrNo = "41350CWR2024454";
//                log.info("Menggunakan data hardcode - custNo: {}, cwrNo: {}", custNo, cwrNo);
//
//                PersonDto hardcodedResult = new PersonDto();
//                hardcodedResult.setStatusCode("200");
//                hardcodedResult.setMessage("Success");
//
//                List<PersonDto.Signer> hardcodedSigners = new ArrayList<>();
//
//                PersonDto.Signer signer1 = new PersonDto.Signer();
//                signer1.setCwrSignerId(1737);
//                signer1.setCwrCustId(4848);
//                signer1.setSignerType("MFSIGNER");
//                signer1.setSignerName("ANDIKA PRASETYO JUDIANTO");
//                signer1.setSignerPosition("CREDIT MANAGEMENT GENERAL MANAGER");
//                hardcodedSigners.add(signer1);
//
//                PersonDto.Signer signer2 = new PersonDto.Signer();
//                signer2.setCwrSignerId(1738);
//                signer2.setCwrCustId(4848);
//                signer2.setSignerType("SHAREHOLDER");
//                signer2.setSignerName("NURWA*****");
//                signer2.setSignerPosition("DIREKTUR");
//                hardcodedSigners.add(signer2);
//
//                hardcodedResult.setSigners(hardcodedSigners);
//                return hardcodedResult;
//
//            } else {
//                UUID uuid = UUID.fromString(financingHdrCode);
//                Agreement agreement = agreementRepository.findByFinancingHdr_FinancingHdrCode2(uuid)
//                        .orElseThrow(() -> new RuntimeException("Agreement not found"));
//
//                custNo = agreement.getCwr().getCustomer().getCustNo();
//                cwrNo = agreement.getCwr().getCwrCode();
//                log.info("Menggunakan data database - custNo: {}, cwrNo: {}", custNo, cwrNo);
//
//                SignerRequestDto request = new SignerRequestDto(custNo, cwrNo, LocalDate.now().toString());
//                ExternalApiResponse response = callExternalApi(request);
//                return mapToPersonDto(response);
//            }
//
//        } catch (Exception e) {
//            PersonDto error = new PersonDto();
//            error.setStatusCode("200");
//            error.setMessage("Success");
//            List<PersonDto.Signer> hardcodedSigners = new ArrayList<>();
//
//            PersonDto.Signer signer1 = new PersonDto.Signer();
//            signer1.setCwrSignerId(1737);
//            signer1.setCwrCustId(4848);
//            signer1.setSignerType("MFSIGNER");
//            signer1.setSignerName("ANDIKA PRASETYO JUDIANTO");
//            signer1.setSignerPosition("CREDIT MANAGEMENT GENERAL MANAGER");
//            hardcodedSigners.add(signer1);
//
//            PersonDto.Signer signer2 = new PersonDto.Signer();
//            signer2.setCwrSignerId(1738);
//            signer2.setCwrCustId(4848);
//            signer2.setSignerType("SHAREHOLDER");
//            signer2.setSignerName("NURWA*****");
//            signer2.setSignerPosition("DIREKTUR");
//            hardcodedSigners.add(signer2);
//
//            error.setSigners(hardcodedSigners);
//            return error;
//        }
//    }

    private ExternalApiResponse callExternalApi(SignerRequestDto request) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("AdInsKey", adInsKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<SignerRequestDto> entity = new HttpEntity<>(request, headers);

        ResponseEntity<ExternalApiResponse> response = restTemplate.exchange(
                confinsUrl,
                HttpMethod.POST,
                entity,
                ExternalApiResponse.class);

        return response.getBody();
    }

    private PersonDto mapToPersonDto(ExternalApiResponse externalResponse) {
        PersonDto result = new PersonDto();

        if (externalResponse == null) {
            result.setStatusCode("500");
            result.setMessage("No response from external API");
            result.setSigners(List.of());
            return result;
        }

        result.setStatusCode(externalResponse.getStatusCode());
        result.setMessage(externalResponse.getMessage());

        if (externalResponse.getReturnObject() == null || externalResponse.getReturnObject().isEmpty()) {
            result.setMessage("Tidak ada data signer yang tersedia");
            result.setSigners(List.of());
            return result;
        }

        result.setSigners(
                externalResponse.getReturnObject().stream()
                        .map(extSigner -> {
                            PersonDto.Signer signer = new PersonDto.Signer();
                            signer.setCwrSignerId(extSigner.getCwrSignerId());
                            signer.setCwrCustId(extSigner.getCwrCustId());
                            signer.setSignerType(extSigner.getSignerType());
                            signer.setSignerName(extSigner.getSignerName());
                            signer.setSignerPosition(extSigner.getSignerPosition());
                            return signer;
                        })
                        .toList()
        );
        return result;
    }

    // use fileStorageService
    public CommonResult<AgreementFileSigning> uploadFileHandler(FileUploadRequest request, Authentication authentication) {
        try {
            if (request.getFile() == null || request.getFile().isEmpty()) {
                return new CommonResult<AgreementFileSigning>().fail(400, "File harus diupload");
            }
            if (request.getAgreementCode() == null || request.getAgreementCode().isEmpty()) {
                return new  CommonResult<AgreementFileSigning>().fail(400, "Agreement tidak boleh kosong");
            }
            if (request.getFileName() == null || request.getFileName().isEmpty()) {
                return new  CommonResult<AgreementFileSigning>().fail(400, "Nama Document tidak boleh kosong");
            }
            if (request.getFileTypeCode() == null || request.getFileTypeCode().isEmpty()) {
                return new  CommonResult<AgreementFileSigning>().fail(400, "No Document tidak boleh kosong");
            }

            String uploadDir = "agreements/" + request.getAgreementCode();
            String uploadName = "file_" + System.currentTimeMillis() + "_" + request.getFile().getOriginalFilename();

            String filePath = fileStorageService.save(
                    request.getFile(),
                    uploadDir,
                    uploadName,
                    null
            );

            String username = authentication != null ?
                    authentication.getName() :
                    "SYSTEM";

            AgreementFileSigning entity = new AgreementFileSigning();
            entity.setDocumentId("");
            entity.setAgreementCode(request.getAgreementCode());
            entity.setFileTypeCode(request.getFileTypeCode());
            entity.setFileName(request.getFileName());
            entity.setFilePath(filePath);
            entity.setStamp(false);
            entity.setUsrCrt(username);
            entity.setDtmCrt(LocalDateTime.now());
            entity.setSigner("");
            entity.setDocumentId("");


            AgreementFileSigning savedFile = agreementFileSigningRepository.save(entity);

            notifDebtorRepository.save(NotifDebtor.builder()
                    .financingHdrCode(agreementRepository.findByAgreementCode(request.getAgreementCode())
                            .map(agreement -> agreement.getFinancingHdr().getFinancingHdrCode().toString())
                            .orElseThrow(() -> new RuntimeException("Agreement dengan code " + request.getAgreementCode() + " tidak ditemukan")))
                    .notification("Permintaan Tanda Tangan Dokumen")
                    .description("Dokumen yang memerlukan tanda tangan "+ getDebtorData(request.getAgreementCode()).getKaryawanName() +", telah tersedia. Mohon segera menandatangani dokumen tersebut atau menghubungi pihak terkait.")
                    .usrCrt(username)
                    .dtmCrt(LocalDateTime.now())
                    .build());

            callExternalSigningAPI(request, username);

            return new  CommonResult<AgreementFileSigning>().success(savedFile);

        } catch (Exception e) {
            return new  CommonResult<AgreementFileSigning>().fail(500, "Error: " + e.getMessage());
        }
    }

    private void callExternalSigningAPI(FileUploadRequest request, String username) {
        try {
            Debtor debtor = getDebtorData(request.getAgreementCode());
            String branchCode = getBranchCodeFromAgreement(request.getAgreementCode());
            String base64File = convertFileToBase64(request.getFile());

            // 1. Prepare original request body
            Map<String, Object> requestBody = new LinkedHashMap<>();
            requestBody.put("tenantCode", "CSUL_DEV");
            requestBody.put("psreCode", "VIDA");

            Map<String, String> audit = new LinkedHashMap<>();
            audit.put("callerId", username);
            requestBody.put("audit", audit);

            List<Map<String, Object>> requests = new ArrayList<>();
            Map<String, Object> documentRequest = new LinkedHashMap<>();
            documentRequest.put("referenceNo", request.getAgreementCode());
            documentRequest.put("documentTemplateCode", "PERJANJIAN_1A");
            documentRequest.put("documentName", request.getFileName());
            documentRequest.put("officeCode", branchCode);
            documentRequest.put("officeName", "JAKARTA");
            documentRequest.put("regionCode", branchCode);
            documentRequest.put("regionName", username);
            documentRequest.put("businessLineCode", "CBU");
            documentRequest.put("businessLineName", "Corporate Business Unit");

            List<Map<String, String>> signers = new ArrayList<>();
            Map<String, String> signer = new LinkedHashMap<>();
            signer.put("signAction", "mt");
            signer.put("signerType", "CUST");
            signer.put("idKtp", debtor.getIdentityNo());
            signer.put("tlp", debtor.getNoTelp());
            signer.put("email", debtor.getEmail());
            signer.put("seqNo", "0");
            signers.add(signer);

            documentRequest.put("documentFile", base64File);
            documentRequest.put("signers", signers);
            documentRequest.put("isSequence", "");
            documentRequest.put("useSignQR", "");
            requests.add(documentRequest);
            requestBody.put("requests", requests);

            // 2. Create sanitized version for logging
            Map<String, Object> logRequestBody = new LinkedHashMap<>(requestBody);
            List<Map<String, Object>> logRequests = new ArrayList<>();

            Map<String, Object> logDocumentRequest = new LinkedHashMap<>(documentRequest);
            logDocumentRequest.put("documentFile", "[BASE64_PDF_REDACTED]"); // Redact the base64

            logRequests.add(logDocumentRequest);
            logRequestBody.put("requests", logRequests);

            // 3. Log the sanitized version
            ObjectMapper mapper = new ObjectMapper();
            String requestBodyLog = mapper.writeValueAsString(logRequestBody);
            log.info("Request Body to e-sign API (documentFile redacted): {}", requestBodyLog);

            // 4. Proceed with original request
            HttpHeaders headers = new HttpHeaders();
            headers.set("x-api-key", apiKey);
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Use original requestBody (with actual base64) for API call
            HttpEntity<String> entity = new HttpEntity<>(mapper.writeValueAsString(requestBody), headers);
            RestTemplate restTemplate = new RestTemplate();

            String apiUrl = sendDoc;
            ResponseEntity<Map> response = restTemplate.postForEntity(apiUrl, entity, Map.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("Gagal memanggil API e-sign. Status: " + response.getStatusCodeValue());
            }

            Map<String, Object> responseBody = response.getBody();
            if (responseBody == null || !responseBody.containsKey("status")) {
                throw new RuntimeException("Invalid response from e-sign API");
            }

            Map<String, Object> status = (Map<String, Object>) responseBody.get("status");
            int code = (int) status.get("code");
            if (code != 0) {
                String errorMsg = (String) status.getOrDefault("message", "Unknown error");
                throw new RuntimeException("Gagal memproses e-sign. Kode: " + code + ", Pesan: " + errorMsg);
            }
        } catch (Exception e) {
            log.error("Error calling e-sign API: {}", e.getMessage(), e);
            throw new RuntimeException("Gagal memanggil API e-sign: " + e.getMessage());
        }
    }

    private String convertFileToBase64(MultipartFile file) throws IOException {
        byte[] fileBytes = file.getBytes();
        return Base64.getEncoder().encodeToString(fileBytes);
    }

    private String getBranchCodeFromAgreement(String agreementCode) {
        // Langsung query branch_code dari agreement -> financing_hdr -> cwr
        return agreementRepository.findByAgreementCode(agreementCode)
                .map(agreement -> {
                    if (agreement.getFinancingHdr() == null) {
                        throw new RuntimeException("FinancingHdr tidak ditemukan untuk agreement " + agreementCode);
                    }
                    String cwrCode = agreement.getCwr().getCwrCode();

                    // Query branch_code dari cwr
                    return cwrRepository.findByCwrCode(cwrCode)
                            .map(cwr -> {
                                if (cwr.getBranchCode() == null || cwr.getBranchCode().isEmpty()) {
                                    throw new RuntimeException("Branch code kosong untuk cwr " + cwrCode);
                                }
                                return cwr.getBranchCode();
                            })
                            .orElseThrow(() -> new RuntimeException("Cwr dengan code " + cwrCode + " tidak ditemukan"));
                })
                .orElseThrow(() -> new RuntimeException("Agreement dengan code " + agreementCode + " tidak ditemukan"));
    }

    private Debtor getDebtorData(String agreementCode) {
        String financingHdrCode = String.valueOf(agreementRepository.findByAgreementCode(agreementCode)
                .map(agreement -> {
                    if (agreement.getFinancingHdr() == null) {
                        throw new RuntimeException("FinancingHdr tidak ditemukan untuk agreement " + agreementCode);
                    }
                    return agreement.getFinancingHdr().getFinancingHdrCode();
                })
                .orElseThrow(() -> new RuntimeException("Agreement dengan code " + agreementCode + " tidak ditemukan")));

        Debtor debtor = debtorRepository.findTopByFinancingHdrCodeOrderByDtmCrtDesc(financingHdrCode)
                .orElseThrow(() -> new RuntimeException("Data debtors tidak ditemukan untuk financingHdrCode: " + financingHdrCode));


        if (debtor.getIdentityNo() == null || debtor.getIdentityNo().isEmpty()) {
            throw new RuntimeException("NIK kosong untuk debtors dengan financingHdrCode: " + financingHdrCode);
        }
        if (debtor.getNoTelp() == null || debtor.getNoTelp().isEmpty()) {
            throw new RuntimeException("Nomor telepon kosong untuk debtors dengan financingHdrCode: " + financingHdrCode);
        }
        if (debtor.getEmail() == null || debtor.getEmail().isEmpty()) {
            throw new RuntimeException("Email kosong untuk debtors dengan financingHdrCode: " + financingHdrCode);
        }

        return debtor;
    }

    public List<SignerAgreementDto> signerAgreement(String financingHdrCode) {
        try {
            UUID uuid = UUID.fromString(financingHdrCode);

            List<Agreement> agreements = agreementRepository.findByFinancingHdr_FinancingHdrCode(uuid);

            if (agreements.isEmpty()) {
                return Collections.singletonList(
                        new SignerAgreementDto(
                                "NOT_FOUND",
                                "FinancingHdrCode " + financingHdrCode + " tidak memiliki agreement"
                        )
                );
            }

            return agreements.stream()
                    .map(agreement -> new SignerAgreementDto(
                            agreement.getAgreementCode(),
                            agreement.getFinancingHdr().getFinancingHdrCode().toString()
                    ))
                    .toList();

        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Format UUID tidak valid");
        }
    }

    public List<SignerDocDto> signerDocList(String financingHdrCode) {
        try {
            UUID financingHdrUuid = UUID.fromString(financingHdrCode);

            List<Agreement> agreements = agreementRepository.findByFinancingHdr_FinancingHdrCode(financingHdrUuid);

            if (agreements.isEmpty()) {
                return Collections.emptyList();
            }

            FinancingHdr financingHdr = financingHdrRepository.findByFinancingHdrCode(financingHdrUuid)
                    .orElseThrow(() -> new EntityNotFoundException("Financing header not found"));

            String bowheerName = financingHdr.getBouwheer().getBouwheerName();

            List<String> agreementCodes = agreements.stream()
                    .map(Agreement::getAgreementCode)
                    .collect(Collectors.toList());

            List<AgreementFileSigning> fileSignings = agreementFileSigningRepository.findByAgreementCodes(agreementCodes);

            return fileSignings.stream()
                    .map(signing -> SignerDocDto.builder()
                            .agreementFileId(signing.getAgreementFileId())
                            .agreementCode(signing.getAgreementCode())
                            .bowheerName(bowheerName)
                            .verifDate(signing.getDtmCrt() != null ?
                                    signing.getDtmCrt().toString() : null)
                            .status("Menunggu TTD") // di-hardcode atau dari entity?
                            .agreementDoc(signing.getFilePath())
                            .build())
                    .collect(Collectors.toList());

        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid financingHdrCode format: " + financingHdrCode);
        }
    }



    public SignerCheckResultDto compareSigners(String financingHdrCode) {

        List<String> dbSigners = getSignersFromDatabase(financingHdrCode);

        List<String> externalApiSigners = getSignersFromExternalApi2(financingHdrCode);

        SignerCheckResultDto result = createComparisonResult(dbSigners, externalApiSigners);

        return result;
    }

    private List<String> getSignersFromDatabase(String financingHdrCode) {
        try {
            List<Debtor> debtors = debtorRepository.findByFinancingHdrCode(financingHdrCode);

            return debtors.stream()
                    .map(Debtor::getKaryawanName)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            throw new RuntimeException("Failed to get signers from database: " + e.getMessage());
        }
    }

    private List<String> getSignersFromExternalApi2(String financingHdrCode) {
        try {
            UUID uuid = UUID.fromString(financingHdrCode);
            Agreement agreement = agreementRepository.findByFinancingHdr_FinancingHdrCode2(uuid)
                    .orElseThrow(() -> new RuntimeException("Agreement not found"));

            String custNo = agreement.getCwr().getCustomer().getCustNo();
            String cwrNo = agreement.getCwr().getCwrCode();

            //hardcode
//            String custNo = "41000001137";
//            String cwrNo = "41350CWR2024454";

            SignerRequestDto request = new SignerRequestDto(custNo, cwrNo, LocalDate.now().toString());
            ExternalApiResponse response = callExternalApi(request);

            return response.getReturnObject().stream()
                    .map(signer -> signer.getSignerName())
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            throw new RuntimeException("Failed to get signers from external API: " + e.getMessage());
        }
    }

    private SignerCheckResultDto createComparisonResult(List<String> dbSigners, List<String> externalApiSigners) {
        SignerCheckResultDto result = new SignerCheckResultDto();
        result.setConfinsSigners(externalApiSigners);

        List<String> matched = new ArrayList<>();
        List<String> unmatched = new ArrayList<>();

        for (String signer : externalApiSigners) {
            if (dbSigners.contains(signer)) {
                matched.add(signer);
            } else {
                unmatched.add(signer);
            }
        }

        result.setDBSigners(matched);
        result.setUnmatchedSigners(unmatched);

        return result;
    }

    public ResponseEntity<ApiResponse<?>> downloadDocument(String agreementCode, Authentication authentication) {
        try {
            String username = authentication != null ? authentication.getName() : "SYSTEM";

            // 1. Validasi agreementCode
            if (agreementCode == null || agreementCode.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse<>(false, "Agreement code is required", null, null, null));
            }

            // 2. Get documentId from database
            String documentId = agreementFileSigningRepository.findDocumentIdByAgreementCode(agreementCode);

            if (documentId == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse<>(false, "Document not found in database", null, null, null));
            }

            // 3. Prepare external API request
            HttpHeaders requestHeaders = new HttpHeaders();
            requestHeaders.set("X-api-Key", apiKey);
            requestHeaders.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> requestBody = new LinkedHashMap<>();
            Map<String, String> audit = new LinkedHashMap<>();
            audit.put("callerId", username);
            requestBody.put("audit", audit);
            requestBody.put("documentId", documentId);

            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, requestHeaders);

            // 4. Call external API
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<ExternalDownloadResponse> response;

            try {
                response = restTemplate.exchange(
                        downloadDoc,
                        HttpMethod.POST,
                        requestEntity,
                        ExternalDownloadResponse.class);
            } catch (HttpClientErrorException | HttpServerErrorException e) {
                log.error("External API error: {}", e.getResponseBodyAsString());
                return ResponseEntity.status(e.getStatusCode())
                        .body(new ApiResponse<>(false, "External API error", null, null, e.getResponseBodyAsString()));
            }

            // 5. Handle response
            ExternalDownloadResponse responseBody = response.getBody();
            if (responseBody == null) {
                return ResponseEntity.internalServerError()
                        .body(new ApiResponse<>(false, "Empty response from external API", null, null, null));
            }

            int externalStatusCode = responseBody.getStatus().getCode();
            String externalMessage = responseBody.getStatus().getMessage();

            // Handle semua case dalam format JSON
            if (externalStatusCode == 0) { // Success
                try {
                    byte[] pdfBytes = Base64.getDecoder().decode(responseBody.getPdfBase64());
                    String base64Pdf = responseBody.getPdfBase64(); // Return as base64 string

                    return ResponseEntity.ok()
                            .body(new ApiResponse<>(true, "Document retrieved successfully",
                                    Map.of(
                                            "filename", "document_" + agreementCode + ".pdf",
                                            "content", base64Pdf,
                                            "length", pdfBytes.length
                                    ),
                                    externalStatusCode,
                                    externalMessage));
                } catch (IllegalArgumentException e) {
                    log.error("Invalid Base64 content", e);
                    return ResponseEntity.internalServerError()
                            .body(new ApiResponse<>(false, "Invalid document format", null, null, null));
                }
            } else {
                // Untuk semua error case
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ApiResponse<>(false, "Document processing failed",
                                null,
                                externalStatusCode,
                                externalMessage));
            }

        } catch (Exception e) {
            log.error("Unexpected error", e);
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse<>(false, "Internal server error: " + e.getMessage(), null, null, null));
        }
    }
}
