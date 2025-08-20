package com.kmkbe.modules.branch_admin.service;

import com.kmkbe.core.domain.dto.SignerCsulDto;
import com.kmkbe.core.domain.entity.CsulSigner;
import com.kmkbe.core.domain.repository.CsulSignerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CsulSignerService {
    private final CsulSignerRepository csulSignerRepository;


    public List<SignerCsulDto> signerCsulList(Authentication authentication) {

        List<CsulSigner> entities = csulSignerRepository.findAll();

        // Mapping Entity -> DTO
        return entities.stream()
                .map(entity -> new SignerCsulDto(
                        entity.getSignerId(),
                        entity.getKaryawanName(),
                        entity.getJabatan(),
                        entity.getIdentityNo(),
                        entity.getEmail(),
                        entity.getNoTelp(),
                        entity.getTempatLahir(),
                        entity.getTanggalLahir(),
                        entity.getJenisKelamin(),
                        entity.getAlamat(),
                        entity.getRt(),
                        entity.getRw(),
                        entity.getKodePos(),
                        entity.getKelurahan(),
                        entity.getKecamatan(),
                        entity.getKota(),
                        entity.getIsActive(),
                        entity.getSignhubStatus(),
                        entity.getUsrCrt(),
                        entity.getDtmCrt()
                ))
                .toList();
    }


}
