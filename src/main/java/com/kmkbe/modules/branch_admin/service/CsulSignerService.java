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
import java.util.*;
import java.util.stream.Collectors;

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


    public void updateSigner(Long id, SignerCsulRequest request, Authentication authentication) {
        CsulSigner entity = csulSignerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Signer dengan ID " + id + " tidak ditemukan"));

        // Cek kalau identityNo sudah dipakai user lain
        if (csulSignerRepository.existsByIdentityNo(request.getIdentityNo())
                && !request.getIdentityNo().equals(entity.getIdentityNo())) {
            throw new RuntimeException("NIK sudah terdaftar");
        }

        // Update field dari request
        entity.setKaryawanName(request.getKaryawanName());
        entity.setJabatan(request.getJabatan());
        entity.setIdentityNo(request.getIdentityNo());
        entity.setEmail(request.getEmail());
        entity.setNoTelp(request.getNoTelp());
        entity.setTempatLahir(request.getTempatLahir());
        entity.setTanggalLahir(request.getTanggalLahir());
        entity.setJenisKelamin(request.getJenisKelamin());
        entity.setAlamat(request.getAlamat());
        entity.setRt(request.getRt());
        entity.setRw(request.getRw());
        entity.setKodePos(request.getKodePos());
        entity.setKelurahan(request.getKelurahan());
        entity.setKecamatan(request.getKecamatan());
        entity.setKota(request.getKota());
        entity.setIsActive(request.getIsActive());

        // update user yang edit & timestamp
        entity.setUsrCrt(authentication.getName());
        entity.setDtmCrt(LocalDateTime.now());

        csulSignerRepository.save(entity);
    }

    public Map<String, Object> getSignersGrouped() {
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
