package com.kmkbe.modules.branch_admin.service;

import com.kmkbe.core.domain.dto.PolicyAgreementDto;
import com.kmkbe.core.domain.entity.PolicyAgreement;
import com.kmkbe.core.domain.entity.PolicyAgreementHistory;
import com.kmkbe.core.domain.model.CommonResult;
import com.kmkbe.core.domain.repository.PolicyAgreementHistoryRepository;
import com.kmkbe.core.domain.repository.PolicyAgreementRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class PolicyAgreementService {

    @Autowired
    private PolicyAgreementRepository policyAgreementRepository;

    @Autowired
    private PolicyAgreementHistoryRepository policyAgreementHistoryRepository;

    @Transactional
    public CommonResult<PolicyAgreementDto> createPolicyAgreement(PolicyAgreementDto policyAgreementDto) {
        PolicyAgreement policyAgreement = new PolicyAgreement();
        policyAgreement.setPolicyName(policyAgreementDto.getPolicyName());
        policyAgreement.setPolicyDescription(policyAgreementDto.getPolicyDescription());
        policyAgreement.setPolicyContent(policyAgreementDto.getPolicyContent());
        policyAgreement.setVersion(policyAgreementDto.getVersion());
        policyAgreement.setIsActive(policyAgreementDto.getIsActive());
//        policyAgreement.setUsrCrt("SYSTEM");

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = (authentication != null) ? authentication.getName() : "UNKNOWN";  // Jika tidak ada user, beri default "UNKNOWN"

        policyAgreement.setUsrCrt(currentUsername);
        policyAgreement.setDtmCrt(LocalDateTime.now());

        String generatedPolicyCode = UUID.randomUUID().toString();  // Menghasilkan UUID sebagai policy code
        policyAgreement.setPolicyCode(generatedPolicyCode);

        // Simpan ke repository
        policyAgreementRepository.save(policyAgreement);

        return new CommonResult<PolicyAgreementDto>().success(policyAgreementDto);
    }

    // Method untuk mengambil semua PolicyAgreement dari DB dan mengonversinya ke PolicyAgreementDto
    public List<PolicyAgreementDto> getPolicyAgreementList() {
        // Ambil semua data PolicyAgreement
        List<PolicyAgreement> policyAgreements = policyAgreementRepository.findAll();

        // Convert list entity ke DTO tanpa builder
        List<PolicyAgreementDto> dtoList = new ArrayList<>();

        for (PolicyAgreement policy : policyAgreements) {
            PolicyAgreementDto policyAgreementDto = new PolicyAgreementDto();
            policyAgreementDto.setPolicyId(policy.getPolicyId());
            policyAgreementDto.setPolicyCode(policy.getPolicyCode().toString()); // Convert UUID ke String
            policyAgreementDto.setPolicyName(policy.getPolicyName());
            policyAgreementDto.setPolicyDescription(policy.getPolicyDescription());
            policyAgreementDto.setPolicyContent(policy.getPolicyContent());
            policyAgreementDto.setVersion(policy.getVersion());
            policyAgreementDto.setIsActive(policy.getIsActive());
            policyAgreementDto.setUsrCrt(policy.getUsrCrt());
            policyAgreementDto.setDtmCrt(policy.getDtmCrt());
            policyAgreementDto.setUsrUpd(policy.getUsrUpd());
            policyAgreementDto.setDtmUpd(policy.getDtmUpd());

            dtoList.add(policyAgreementDto);
        }

        return dtoList;
    }

    // get code PA history
    public CommonResult<PolicyAgreementDto> getPolicyAgreementHistoryByCode(String policyCode) {
        // Mengambil semua entri berdasarkan policyCode
        List<PolicyAgreementHistory> policyAgreementHistoryList = policyAgreementHistoryRepository.findByPolicyCode(policyCode);

        if (!policyAgreementHistoryList.isEmpty()) {
            // Mengambil entri dengan versi terbesar
            PolicyAgreementHistory latestHistory = policyAgreementHistoryList.stream()
                    .max(Comparator.comparingInt(PolicyAgreementHistory::getVersion))
                    .orElseThrow(() -> new RuntimeException("No policy history found"));

            // Mapping PolicyAgreementHistory ke PolicyAgreementDto
            PolicyAgreementDto policyAgreementDto = new PolicyAgreementDto();
            policyAgreementDto.setPolicyCode(latestHistory.getPolicyCode());
            policyAgreementDto.setPolicyContent(latestHistory.getPolicyContent());
            policyAgreementDto.setVersion(latestHistory.getVersion());
            policyAgreementDto.setUsrCrt(latestHistory.getUsrCrt());
            policyAgreementDto.setDtmCrt(latestHistory.getDtmCrt());

            // Mengembalikan hasil
            return new CommonResult<PolicyAgreementDto>().success(policyAgreementDto);
        } else {
            // Jika tidak ditemukan, kembalikan hasil gagal
            return new CommonResult<PolicyAgreementDto>().fail(400, "Policy Agreement History not found");
        }
    }



//    // Update Policy Agreement
//    public CommonResult<PolicyAgreementDto> updatePolicyAgreement(Long id, PolicyAgreementDto policyAgreementDto) {
//        Optional<PolicyAgreement> existingPolicy = policyAgreementRepository.findById(id);
//
//        if (!existingPolicy.isPresent()) {
//            return new CommonResult<PolicyAgreementDto>().fail(400,"Policy Agreement not found");
//        }
//
//        PolicyAgreement policy = existingPolicy.get();
//
//        // Update fields using setters (no builder)
//        policy.setPolicyName(policyAgreementDto.getPolicyName());
//        policy.setPolicyDescription(policyAgreementDto.getPolicyDescription());
//        policy.setPolicyContent(policyAgreementDto.getPolicyContent());
//        policy.setVersion(policyAgreementDto.getVersion());
//        policy.setIsActive(policyAgreementDto.getIsActive());
//        // Set other fields if necessary
//        policy.setUsrUpd(policyAgreementDto.getUsrUpd());  // Example for user update
//        policy.setDtmUpd(LocalDateTime.now()); // Example for timestamp update
//
//        // Save updated policy back to the database
//        policyAgreementRepository.save(policy);
//
//        // Convert updated entity back to DTO and return response
//        PolicyAgreementDto updatedPolicyDto = new PolicyAgreementDto();
//        updatedPolicyDto.setPolicyId(policy.getPolicyId());
//        updatedPolicyDto.setPolicyName(policy.getPolicyName());
//        updatedPolicyDto.setPolicyDescription(policy.getPolicyDescription());
//        updatedPolicyDto.setPolicyContent(policy.getPolicyContent());
//        updatedPolicyDto.setVersion(policy.getVersion());
//        updatedPolicyDto.setIsActive(policy.getIsActive());
//
//        return new CommonResult<PolicyAgreementDto>().success(updatedPolicyDto);
//    }

    // get by id

    public CommonResult<PolicyAgreementDto> getPolicyAgreementById(Long id) {
        // Cek apakah policy agreement ditemukan
        Optional<PolicyAgreement> policyAgreementOpt = policyAgreementRepository.findById(id);

        if (policyAgreementOpt.isPresent()) {
            PolicyAgreement policyAgreement = policyAgreementOpt.get();

            // Mapping PolicyAgreement ke PolicyAgreementDto
            PolicyAgreementDto policyAgreementDto = new PolicyAgreementDto();
            policyAgreementDto.setPolicyName(policyAgreement.getPolicyName());
            policyAgreementDto.setPolicyDescription(policyAgreement.getPolicyDescription());
            policyAgreementDto.setPolicyContent(policyAgreement.getPolicyContent());
            policyAgreementDto.setVersion(policyAgreement.getVersion());
            policyAgreementDto.setIsActive(policyAgreement.getIsActive());

            // Mengembalikan hasil
            return new CommonResult<PolicyAgreementDto>().success(policyAgreementDto);
        } else {
            // Jika tidak ditemukan, kembalikan hasil gagal
            return new CommonResult<PolicyAgreementDto>().fail(400,"Policy Agreement not found");
        }
    }

    // Update Policy Agreement
//    public CommonResult<PolicyAgreementDto> updatePolicyAgreement(Long id, PolicyAgreementDto policyAgreementDto) {
//        Optional<PolicyAgreement> existingPolicy = policyAgreementRepository.findById(id);
//
//        if (!existingPolicy.isPresent()) {
//            return new CommonResult<PolicyAgreementDto>().fail(400,"Policy Agreement not found");
//        }
//
//        PolicyAgreement policy = existingPolicy.get();
//
//        // Update fields using setters (no builder)
//        policy.setPolicyName(policyAgreementDto.getPolicyName());
//        policy.setPolicyDescription(policyAgreementDto.getPolicyDescription());
//        policy.setPolicyContent(policyAgreementDto.getPolicyContent());
//
//        // Increment version by 1
//        policy.setVersion(policy.getVersion() + 1); // Menambah versi setiap kali diupdate
//
//        policy.setIsActive(policyAgreementDto.getIsActive());
//        // Set other fields if necessary
//        policy.setUsrUpd(policyAgreementDto.getUsrUpd());  // Example for user update
//        policy.setDtmUpd(LocalDateTime.now()); // Example for timestamp update
//
//        // Save updated policy back to the database
//        policyAgreementRepository.save(policy);
//
//        // Convert updated entity back to DTO and return response
//        PolicyAgreementDto updatedPolicyDto = new PolicyAgreementDto();
//        updatedPolicyDto.setPolicyId(policy.getPolicyId());
//        updatedPolicyDto.setPolicyName(policy.getPolicyName());
//        updatedPolicyDto.setPolicyDescription(policy.getPolicyDescription());
//        updatedPolicyDto.setPolicyContent(policy.getPolicyContent());
//        updatedPolicyDto.setVersion(policy.getVersion()); // Ensure the version is updated
//        updatedPolicyDto.setIsActive(policy.getIsActive());
//
//        return new CommonResult<PolicyAgreementDto>().success(updatedPolicyDto);
//    }

    public CommonResult<PolicyAgreementDto> updatePolicyAgreement(Long id, PolicyAgreementDto policyAgreementDto) {
        Optional<PolicyAgreement> existingPolicy = policyAgreementRepository.findById(id);

        if (!existingPolicy.isPresent()) {
            return new CommonResult<PolicyAgreementDto>().fail(400,"Policy Agreement not found");
        }

        PolicyAgreement policy = existingPolicy.get();

        PolicyAgreementHistory history = new PolicyAgreementHistory();
        history.setPolicyCode(policy.getPolicyCode());
        history.setPolicyContent(policy.getPolicyContent());
        history.setVersion(policy.getVersion());
        history.setUsrCrt(policy.getUsrCrt());
        history.setDtmCrt(policy.getDtmCrt());
        policyAgreementHistoryRepository.save(history);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = (authentication != null) ? authentication.getName() : "UNKNOWN";
        policy.setPolicyName(policyAgreementDto.getPolicyName());
        policy.setPolicyDescription(policyAgreementDto.getPolicyDescription());
        policy.setPolicyContent(policyAgreementDto.getPolicyContent());
        policy.setVersion(policy.getVersion() + 1);
        policy.setIsActive(policyAgreementDto.getIsActive());
        policy.setUsrUpd(currentUsername);
        policy.setDtmUpd(LocalDateTime.now());

        policyAgreementRepository.save(policy);

        PolicyAgreementDto updatedPolicyDto = new PolicyAgreementDto();
        updatedPolicyDto.setPolicyId(policy.getPolicyId());
        updatedPolicyDto.setPolicyName(policy.getPolicyName());
        updatedPolicyDto.setPolicyDescription(policy.getPolicyDescription());
        updatedPolicyDto.setPolicyContent(policy.getPolicyContent());
        updatedPolicyDto.setVersion(policy.getVersion());
        updatedPolicyDto.setIsActive(policy.getIsActive());

        return new CommonResult<PolicyAgreementDto>().success(updatedPolicyDto);
    }


}
