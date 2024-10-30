package com.kmkbe.core.domain.repository;

import com.kmkbe.core.domain.entity.BranchAreaMapping;
import com.kmkbe.core.domain.entity.Product;
import com.kmkbe.modules.user.entity.MstBranch;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<List<Product>> findAllByIsActiveOrderByIsActive(Boolean isActive);

    @Query(
            value = "select * from product where ntf_from <= :amount and ntf_to >= :amount",
            nativeQuery = true
    )
    Optional<Product> findNtfRange(@Param("amount") Double amount);

    default Optional<List<Product>> getAllActive() {
        return findAllByIsActiveOrderByIsActive(true);
    }

    Page<Product> findAllByIsActive(Pageable pageable, Boolean isActive);
}
