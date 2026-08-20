package com.kmkbe.modules.customer.repository;

import com.kmkbe.modules.customer.model.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long>, PagingAndSortingRepository<Customer,Long> {
    Optional<Customer> findByCustEmail(String email);

    Optional<Customer> findByCustEmailOrderByCustIdDesc(String email);

    Optional<Customer> findByCustCode(UUID custCode);

    boolean existsByCustEmailIgnoreCaseAndCustIdNoNot(String email, String custIdNo);

   Optional<Customer> findFirstByBouwheer(String bouwheerCode);

   Page<Customer> findAll(Pageable pageable);

   Page<Customer> findAll(Specification<Customer> specification, Pageable pageable);

   Optional<Customer> findByBouwheerAndCustExternalCode(String bouwheer, String vendorId);

}
