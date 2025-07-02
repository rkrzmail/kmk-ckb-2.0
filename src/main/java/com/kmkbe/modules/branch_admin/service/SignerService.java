package com.kmkbe.modules.branch_admin.service;

import com.kmkbe.core.domain.dto.*;
import com.kmkbe.core.domain.entity.*;
import com.kmkbe.core.domain.mapper.DebtorMapper;
import com.kmkbe.core.domain.model.PaginationResult;
import com.kmkbe.core.domain.repository.*;
import com.kmkbe.core.domain.request.PaginationRequest;
import com.kmkbe.core.exception.ApiBusinessException;
import com.kmkbe.core.exception.DebtorCreationException;
import com.kmkbe.modules.branch_admin.request.FileUploadRequest;
import com.kmkbe.modules.common.service.EmailService;
import com.kmkbe.modules.user.entity.MstUser;
import com.kmkbe.modules.user.repository.MstUserRepository;
import com.kmkbe.modules.user.utils.UserInternalUtils;
import com.kmkbe.nikita.utils.SpecPagination;
import io.netty.util.internal.StringUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.SignatureException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SignerService {
    private final FinancingHdrRepository financingHdrRepository;
    private final MstUserRepository mstUserRepository;
    private final SignerPersonRepository signerPersonRepository;
    private final RestTemplate restTemplate;
    private final DebtorRepository debtorRepository;
    private final DebtorMapper debtorMapper = DebtorMapper.INSTANCE;
    private final EmailService emailService;
    private final AgreementRepository agreementRepository;
    private final CustomerRepository customerRepository;
    private final AgreementFileSigningRepository agreementFileSigningRepository;

    private final String apiKey = "YiByHB@CSUL_DEV";
    private final String callerId = "USER@AD-INS.COM";
    private final String registerUrl = "https://gdkwebserver.ad-ins.com/adimobile/demo/esign/services/external/user/checkRegistration";
    private final String generateLinkUrl = "https://gdkwebserver.ad-ins.com/adimobile/demo/esign/services/external/user/generateInvLink";
    private final String confinsUrl = "https://confins.csulfinance.com/api/mou/v1/CwrSigner/GetListCwrSignerForUpdatebyCustNoAndCwrNo";

    @Value("${csul.confins.adinskey}")
    private String adInsKey;


    public PaginationResult<SignerDto> assignmentList(
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


            return SpecPagination.paginationData(new SpecPagination<FinancingHdr, SignerDto>(financingHdrPage.stream().toList(), request)
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
                public SignerDto eval(FinancingHdr e) {
                    if (e.getCustomer() == null || e.getBouwheer() == null) {
                        return null;
                    }

                    boolean isNewCust = financingHdrRepository
                            .countByCustomerAndFinancingStatus(
                                    e.getCustomer(),
                                    "PAID"
                            ) == 0;



                    return SignerDto.builder()
                            .financingHdrCode(e.getFinancingHdrCode())
                            .custCode(e.getCustomer().getCustCode())
                            .custName(e.getCustomer().getCustName())
                            .bouwheerName(e.getBouwheer().getBouwheerName())
                            .custStatus(isNewCust ? "New Customer" : "Existing Customer")
                            .build();
                }
            });

        } catch (Exception e) {
            log.error("assignmentList: error {}", e.getMessage());
            throw e;
        }
    }

    public List<DebtorDto> signerPersonList(String financingHdrCode) {
        // Ambil data Debtor berdasarkan financingHdrCode
        List<Debtor> debtors = debtorRepository.findByFinancingHdrCode(financingHdrCode);

        // Convert list entity ke DTO tanpa builder
        List<DebtorDto> dtoList = new ArrayList<>();

        for (Debtor signer : debtors) {
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
            debtorDto.setActive(signer.getIsActive());
            debtorDto.setSignerStatus(signer.getSignerStatus());
            debtorDto.setSignhubStatus(signer.getSignhubStatus());
            debtorDto.setEmailDebtor(signer.getEmailDebtor());
            debtorDto.setFinancingHdrCode(signer.getFinancingHdrCode());

            dtoList.add(debtorDto);
        }

        return dtoList;
    }

    @Transactional
    public DebtorDto createDebtor(DebtorDto debtorDto) {
        try {
            // 1. Header
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-api-key", apiKey);

            // regis
            Map<String, Object> registerResponse = callRegistrationApi(debtorDto, headers);
            log.info("Register API Response: {}", registerResponse);

            // 2. Generate invitation link
            Map<String, Object> inviteResponse = callInvitationApi(debtorDto, headers);
            String invitationLink = (String) inviteResponse.get("link");
            if (invitationLink == null) {
                throw new RuntimeException("Gagal generate link undangan");
            }

            // 3. Kirim email
            emailService.sendInvitationLinkEmail(
                    debtorDto.getEmailDebtor(),
                    invitationLink,
                    debtorDto.getDebtorName()
            );

            // 4. Simpan ke database dan return DebtorDto
            return saveDebtor(debtorDto);

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
        if (responseBody == null) {
            throw new RuntimeException("API registrasi tidak memberikan response");
        }

        // Handle error responses
        if (responseBody.containsKey("status")) {
            Map<String, Object> status = (Map<String, Object>) responseBody.get("status");
            if (!status.get("code").equals(8165)) { // Jika bukan success code
                throw new RuntimeException((String) status.get("message"));
            }
        }

        return responseBody;
    }

    private Map<String, Object> callInvitationApi(DebtorDto debtorDto, HttpHeaders headers) {
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
        requestBody.put("jenisKelamin", debtorDto.getJenisKelamin().equalsIgnoreCase("L") ? "M" : "F");
        requestBody.put("idKtp", debtorDto.getIdentityNo());
        requestBody.put("kodePos", debtorDto.getKodePos());
        requestBody.put("email", debtorDto.getEmail());
        requestBody.put("type", "EMPLOYEE");
        requestBody.put("audit", Map.of("callerId", "USERBAF"));

        ResponseEntity<Map> response = restTemplate.postForEntity(
                generateLinkUrl,
                new HttpEntity<>(requestBody, headers),
                Map.class
        );

        Map<String, Object> responseBody = response.getBody();
        if (responseBody == null) {
            throw new RuntimeException("API undangan tidak memberikan response");
        }

        // Handle error responses
        if (responseBody.containsKey("status")) {
            Map<String, Object> status = (Map<String, Object>) responseBody.get("status");
            if (!status.get("code").equals(0)) { // Jika bukan success code
                throw new RuntimeException((String) status.get("message"));
            }
        }

        return responseBody;
    }

    private DebtorDto saveDebtor(DebtorDto debtorDto) {
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
                .isActive(true)
                .signerStatus("PENDING")
                .signhubStatus("PENDING")
                .emailDebtor(debtorDto.getEmailDebtor())
                .financingHdrCode(debtorDto.getFinancingHdrCode())
                .build();

        return debtorMapper.entityToDto(debtorRepository.save(debtor));
    }

    public PersonDto getSignersFromExternalApi(String financingHdrCode) {
        boolean useHardcode = true; // Ganti nilai ini untuk switch mode

        try {
            String custNo;
            String cwrNo;

            if (useHardcode) {
                // Hardcode values
                custNo = "41000001137";
                cwrNo = "41350CWR2024454";
                log.info("Menggunakan data hardcode - custNo: {}, cwrNo: {}", custNo, cwrNo);
            } else {
                // Ambil dari database
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

//        System.out.println("Raw Response Body: " + response.getBody());

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

//                            signer.setIdentityNo("");
//                            signer.setSignerEndDt(null);

                            return signer;
                        })
                        .toList()
        );
        return result;
    }

    @Transactional
    public AgreementFileSigning uploadAgreementFile(FileUploadRequest request) throws IOException {
        // Validasi
        if (request.getFile() == null || request.getFile().isEmpty()) {
            throw new IllegalArgumentException("File tidak boleh kosong");
        }

        // Buat direktori upload jika belum ada
        Path uploadPath = Paths.get("uploads");
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Generate nama file unik
        String originalFilename = request.getFile().getOriginalFilename();
        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String newFilename = UUID.randomUUID() + fileExtension;

        // Simpan file
        Path targetPath = uploadPath.resolve(newFilename);
        Files.copy(request.getFile().getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        // Simpan ke database
        AgreementFileSigning fileSigning = new AgreementFileSigning();
        fileSigning.setAgreementCode(request.getAgreementCode());
        fileSigning.setFileName(request.getFileName());
        fileSigning.setFileTypeCode(request.getFileTypeCode());
        fileSigning.setFilePath(targetPath.toString());
        fileSigning.setStamp(request.getIsStamp());
        fileSigning.setUsrCrt("SYSTEM");
        fileSigning.setDtmCrt(LocalDateTime.now());

        return agreementFileSigningRepository.save(fileSigning);
    }


}
