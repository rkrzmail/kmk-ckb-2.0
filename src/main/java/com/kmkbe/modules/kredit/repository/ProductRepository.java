package com.kmkbe.modules.kredit.repository;

import com.kmkbe.modules.kredit.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<List<Product>> findAllByIsActive(Boolean isActive);

    default Optional<List<Product>> getAllActive() {
        return findAllByIsActive(true);
    }
}
