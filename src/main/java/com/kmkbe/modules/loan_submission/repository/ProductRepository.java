package com.kmkbe.modules.loan_submission.repository;

import com.kmkbe.modules.loan_submission.entity.Product;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<List<Product>> findAllByIsActive(Boolean isActive);

    @Query(
            value = "select * from product where ntf_from <= :amount and ntf_to >= :amount",
            nativeQuery = true
    )
    Optional<Product> findNtfRange(@Param("amount") Double amount);

    default Optional<List<Product>> getAllActive() {
        return findAllByIsActive(true);
    }
}
