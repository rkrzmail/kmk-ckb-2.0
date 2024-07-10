package com.kmkbe.modules.kredit.repository;

import com.kmkbe.modules.kredit.entity.Bouwheer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface BouwheerRepository extends JpaRepository<Bouwheer, UUID> {
    Optional<Bouwheer> findByBouwheerCode(UUID code);
}
