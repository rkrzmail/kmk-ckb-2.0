package com.kmkbe.modules.branch_admin.service;

import com.kmkbe.core.domain.dto.*;
import com.kmkbe.core.domain.entity.CsulSigner;
import com.kmkbe.core.domain.entity.Debtor;
import com.kmkbe.core.domain.model.CommonResult;
import com.kmkbe.core.domain.repository.CsulSignerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CsulSignerService {
    private final CsulSignerRepository csulSignerRepository;


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
                        .tempatLahir(e.getTempatLahir())
                        .tanggalLahir(e.getTanggalLahir())
                        .jenisKelamin(e.getJenisKelamin())
                        .alamat(e.getAlamat())
                        .rt(e.getRt())
                        .rw(e.getRw())
                        .kodePos(e.getKodePos())
                        .kelurahan(e.getKelurahan())
                        .kecamatan(e.getKecamatan())
                        .kota(e.getKota())
                        .isActive(e.getIsActive())
                        .signhubStatus(e.getSignhubStatus())
                        .build()
                )
                .toList();
    }

    public SignerCsulDto detailSigner(Long id) {
        CsulSigner entity = csulSignerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Signer dengan ID " + id + " tidak ditemukan"));

        // mapping Entity → DTO (pakai builder)
        return SignerCsulDto.builder()
                .signerId(entity.getSignerId())
                .karyawanName(entity.getKaryawanName())
                .jabatan(entity.getJabatan())
                .identityNo(entity.getIdentityNo())
                .email(entity.getEmail())
                .noTelp(entity.getNoTelp())
                .tempatLahir(entity.getTempatLahir())
                .tanggalLahir(entity.getTanggalLahir())
                .jenisKelamin(entity.getJenisKelamin())
                .alamat(entity.getAlamat())
                .rt(entity.getRt())
                .rw(entity.getRw())
                .kodePos(entity.getKodePos())
                .kelurahan(entity.getKelurahan())
                .kecamatan(entity.getKecamatan())
                .kota(entity.getKota())
                .isActive(entity.getIsActive())
                .signhubStatus(entity.getSignhubStatus())
                .build();
    }

    // data static get signer csul
    public ExternalSignerResponse getStaticSigners() {
        List<ExternalSignerDto> signers = List.of(
                new ExternalSignerDto("Head Office", "Finnance", "M.Noprialdo@csul.co.id", "M Noprialdo", "BRANCH MANAGER"),
                new ExternalSignerDto("Head Office", "Finnance", "It.project@csul.co.id", "M Sopii", "Area Sales Manager"),
                new ExternalSignerDto("Head Office", "Finnance", "M.Nanto@csul.co.id", "M Nanto", "Area Sales Manager")
        );

        return ExternalSignerResponse.builder()
                .signers(signers)
                .statusCode("200")
                .message("Success")
                .build();
    }

    public void createSigner(SignerCsulRequest request, Authentication authentication) {
        if (csulSignerRepository.existsByIdentityNo(request.getIdentityNo())) {
            throw new RuntimeException("NIK sudah terdaftar");
        }
        CsulSigner entity = CsulSigner.builder()
                .karyawanName(request.getKaryawanName())
                .jabatan(request.getJabatan())
                .identityNo(request.getIdentityNo())
                .email(request.getEmail())
                .noTelp(request.getNoTelp())
                .tempatLahir(request.getTempatLahir())
                .tanggalLahir(request.getTanggalLahir())
                .jenisKelamin(request.getJenisKelamin())
                .alamat(request.getAlamat())
                .rt(request.getRt())
                .rw(request.getRw())
                .kodePos(request.getKodePos())
                .kelurahan(request.getKelurahan())
                .kecamatan(request.getKecamatan())
                .kota(request.getKota())
                .isActive(request.getIsActive())
                .signhubStatus("Not Registered")
                .usrCrt(authentication.getName())
                .dtmCrt(LocalDateTime.now())
                .build();

        csulSignerRepository.save(entity);
    }


}
