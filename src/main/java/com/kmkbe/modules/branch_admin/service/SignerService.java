package com.kmkbe.modules.branch_admin.service;

import com.kmkbe.core.domain.dto.*;
import com.kmkbe.core.domain.entity.*;
import com.kmkbe.core.domain.mapper.DebtorMapper;
import com.kmkbe.core.domain.model.CommonResult;
import com.kmkbe.core.domain.model.MappedFinancingStatus;
import com.kmkbe.core.domain.model.PaginationResult;
import com.kmkbe.core.domain.repository.*;
import com.kmkbe.core.domain.request.PaginationRequest;
import com.kmkbe.core.service.BaseRemoteService;
import com.kmkbe.core.utils.UriUtils;
import com.kmkbe.modules.common.service.EmailService;
import com.kmkbe.modules.user.entity.MstAppRoleFormUser;
import com.kmkbe.modules.user.entity.MstUser;
import com.kmkbe.modules.user.repository.MstAppRoleFormUserRepository;
import com.kmkbe.modules.user.repository.MstUserRepository;
import com.kmkbe.modules.user.utils.UserInternalUtils;
import com.kmkbe.nikita.utils.SpecPagination;
import com.kmkbe.nikita.utils.Utils;
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
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.security.SignatureException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SignerService {
    private final FinancingHdrRepository financingHdrRepository;
    private final RestTemplate restTemplate;
    private final DebtorRepository debtorRepository;
    private final DebtorMapper debtorMapper = DebtorMapper.INSTANCE;
    private final EmailService emailService;
    private final AgreementRepository agreementRepository;
    private final AgreementFileSigningRepository agreementFileSigningRepository;
    private final AssignmentSubmissionService assignmentSubmissionService;
    private final NotifDebtorRepository notifDebtorRepository;

    private final String apiKey = "YiByHB@CSUL_DEV";
    private final String registerUrl = "https://gdkwebserver.ad-ins.com/adimobile/demo/esign/services/external/user/checkRegistration";
    private final String generateLinkUrl = "https://gdkwebserver.ad-ins.com/adimobile/demo/esign/services/external/user/generateInvLink";
    private final String downloadDoc = "https://gdkwebserver.ad-ins.com/adimobile/demo/esign/services/external/document/downloadDocument";
    private final String checkDoc = "https://gdkwebserver.ad-ins.com/adimobile/demo/esign/services/external/document/checkStatusSigning";
    private final BaseRemoteService baseRemoteService;
    @Value("${csul.confins.adinskey}")
    private String adinsKey;


    @Value("${csul.confins.adinskey}")
    private String adInsKey;

    public PaginationResult<AssignmentDto> assignmentListGroupByCustomer(
            HttpServletRequest httpServletRequest,
            Authentication authentication,
            PaginationRequest request
    ) throws SignatureException {
        PaginationResult<AssignmentDto> originalResult =
                assignmentSubmissionService.assignmentList(httpServletRequest, authentication, request);

        Map<UUID, AssignmentDto> grouped = originalResult.getList().stream()
                .collect(Collectors.toMap(
                        AssignmentDto::getCustCode,
                        dto -> dto,
                        (existing, replacement) -> existing
                ));

        return PaginationResult.<AssignmentDto>builder()
                .currentPage(originalResult.getCurrentPage())
                .totalPage(1)
                .totalData((long) grouped.size())
                .list(new ArrayList<>(grouped.values()))
                .build();
    }

    public List<DebtorDto> signerPersonList(String financingHdrCode, Authentication authentication) {
        String username = authentication != null ?
                authentication.getName() :
                "SYSTEM";

        String debtorName = financingHdrRepository.findDebtorNameByFinancingHdrCode(UUID.fromString(financingHdrCode));

        List<Debtor> debtors = debtorRepository.findByDebtorName(debtorName);

        List<CompletableFuture<DebtorDto>> futures = debtors.stream()
                .map(debtor -> processDebtorAsync(debtor, debtor.getFinancingHdrCode(), username))
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
    public CompletableFuture<DebtorDto> processDebtorAsync(Debtor signer, String financingHdrCode, String username) {
        DebtorDto debtorDto = mapDebtorToDto(signer);
        checkRegistrationStatus(debtorDto, signer.getIdentityNo(), financingHdrCode, username);
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

    @Transactional
    protected void checkRegistrationStatus(DebtorDto debtorDto, String identityNo, String financingHdrCode, String username) {
        try {

            debtorDto.setSignhubStatus("not register");
            Map<String, Object> requestBody = new HashMap<>();
            Map<String, String> audit = new HashMap<>();
            audit.put("callerId", username);
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

            String finalSignhubStatus = "not register";

            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> responseBody = response.getBody();

                if (responseBody != null && responseBody.containsKey("status")) {
                    Map<String, Object> status = (Map<String, Object>) responseBody.get("status");
                    Integer code = (Integer) status.get("code");

                    if (code != null && code == 0 && responseBody.containsKey("registrationData")) {
                        List<Map<String, Object>> registrationData = (List<Map<String, Object>>) responseBody.get("registrationData");
                        if (registrationData != null && !registrationData.isEmpty()) {
                            Map<String, Object> vidaRegistration = registrationData.stream()
                                    .filter(data -> "Vida".equals(data.get("vendor")))
                                    .findFirst()
                                    .orElse(null);

                            if (vidaRegistration != null) {
                                String registrationStatus = vidaRegistration.get("registrationStatus").toString();
                                switch (registrationStatus) {
                                    case "0":
                                        finalSignhubStatus = "not register";
                                        break;
                                    case "1":
                                        finalSignhubStatus = "pending";
                                        break;
                                    case "2":
                                        finalSignhubStatus = "active";
                                        break;
                                    default:
                                        finalSignhubStatus = "not register";
                                }
                            }
                        }
                    }
                }
            }

            debtorDto.setSignhubStatus(finalSignhubStatus);

            checkSignerStatus(debtorDto, financingHdrCode);

            Debtor debtor = debtorRepository.findById(debtorDto.getDebtorId())
                    .orElseThrow(() -> new RuntimeException("Debtor not found with id " + debtorDto.getDebtorId()));

            debtor.setSignhubStatus(finalSignhubStatus);
            debtor.setSignerStatus(debtorDto.getSignerStatus());

            debtorRepository.save(debtor);

        } catch (Exception e) {
            debtorDto.setSignerStatus("not active");
            debtorDto.setSignhubStatus("not register");

            Debtor debtor = debtorRepository.findById(debtorDto.getDebtorId())
                    .orElse(null);
            if (debtor != null) {
                debtor.setSignerStatus("not active");
                debtor.setSignhubStatus("not register");
                debtorRepository.save(debtor);
            }

            System.err.println("Error checking registration for NIK: " + identityNo);
            e.printStackTrace();
        }
    }

    private void checkSignerStatus(DebtorDto debtorDto, String financingHdrCode) {
        try {

            Agreement agreement = agreementRepository.findCwr(UUID.fromString(financingHdrCode))
                    .orElseThrow(() -> new RuntimeException("Agreement not found"));

            Map<String, String> signerRequestBody = new HashMap<>();
            signerRequestBody.put("custNo", agreement.getCwr().getCustomer().getCustNo());
            signerRequestBody.put("cwrNo", agreement.getCwr().getCwrCode());
            signerRequestBody.put("RequestDateTime", LocalDate.now().toString());

            log.info("ini custNo: " + agreement.getCwr().getCustomer().getCustNo());
            log.info("ini cwrNo: " + agreement.getCwr().getCwrCode());

            HttpHeaders headers = new HttpHeaders();
            headers.set("AdInsKey", adinsKey);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, String>> signerEntity = new HttpEntity<>(signerRequestBody, headers);

            ResponseEntity<Map> signerResponse = restTemplate.exchange(
                    "http://172.21.10.149:8083/mou_getsigner.php",
                    HttpMethod.POST,
                    signerEntity,
                    Map.class
            );

            if (signerResponse.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> signerResponseBody = signerResponse.getBody();
                if (signerResponseBody != null && signerResponseBody.containsKey("ReturnObject")) {
                    List<Map<String, Object>> returnObject = (List<Map<String, Object>>) signerResponseBody.get("ReturnObject");

                    boolean isSignerFound = returnObject.stream()
                            .anyMatch(signer -> debtorDto.getKaryawanName().equalsIgnoreCase(signer.get("SignerName").toString()));

                    log.info("ini isi debtorDto.getKaryawanName() : " + debtorDto.getKaryawanName());
                    debtorDto.setSignerStatus(isSignerFound ? "active" : "not active");
                } else {
                    debtorDto.setSignerStatus("not active");
                }
            } else {
                debtorDto.setSignerStatus("not active");
            }
        } catch (Exception e) {
            debtorDto.setSignerStatus("not active");
            System.err.println("Error checking signer status for: " + debtorDto.getKaryawanName());
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

            if (debtorRepository.existsByIdentityNo(debtorDto.getIdentityNo())) {
                throw new RuntimeException("NIK yang digunakan sudah terdaftar");
            }

            String username = authentication != null ?
                    authentication.getName() :
                    "SYSTEM";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-api-key", apiKey);

            log.info("Calling Registration API for debtor with identityNo: {}", debtorDto.getIdentityNo());
            Map<String, Object> registerResponse = callRegistrationApi(debtorDto, headers, username);
            log.info("Register API Response: {}", registerResponse);

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
                    log.info("Calling Invitation API for debtor: {}", debtorDto.getDebtorName());
                    Map<String, Object> inviteResponse = callInvitationApi(debtorDto, headers, username);
                    String invitationLink = (String) inviteResponse.get("link");
                    if (invitationLink == null) {
                        throw new RuntimeException("Gagal generate link undangan");
                    }

                    log.info("Sending invitation email to: {}", debtorDto.getEmail());
                    emailService.sendInvitationLinkEmail(
                            debtorDto.getEmail(),
                            invitationLink,
                            debtorDto.getKaryawanName()
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
            throw new RuntimeException(e.getMessage());
        }
    }

    private Map<String, Object> callRegistrationApi(DebtorDto debtorDto, HttpHeaders headers, String username) {
        Map<String, Object> requestBody = Map.of(
                "audit", Map.of("callerId", username),
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

        if (responseBody.containsKey("registrationData")) {
            return responseBody;
        }

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
        requestBody.put("nama", debtorDto.getKaryawanName());
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
//        log.info("Saving debtor to database: {}", debtorDto);
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

        String custCode = String.valueOf(financingHdrRepository.findByFinancingHdrCode(UUID.fromString(debtorDto.getFinancingHdrCode()))
                .map(finHdr -> finHdr.getCustomer().getCustCode())
                .orElseThrow(() -> new RuntimeException("FinancingHdr dengan code "
                        + debtorDto.getFinancingHdrCode() + " tidak ditemukan")));

        notifDebtorRepository.save(NotifDebtor.builder()
                .notification("Terdapat Perubahan Signer Person")
                .description("Signer Person telah berubah. Pastikan signer yang didaftarkan sesuai dan berwenang menandatangani dokumen perjanjian.")
                .financingHdrCode(debtorDto.getFinancingHdrCode())
                .custCode(custCode)
                .usrCrt(username)
                .dtmCrt(LocalDateTime.now())
                .build());

        return debtorMapper.entityToDto(savedDebtor);
    }


    public PersonDto getSignersFromExternalApi(String financingHdrCode, String agreementNo) {
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
                Agreement agreement = agreementRepository.findByFinancingHdr_FinancingHdrCode2(uuid, agreementNo)
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

    private ExternalApiResponse callExternalApi(SignerRequestDto request) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("AdInsKey", adInsKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<SignerRequestDto> entity = new HttpEntity<>(request, headers);

        ResponseEntity<ExternalApiResponse> response = restTemplate.exchange(
                baseRemoteService.Mou_GetSigner_forward(),
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

    public SignerCheckResultDto compareSigners(String financingHdrCode, String agreementNo) {

        List<String> dbSigners = getSignersFromDatabase(financingHdrCode);

        List<String> externalApiSigners = getSignersFromExternalApi2(financingHdrCode, agreementNo);

        SignerCheckResultDto result = createComparisonResult(dbSigners, externalApiSigners);

        return result;
    }

    private List<String> getSignersFromDatabase(String financingHdrCode) {
        try {

            String DebtorName = financingHdrRepository.findDebtorNameByFinancingHdrCode(UUID.fromString(financingHdrCode));

            List<Debtor> debtors = debtorRepository.findByDebtorName(DebtorName);

            return debtors.stream()
                    .map(Debtor::getKaryawanName)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            throw new RuntimeException("Failed to get signers from database: " + e.getMessage());
        }
    }

    private List<String> getSignersFromExternalApi2(String financingHdrCode, String agreementNo) {
        try {
            UUID uuid = UUID.fromString(financingHdrCode);
            Agreement agreement = agreementRepository.findByFinancingHdr_FinancingHdrCode2(uuid, agreementNo)
                    .orElseThrow(() -> new RuntimeException("Agreement not found"));

            String custNo = agreement.getCwr().getCustomer().getCustNo();
            String cwrNo = agreement.getCwr().getCwrCode();

            if (custNo == null || custNo.isBlank()) {
                throw new IllegalArgumentException("custNo untuk financingHdrCode " + financingHdrCode + " tidak tersedia");
            }

            SignerRequestDto request = new SignerRequestDto(custNo, cwrNo, LocalDate.now().toString());
            ExternalApiResponse response = callExternalApi(request);

            return response.getReturnObject().stream()
                    .map(signer -> signer.getSignerName())
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to get signers from external API: " + e.getMessage());
        }
    }

    private SignerCheckResultDto createComparisonResult(List<String> dbSigners, List<String> externalApiSigners) {
        SignerCheckResultDto result = new SignerCheckResultDto();
        result.setConfinsSigners(externalApiSigners);

        List<String> unmatched = new ArrayList<>();

        for (String dbSigner : dbSigners) {
            if (!externalApiSigners.contains(dbSigner)) {
                unmatched.add(dbSigner);
            }
        }

        result.setUnmatchedSigners(unmatched);
        return result;
    }

    public ResponseEntity<ApiResponse<?>> downloadDocument(String documentId, Authentication authentication) {
        try {
            String username = authentication != null ? authentication.getName() : "SYSTEM";

            if (documentId == null || documentId.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse<>(false, "DocumentId is required", null, null, null));
            }

            String doc = String.valueOf(agreementFileSigningRepository.findByDocumentId(documentId));

            if (doc == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse<>(false, "Document not found in database", null, null, null));
            }

            HttpHeaders requestHeaders = new HttpHeaders();
            requestHeaders.set("x-api-Key", apiKey);
            requestHeaders.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> requestBody = new LinkedHashMap<>();
            Map<String, String> audit = new LinkedHashMap<>();
            audit.put("callerId", username);
            requestBody.put("audit", audit);
            requestBody.put("documentId", documentId);

            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, requestHeaders);

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

            ExternalDownloadResponse responseBody = response.getBody();
            if (responseBody == null) {
                return ResponseEntity.internalServerError()
                        .body(new ApiResponse<>(false, "Empty response from external API", null, null, null));
            }

            int externalStatusCode = responseBody.getStatus().getCode();
            String externalMessage = responseBody.getStatus().getMessage();

            if (externalStatusCode == 0) { // Success
                try {
                    byte[] pdfBytes = Base64.getDecoder().decode(responseBody.getPdfBase64());
                    String base64Pdf = responseBody.getPdfBase64();

                    return ResponseEntity.ok()
                            .body(new ApiResponse<>(true, "Document retrieved successfully",
                                    Map.of(
                                            "filename", "document_" + documentId + ".pdf",
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

    @Transactional
    public List<SignerDocDto> signerDocList(String financingHdrCode, Authentication authentication) {
        try {
            UUID financingHdrUuid = UUID.fromString(financingHdrCode);
            List<Agreement> agreements = agreementRepository.findByFinancingHdr_FinancingHdrCode(financingHdrUuid);

            if (agreements.isEmpty()) {
                return Collections.emptyList();
            }

            String username = authentication != null ? authentication.getName() : "SYSTEM";
            FinancingHdr financingHdr = financingHdrRepository.findByFinancingHdrCode(financingHdrUuid)
                    .orElseThrow(() -> new EntityNotFoundException("Financing header not found"));

            String bowheerName = financingHdr.getBouwheer().getBouwheerName();

            String signerName = financingHdrRepository.findSignerNameByFinancingHdrCode(financingHdrUuid);

            List<AgreementFileSigning> fileSignings = agreementFileSigningRepository.findByKaryawan(signerName);

            checkExternalSigningStatus(fileSignings, username);

            fileSignings = agreementFileSigningRepository.findByKaryawan(signerName);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

            List<String> agreementCodes = fileSignings.stream()
                    .map(AgreementFileSigning::getAgreementCode)
                    .toList();

            Map<String, String> cwrMap = agreementRepository.findCwrCodesByAgreementCodes(agreementCodes)
                    .stream()
                    .collect(Collectors.toMap(
                            row -> (String) row[0],  // agreementCode
                            row -> (String) row[1]   // cwrCode
                    ));


            return fileSignings.stream()
                    .map(signing -> SignerDocDto.builder()
                            .agreementFileId(signing.getAgreementFileId())
                            .agreementCode(signing.getAgreementCode())
                            .cwrCode(cwrMap.getOrDefault(signing.getAgreementCode(), "")) // di db belum ada
                            .bowheerName(bowheerName)
                            .verifDate(
                                    signing.getVerifDate() != null
                                            ? signing.getVerifDate().format(formatter)
                                            : (signing.getDtmCrt() != null ? signing.getDtmCrt().format(formatter) : null)
                            )
                            .signProgress(signing.getSignProgress())
                            .status(signing.stamp())
                            .documentId(signing.getDocumentId())
                            .build())
                    .collect(Collectors.toList());

        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid financingHdrCode format: " + financingHdrCode);
        }
    }

    private void checkExternalSigningStatus(List<AgreementFileSigning> fileSignings, String username) {
        RestTemplate restTemplate = new RestTemplate();

        for (AgreementFileSigning signing : fileSignings) {
            try {
                Map<String, Object> request = new HashMap<>();
                Map<String, String> audit = new HashMap<>();
                audit.put("callerId", username);
                request.put("audit", audit);
                request.put("refNumber", signing.getAgreementCode());
                request.put("byPassActiveCheck", 0);

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.set("x-api-key", apiKey);

                ResponseEntity<Map> response = restTemplate.exchange(
                        checkDoc,
                        HttpMethod.POST,
                        new HttpEntity<>(request, headers),
                        Map.class
                );

                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    Map<String, Object> responseBody = response.getBody();
                    List<Map<String, Object>> statusSigning = (List<Map<String, Object>>) responseBody.get("statusSigning");

//                    if (statusSigning != null) {
//                        for (Map<String, Object> status : statusSigning) {
//                            if (signing.getDocumentId().equals(status.get("documentId"))) {
//                                List<Map<String, String>> signers = (List<Map<String, String>>) status.get("signer");
//                                String newStatus = determineStatusFromSigners(signers);
//                                if ("signed".equalsIgnoreCase(newStatus) && signing.getVerifDate() == null) {
//                                    signing.setVerifDate(LocalDateTime.now());
//                                }
//                                signing.setStamp(newStatus);
////                                signing.setDtmUpd(LocalDateTime.now());
//                                updateFinancingStep(signing.getFinancingHdrCode(), newStatus);
//                                break;
//                            }
//                        }
//                    }
                    if (statusSigning != null) {
                        for (Map<String, Object> status : statusSigning) {
                            if (signing.getDocumentId().equals(status.get("documentId"))) {
                                List<Map<String, String>> signers = (List<Map<String, String>>) status.get("signer");

                                int totalSigners = signers.size();
                                int signedSigners = 0;
                                boolean hasFailed = false;
                                boolean inProcess = false;

                                for (Map<String, String> signer : signers) {
                                    String signStatus = signer.get("signStatus");
                                    if ("1".equals(signStatus)) {
                                        signedSigners++;
                                    } else if ("2".equals(signStatus)) {
                                        hasFailed = true;
                                    } else if ("3".equals(signStatus)) {
                                        inProcess = true;
                                    }
                                }

                                // Simpan progress "x/y"
                                signing.setSignProgress(signedSigners + "/" + totalSigners);

                                String newStatus;
                                if (hasFailed) {
                                    newStatus = "Sign Failed";
                                } else if (signedSigners == totalSigners) {
                                    newStatus = "signed";
                                    // set verifDate kalau baru pertama kali signed
                                    if (signing.getVerifDate() == null) {
                                        signing.setVerifDate(LocalDateTime.now());
                                    }
                                } else if (inProcess || signedSigners > 0) {
                                    newStatus = "Signing in Process";
                                } else {
                                    newStatus = "Menunggu TTD";
                                }

                                signing.setStamp(newStatus);
                                updateFinancingStep(signing.getFinancingHdrCode(), newStatus);
                                break;
                            }
                        }
                    }

                }
            } catch (Exception e) {
                signing.setStamp("Menunggu TTD");
            } finally {
                AgreementFileSigning updated =agreementFileSigningRepository.save(signing);
                log.info("Updated signing id={} stamp={}", updated.getAgreementFileId(), updated.getStamp());
            }
        }
    }

    private String determineStatusFromSigners(List<Map<String, String>> signers) {
        if (signers == null || signers.isEmpty()) {
            return "Menunggu TTD";
        }

        for (Map<String, String> signer : signers) {
            String signStatus = signer.get("signStatus");
            if ("1".equals(signStatus)) return "signed";
            if ("2".equals(signStatus)) return "Sign Failed";
            if ("3".equals(signStatus)) return "Signing in Process";
        }

        return "Menunggu TTD";
    }

    private void updateFinancingStep(String financingHdrCode, String stampStatus) {
        financingHdrRepository.findByFinancingHdrCode(UUID.fromString(financingHdrCode))
                .ifPresent(finHdr -> {
                    if ("Signing in Process".equalsIgnoreCase(stampStatus)) {
                        finHdr.setFinancingStep("SIGNING");
                    } else if ("Menunggu TTD".equalsIgnoreCase(stampStatus)) {
                        finHdr.setFinancingStep("SIGNING");
                    } else if ("signed".equalsIgnoreCase(stampStatus)) {
                        finHdr.setFinancingStep("SIGNED");
                    }
                    financingHdrRepository.save(finHdr);
                    log.info("Updated financingHdrCode={} step={}", financingHdrCode, finHdr.getFinancingStep());
                });
    }

    public List<DebtorDto> checkSignerDanasakti(String financingHdrCode, Authentication authentication) {
        String username = authentication != null ? authentication.getName() : "SYSTEM";

        String debtorName = financingHdrRepository.findDebtorNameByFinancingHdrCode(UUID.fromString(financingHdrCode));

        if (debtorName == null) {
            return Collections.emptyList();
        }

        List<Debtor> debtors = debtorRepository.findByDebtorName(debtorName);

        if (debtors == null || debtors.isEmpty()) {
            return Collections.emptyList();
        }

        List<CompletableFuture<DebtorDto>> futures = debtors.stream()
                .map(debtor -> processDebtorAsync(debtor, debtor.getFinancingHdrCode(), username))
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

    public Map<String, Object> checkSendDocument(String financingHdrCode, String agreementCode) {
        Map<String, Object> response = new HashMap<>();

        List<AgreementFileSigning> existingFiles = agreementFileSigningRepository.findByAgreementCode(agreementCode);

        if (existingFiles.isEmpty()) {
            // Belum pernah dikirim -> belum ada tanda tangan
            response.put("needConfirmation", false);
            response.put("message", "Dokumen belum pernah dikirim, bisa langsung kirim");
            return response;
        }

        AgreementFileSigning file = existingFiles.get(0);

        // Kalau progress ada tapi belum lengkap (misalnya 1/2, 2/3)
        boolean partialSigned = file.getSignProgress() != null
                && !file.getSignProgress().split("/")[0].equals(file.getSignProgress().split("/")[1]);

        if (partialSigned) {
            response.put("needConfirmation", true);
            response.put("message", "Dokumen sudah ditandatangani sebagian (" + file.getSignProgress() + "). Apakah Anda yakin ingin mengirim ulang?");
            return response;
        }

        // Default -> belum ada tanda tangan sama sekali
        response.put("needConfirmation", false);
        response.put("message", "Dokumen belum ditandatangani, bisa langsung kirim");
        return response;
    }


}
