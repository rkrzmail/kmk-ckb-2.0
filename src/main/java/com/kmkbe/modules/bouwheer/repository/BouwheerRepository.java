package com.kmkbe.modules.bouwheer.repository;

import com.kmkbe.modules.bouwheer.model.entity.Bouwheer;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.Optional;
import java.util.UUID;

public interface BouwheerRepository extends PagingAndSortingRepository<Bouwheer, UUID>, CrudRepository<Bouwheer, UUID> {

    Optional<Bouwheer> findFirstByBouwheerName(String name);

    Optional<Bouwheer> findByBouwheerCode(UUID code);

    default Bouwheer findFirstByBouwheerName(){
        return findFirstByBouwheerName("PT. Trakindo Utama").orElse(null);
    }
}
