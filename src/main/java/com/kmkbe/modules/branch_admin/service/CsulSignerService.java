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
    private final CsulSignerMapper csulSignerMapper = CsulSignerMapper.INSTANCE;
    private final MstBranchRepository mstBranchRepository;
    private final AuthRemoteService authRemoteService;
    private final EmailAo emailAo;
    private final String apiKey = "YiByHB@CSUL_DEV";
    private final String registerUrl = "https://gdkwebserver.ad-ins.com/adimobile/demo/esign/services/external/user/checkRegistration";

    private String jwtToken;

    private void ensureJwtToken() {
        jwtToken = authRemoteService.fetchAuthJwt().getData();
    }

    public List<SignerCsulDto> signerCsulList(Authentication authentication) {

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
            } else if (csulSignerRepository.existsByKaryawanName(request.getKaryawanName())) {
                throw new RuntimeException("Signer sudah di daftarkan");
            }

            String username = authentication != null ?
                    authentication.getName() :
                    "SYSTEM";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-api-key", apiKey);

            Map<String, Object> registerResponse = callRegistrationApi(request, headers, username);

            String registrationStatus = "0";

            if (registerResponse.containsKey("registrationData")) {
                List<Map<String, Object>> registrationData =
                        (List<Map<String, Object>>) registerResponse.get("registrationData");

                if (registrationData != null && !registrationData.isEmpty()) {
                    for (Map<String, Object> vendorData : registrationData) {
                        if ("Vida".equalsIgnoreCase((String) vendorData.get("vendor"))) {
                            registrationStatus = String.valueOf(vendorData.get("registrationStatus"));
                            break;
                        }
                    }
                }
            }

            String signhubStatus = "Not Registered";
            String registrationMessage;

            switch (registrationStatus) {
                case "0":
                case "1":
                    registrationMessage="Harap daftarkan signer ke eSignHub terlebih dahulu";
                    break;
                case "2":
                    registrationMessage="Signer person sudah register dan aktivasi ";
                    signhubStatus = "Registered";
                    break;
                default:
                    throw new RuntimeException("Status registrasi tidak dikenali: " + registrationStatus);
            }

            SignerCsulRequest savedSigner = saveSigner(request, username, signhubStatus);
            savedSigner.setRegistrationMessage(registrationMessage);

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

    private String formatJabatan(String jabatan) {
        if (jabatan == null) return null;
        switch (jabatan) {
            case "BranchManager": return "Branch Manager";
            case "AreaSalesManager": return "Area Sales Manager";
            default: return jabatan; // fallback biar tidak error
        }
    }

    private SignerCsulRequest saveSigner(SignerCsulRequest request, String username, String signhubStatus) {
        CsulSigner entity = CsulSigner.builder()
                .karyawanName(request.getKaryawanName())
                .jabatan(formatJabatan(request.getJabatan()))
                .identityNo(request.getIdentityNo())
                .email(request.getEmail())
                .noTelp(request.getNoTelp())
                .isActive(request.getIsActive())
                .signhubStatus(signhubStatus)
                .usrCrt(username)
                .dtmCrt(LocalDateTime.now())
                .build();

        CsulSigner savedSigner = csulSignerRepository.save(entity);

        return csulSignerMapper.entityToDto(savedSigner);
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
                .filter(s -> "Registered".equalsIgnoreCase(s.getSignhubStatus()))
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

        responseData.put("Branch Manager", grouped.getOrDefault("Branch Manager", new ArrayList<>()));
        responseData.put("Area Sales Manager", grouped.getOrDefault("Area Sales Manager", new ArrayList<>()));

        return responseData;
    }

    @Transactional
    public String updateSignerStatus(String identityNo, Authentication authentication) {
        CsulSigner signer = csulSignerRepository.findByIdentityNo(identityNo)
                .orElseThrow(() -> new RuntimeException("Signer dengan identityNo " + identityNo + " tidak ditemukan"));

        String username = authentication != null ? authentication.getName() : "SYSTEM";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-api-key", apiKey);

        SignerCsulRequest tempRequest = new SignerCsulRequest();
        tempRequest.setIdentityNo(identityNo);

        Map<String, Object> registerResponse = callRegistrationApi(tempRequest, headers, username);

        String registrationStatus = "0";
        if (registerResponse.containsKey("registrationData")) {
            List<Map<String, Object>> registrationData = (List<Map<String, Object>>) registerResponse.get("registrationData");
            if (registrationData != null && !registrationData.isEmpty()) {
                for (Map<String, Object> vendorData : registrationData) {
                    if ("Vida".equalsIgnoreCase((String) vendorData.get("vendor"))) {
                        registrationStatus = String.valueOf(vendorData.get("registrationStatus"));
                        break;
                    }
                }
            }
        }

        if ("2".equals(registrationStatus)) {
            signer.setSignhubStatus("Registered");
            signer.setUsrUpd(username);
            signer.setDtmUpd(LocalDateTime.now());
            csulSignerRepository.save(signer);
            return "Signer sudah register dan aktivasi";
        } else {
            return "Signer masih belum terdaftar";
        }
    }

}
