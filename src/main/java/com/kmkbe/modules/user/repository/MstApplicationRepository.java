package com.kmkbe.modules.user.repository;

import com.kmkbe.modules.user.entity.MstApplication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MstApplicationRepository extends JpaRepository<MstApplication, String> {
    Optional<MstApplication> findByApplicationCode(String code);
}
