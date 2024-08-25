package com.kmkbe.core.domain.repository;

import com.kmkbe.core.domain.entity.Bouwheer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface BouwheerRepository extends JpaRepository<Bouwheer, UUID> {
    Optional<Bouwheer> findFirstByOrderByBouwheerId();

    Optional<Bouwheer> findFirstByBouwheerName(String name);

    Optional<Bouwheer> findByBouwheerCode(UUID code);
}
