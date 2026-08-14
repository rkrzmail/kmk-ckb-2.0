package com.kmkbe.core.domain.repository;

import com.kmkbe.core.domain.entity.Product;
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
    value = "select * from product where ntf_from <= :amount and ntf_to >= :amount AND is_active='t' LIMIT 1;",
    nativeQuery = true
  )
  Optional<Product> findNtfRange(@Param("amount") Double amount);


  @Query("SELECT p FROM Product p WHERE :amount BETWEEN p.ntfFrom AND p.ntfTo AND p.isActive = true")
  Optional<Product> findFirstByAmountInRange(@Param("amount") Double amount);


  default Optional<List<Product>> getAllActive() {
    return findAllByIsActiveOrderByIsActive(true);
  }

  Page<Product> findAllByProductId(Pageable pageable, Long productId);

  @Query("SELECT p FROM Product p ORDER BY p.productId DESC")
  List<Product> findLatestProduct(Pageable pageable);

  Optional<Product> findByProductCode(String productCode);

  Product findTopByOrderByProductIdDesc();

  Optional<Product> findById(Long productId);

}
