package com.kmkbe.modules.branch_admin.service;

import com.kmkbe.core.domain.dto.*;
import com.kmkbe.core.domain.entity.CsulSigner;
import com.kmkbe.core.domain.mapper.CsulSignerMapper;
import com.kmkbe.core.domain.repository.CsulSignerRepository;
import com.kmkbe.core.domain.repository.ExternalSignerRepository;
import com.kmkbe.modules.common.service.EmailService;
import com.kmkbe.modules.remote.service.AuthRemoteService;
import com.kmkbe.modules.remote.service.EmailAo;
import com.kmkbe.modules.user.entity.MstBranch;
import com.kmkbe.modules.user.repository.MstBranchRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CsulSignerService {
    private final RestTemplate restTemplate;
    private final CsulSignerRepository csulSignerRepository;
    private final ExternalSignerRepository externalSignerRepository;
    private final CsulSignerMapper csulSignerMapper = CsulSignerMapper.INSTANCE;
    private final MstBranchRepository mstBranchRepository;
    private final AuthRemoteService authRemoteService;
    private final EmailAo emailAo;
    private final EmailService emailService;
    private final String apiKey = "YiByHB@CSUL_DEV";
    private final String registerUrl = "https://gdkwebserver.ad-ins.com/adimobile/demo/esign/services/external/user/checkRegistration";
    private final String generateLinkUrl = "https://gdkwebserver.ad-ins.com/adimobile/demo/esign/services/external/user/generateInvLink";

    private String jwtToken;

    private void ensureJwtToken() {
        jwtToken = authRemoteService.fetchAuthJwt().getData();
    }

    public List<SignerCsulDto> signerCsulList(Authentication authentication) {
        String username = authentication != null ?
                authentication.getName() :
                "SYSTEM";

        List<CsulSigner> entities = csulSignerRepository.findAll();
        return entities.stream()
                .map(e -> SignerCsulDto.builder()
                        .signerId(e.getSignerId())
                        .karyawanName(e.getKaryawanName())
                        .jabatan(e.getJabatan())
                        .identityNo(e.getIdentityNo())
                        .email(e.getEmail())
                        .noTelp(e.getNoTelp())
                        .isActive(e.getIsActive())
                        .signhubStatus(e.getSignhubStatus())
                        .build()
                )
                .toList();
    }

    public SignerCsulDto detailSigner(Long id) {
        CsulSigner entity = csulSignerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Signer dengan ID " + id + " tidak ditemukan"));

        return SignerCsulDto.builder()
                .signerId(entity.getSignerId())
                .karyawanName(entity.getKaryawanName())
                .jabatan(entity.getJabatan())
                .identityNo(entity.getIdentityNo())
                .email(entity.getEmail())
                .noTelp(entity.getNoTelp())
                .isActive(entity.getIsActive())
                .signhubStatus(entity.getSignhubStatus())
                .build();
    }

//     data static get signer csul
    public ExternalSignerResponse getSignersStatic() {
        List<ExternalSignerDto> signers = externalSignerRepository.findAll()
                .stream()
                .map(signer -> new ExternalSignerDto(
                        signer.getOffice(),
                        signer.getDepartment(),
                        signer.getEmail(),
                        signer.getName(),
                        signer.getPosition()
                ))
                .collect(Collectors.toList());

        return ExternalSignerResponse.builder()
                .signers(signers)
                .statusCode("200")
                .message("Success")
                .build();
    }

    public Map<String, Object> getSignersGrouped(String username) {
        ensureJwtToken();

        String cleanUsername = username.replaceFirst("(?i)^Admin\\s*", "");

        String branchCode = mstBranchRepository.findByBranchName(cleanUsername)
                .map(MstBranch::getBranchCode)
                .orElseThrow(() -> {
                    return new RuntimeException("BranchCode tidak ditemukan untuk username: " + cleanUsername);
                });

        List<Map<String, String>> bmList = emailAo.getEmailByPosition(branchCode, "BM/BOH", jwtToken);

        List<Map<String, Object>> branchManagers = bmList.stream().map(bm -> {
            Map<String, Object> bmMap = new HashMap<>();
            bmMap.put("employeeCode", bm.get("employeeCode"));
            bmMap.put("employeeName", toCamelCase(bm.get("employeeName")));
            bmMap.put("email", bm.get("email"));
            bmMap.put("branchCode", branchCode);
            return bmMap;
        }).collect(Collectors.toList());

        List<Map<String, String>> asmList = emailAo.getEmailByPosition(branchCode, "RM", jwtToken);

        List<Map<String, Object>> areaSalesManagers = asmList.stream().map(asm -> {
            Map<String, Object> asmMap = new HashMap<>();
            asmMap.put("employeeCode", asm.get("employeeCode"));
            asmMap.put("employeeName", toCamelCase(asm.get("employeeName")));
            asmMap.put("email", asm.get("email"));
            asmMap.put("branchCode", branchCode);
            return asmMap;
        }).collect(Collectors.toList());

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("BranchManager", branchManagers);
        responseData.put("AreaSalesManager", areaSalesManagers);

        return responseData;
    }


    private String toCamelCase(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        text = text.toLowerCase();
        String[] words = text.split(" ");
        StringBuilder camelCaseText = new StringBuilder();

        for (String word : words) {
            if (!word.isEmpty()) {
                camelCaseText.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1))
                        .append(" ");
            }
        }

        return camelCaseText.toString().trim();
    }



    @Transactional
    public SignerCsulRequest createSigner(SignerCsulRequest request, Authentication authentication) {
        try{
            if (csulSignerRepository.existsByIdentityNo(request.getIdentityNo())) {
            throw new RuntimeException("NIK sudah terdaftar");
            }

            String username = authentication != null ?
                    authentication.getName() :
                    "SYSTEM";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-api-key", apiKey);

            log.info("Calling Registration API for debtor with identityNo: {}", request.getIdentityNo());
            Map<String, Object> registerResponse = callRegistrationApi(request, headers, username);
            log.info("Register API Response: {}", registerResponse);

            String registrationStatus = "0";

            if (registerResponse.containsKey("registrationData")) {
                List<Map<String, Object>> registrationData = (List<Map<String, Object>>) registerResponse.get("registrationData");
                if (registrationData != null && !registrationData.isEmpty()) {
                    registrationStatus = (String) registrationData.get(0).get("registrationStatus");
                }
            }

            SignerCsulRequest savedSigner = saveSigner(request, username);

            switch (registrationStatus) {
                case "0":
                    Map<String, Object> inviteResponse = callInvitationApi(request, headers, username);
                    String invitationLink = (String) inviteResponse.get("link");
                    if (invitationLink == null) {
                        throw new RuntimeException("Gagal generate link undangan");
                    }

                    log.info("Sending invitation email to: {}", request.getEmail());
                    emailService.sendInvitationLinkEmail(
                            request.getEmail(),
                            invitationLink,
                            request.getKaryawanName()
                    );

                    savedSigner.setRegistrationMessage("Registrasi berhasil dan undangan telah dikirim");
                    break;
                case "1":
                    savedSigner.setRegistrationMessage("Akun sudah registrasi, namun belum di aktivasi");
                    break;
                case "2":
                    savedSigner.setRegistrationMessage("Signer person sudah register dan aktivasi");
                    break;
                default:
                    throw new RuntimeException("Status registrasi tidak dikenali: " + registrationStatus);
            }

            return savedSigner;

        } catch(
        Exception e) {
            log.error("Error: {}", e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    private Map<String, Object> callRegistrationApi(SignerCsulRequest request, HttpHeaders headers, String username) {
        Map<String, Object> requestBody = Map.of(
                "audit", Map.of("callerId", username),
                "dataType", "NIK",
                "userData", request.getIdentityNo()
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

    private SignerCsulRequest saveSigner(SignerCsulRequest request, String username) {
        CsulSigner entity = CsulSigner.builder()
                .karyawanName(request.getKaryawanName())
                .jabatan(request.getJabatan())
                .identityNo(request.getIdentityNo())
                .email(request.getEmail())
                .noTelp(request.getNoTelp())
                .isActive(request.getIsActive())
                .signhubStatus("Not Registered")
                .usrCrt(username)
                .dtmCrt(LocalDateTime.now())
                .build();

        CsulSigner savedSigner = csulSignerRepository.save(entity);

        return csulSignerMapper.entityToDto(savedSigner);
    }

    private Map<String, Object> callInvitationApi(SignerCsulRequest request, HttpHeaders headers, String username) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("provinsi", "DKI JAKARTA");
        requestBody.put("kota", request.getKota());
        requestBody.put("kelurahan", request.getKelurahan());
        requestBody.put("tmpLahir", request.getTempatLahir());
        requestBody.put("alamat", request.getAlamat());
        requestBody.put("tglLahir", request.getTanggalLahir());
        requestBody.put("nama", request.getKaryawanName());
        requestBody.put("kecamatan", request.getKecamatan());
        requestBody.put("tlp", request.getNoTelp());
        requestBody.put("jenisKelamin", request.getJenisKelamin());
        requestBody.put("idKtp", request.getIdentityNo());
        requestBody.put("kodePos", request.getKodePos());
        requestBody.put("email", request.getEmail());
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

    public void updateSigner(Long id, SignerCsulRequest request, Authentication authentication) {
        CsulSigner entity = csulSignerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Signer dengan ID " + id + " tidak ditemukan"));

        if (csulSignerRepository.existsByIdentityNo(request.getIdentityNo())
                && !request.getIdentityNo().equals(entity.getIdentityNo())) {
            throw new RuntimeException("NIK sudah terdaftar");
        }

        entity.setKaryawanName(request.getKaryawanName());
        entity.setJabatan(request.getJabatan());
        entity.setIdentityNo(request.getIdentityNo());
        entity.setEmail(request.getEmail());
        entity.setNoTelp(request.getNoTelp());
        entity.setIsActive(request.getIsActive());
        entity.setUsrCrt(authentication.getName());
        entity.setDtmCrt(LocalDateTime.now());

        csulSignerRepository.save(entity);
    }

    public Map<String, Object> getSignersGrouped2() {
        List<CsulSigner> signers = csulSignerRepository.findAll();

        List<SignerGroupedDto> dtoList = signers.stream()
                .map(s -> new SignerGroupedDto(
                        s.getSignerId(),
                        s.getKaryawanName(),
                        s.getJabatan(),
                        s.getIdentityNo(),
                        s.getEmail()
                ))
                .toList();

        Map<String, List<SignerGroupedDto>> grouped = dtoList.stream()
                .collect(Collectors.groupingBy(SignerGroupedDto::getJabatan));

        Map<String, Object> responseData = new LinkedHashMap<>();

        responseData.put("BranchManager", grouped.getOrDefault("Branch Manager", new ArrayList<>()));
        responseData.put("AreaSalesManager", grouped.getOrDefault("Area Sales Manager", new ArrayList<>()));

        return responseData;
    }
}
