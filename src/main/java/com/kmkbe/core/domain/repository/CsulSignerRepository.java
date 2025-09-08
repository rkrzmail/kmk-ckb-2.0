package com.kmkbe.core.domain.repository;

import com.kmkbe.core.domain.entity.CsulSigner;
import com.kmkbe.core.domain.entity.Debtor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface CsulSignerRepository extends JpaRepository<CsulSigner, Long> {
    boolean existsByIdentityNo(String identityNo);

    boolean existsByKaryawanName(String karyawanName);

    Optional<CsulSigner> findByKaryawanNameAndJabatan(String karyawanName, String jabatan);

    Optional<CsulSigner> findByIdentityNo(String identityNo);

    Optional<CsulSigner> findByKaryawanName(String karyawanName);

    List<CsulSigner> findByUsrCrt(String usrCrt);

}
