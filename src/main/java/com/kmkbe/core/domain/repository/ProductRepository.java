package com.kmkbe.core.domain.repository;

import com.kmkbe.modules.product.model.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
  Optional<List<Product>> findAllByIsActiveOrderByIsActive(Boolean isActive);

  @Query(
    value = "select * from product where ntf_from <= :amount and ntf_to >= :amount AND is_active='t' LIMIT 1;",
    nativeQuery = true
  )
  Optional<Product> findNtfRange(@Param("amount") Double amount);


  @Query("SELECT p FROM Product p WHERE :amount BETWEEN p.ntfFrom AND p.ntfTo AND p.isActive = true")
  Optional<Product> findFirstByAmountInRange(@Param("amount") Double amount);

  @Query(
    "SELECT p FROM Product p " +
      "WHERE :amount BETWEEN p.ntfFrom AND p.ntfTo " +
      "AND p.isActive = true " +
      "AND p.bouwheer.bouwheerCode = :bouwheerCode"
  )
  Optional<Product> findFirstByAmountInRangeAndBouwheerCode(
    @Param("amount") Double amount,
    @Param("bouwheerCode") UUID bouwheerCode
  );

  @Query(
    value = "select * from product " +
      "where ntf_from <= :amount " +
      "and ntf_to >= :amount " +
      "and is_active='t' " +
      "and bouwheer_code = :bouwheerCode " +
      "limit 1",
    nativeQuery = true
  )
  Optional<Product> findNtfRangeByBouwheerCode(
    @Param("amount") Double amount,
    @Param("bouwheerCode") UUID bouwheerCode
  );

  default Optional<List<Product>> getAllActive() {
    return findAllByIsActiveOrderByIsActive(true);
  }

  Page<Product> findAllByProductId(Pageable pageable, Long productId);

  Page<Product> findAll(Specification<Product> specification, Pageable pageable);

  @Query("SELECT p FROM Product p ORDER BY p.productId DESC")
  List<Product> findLatestProduct(Pageable pageable);

  Optional<Product> findByProductCode(String productCode);

  Optional<Product> findByProductCodeIgnoreCase(String productCode);

  Product findTopByOrderByProductIdDesc();

  Optional<Product> findById(Long productId);

}
