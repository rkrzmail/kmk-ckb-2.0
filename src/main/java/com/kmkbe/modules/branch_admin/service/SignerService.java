package com.kmkbe.modules.branch_admin.service;

import com.kmkbe.core.domain.dto.*;
import com.kmkbe.core.domain.entity.*;
import com.kmkbe.core.domain.mapper.DebtorMapper;
import com.kmkbe.core.domain.model.PaginationResult;
import com.kmkbe.core.domain.repository.*;
import com.kmkbe.core.domain.request.PaginationRequest;
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

import java.security.SignatureException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    public List<DebtorDto> signerPersonList() {
        // Ambil semua data PolicyAgreement
        List<Debtor> Debtor = debtorRepository.findAll();

        // Convert list entity ke DTO tanpa builder
        List<DebtorDto> dtoList = new ArrayList<>();

        for (Debtor signer : Debtor) {
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

            dtoList.add(debtorDto);
        }

        return dtoList;
    }

    @Transactional
    public DebtorDto createDebtor(DebtorDto debtorDto) {

        // 1. Check Registration
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-api-key", apiKey);

        Map<String, Object> checkBody = new HashMap<>();
        checkBody.put("audit", Map.of("callerId", callerId));
        checkBody.put("dataType", "NIK");
        checkBody.put("userData", debtorDto.getIdentityNo());

        HttpEntity<Map<String, Object>> checkRequest = new HttpEntity<>(checkBody, headers);
        ResponseEntity<Map> checkResponse = restTemplate.postForEntity(registerUrl, checkRequest, Map.class);

        System.out.println("===== [CHECK REGISTRATION RESPONSE] =====");
        System.out.println(checkResponse.getBody()); // log seluruh response
        System.out.println("==========================================");

        Map<String, Object> status = (Map<String, Object>) checkResponse.getBody().get("status");

        if (status != null && ((Integer) status.get("code")) == 8165) {
            // 2. Generate Invitation Link
            Map<String, Object> generateBody = new HashMap<>();
            generateBody.put("provinsi", "DKI JAKARTA");
            generateBody.put("kota", debtorDto.getKota());
            generateBody.put("kelurahan", debtorDto.getKelurahan());
            generateBody.put("tmpLahir", debtorDto.getTempatLahir());
            generateBody.put("alamat", debtorDto.getAlamat());
            generateBody.put("tglLahir", debtorDto.getTanggalLahir());
            generateBody.put("nama", debtorDto.getDebtorName());
            generateBody.put("kecamatan", debtorDto.getKecamatan());
            generateBody.put("tlp", debtorDto.getNoTelp());
            generateBody.put("jenisKelamin", debtorDto.getJenisKelamin().equalsIgnoreCase("L") ? "M" : "F");
            generateBody.put("idKtp", debtorDto.getIdentityNo());
            generateBody.put("kodePos", debtorDto.getKodePos());
            generateBody.put("email", debtorDto.getEmail());
            generateBody.put("type", "EMPLOYEE");
            generateBody.put("audit", Map.of("callerId", "USERBAF"));

            HttpEntity<Map<String, Object>> generateRequest = new HttpEntity<>(generateBody, headers);
            ResponseEntity<Map> generateResponse = restTemplate.postForEntity(generateLinkUrl, generateRequest, Map.class);

            System.out.println("===== [GENERATE INVITATION LINK RESPONSE] =====");
            System.out.println(generateResponse.getBody()); // log seluruh response
            System.out.println("================================================");

            Map<String, Object> genResponse = generateResponse.getBody();
            String invitationLink = (String) genResponse.get("link");

            if (invitationLink != null) {
                System.out.println(">> INVITATION LINK GENERATED: " + invitationLink);
                emailService.sendInvitationLinkEmail(debtorDto.getEmailDebtor(), invitationLink);
                System.out.println("email tujuan : " + debtorDto.getEmailDebtor());
            } else {
                System.out.println(">> WARNING: Invitation link tidak tersedia!");
            }
        } else {
            System.out.println(">> NIK ditemukan, tidak perlu generate link.");
        }

        // 3. Simpan ke DB
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
                .build();

        Debtor savedDebtor = debtorRepository.save(debtor);

        return debtorMapper.entityToDto(savedDebtor);
    }

    public PersonDto getSignersFromExternalApi() {
        String custNo = "41000001137"; //hardcode sementara
        String cwrNo = "41350CWR2024454"; //hardcode sementara
        String requestDateTime = LocalDate.now().toString();

        SignerRequestDto externalRequest = new SignerRequestDto(
                custNo,
                cwrNo,
                requestDateTime
        );

        try {
            ExternalApiResponse response = callExternalApi(externalRequest);
            return mapToPersonDto(response);

        } catch (Exception e) {
            PersonDto errorDto = new PersonDto();
            errorDto.setStatusCode("500");
            errorDto.setMessage("Error calling external API");
            return errorDto;
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

        if (externalResponse != null) {
            result.setStatusCode(externalResponse.getStatusCode());
            result.setMessage(externalResponse.getMessage());

            if (externalResponse.getReturnObject() != null) {
                result.setSigners(
                        externalResponse.getReturnObject().stream()
                                .map(externalSigner -> {
                                    PersonDto.Signer signer = new PersonDto.Signer();
                                    signer.setCwrSignerId(externalSigner.getCwrSignerId());
                                    signer.setCwrCustId(externalSigner.getCwrCustId());
                                    signer.setSignerType(externalSigner.getSignerType());
                                    signer.setSignerName(externalSigner.getSignerName());
                                    signer.setSignerPosition(externalSigner.getSignerPosition());
                                    return signer;
                                })
                                .collect(Collectors.toList())
                );
            }
        }

//        System.out.println("Hasil mapping: " + result); // Log hasil mapping
        return result;
    }
}
