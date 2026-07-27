package com.kmkbe.core.domain.repository;

import com.kmkbe.core.domain.dto.ProyeksiReportDto;
import com.kmkbe.core.domain.dto.ReportDueDateDto;
import com.kmkbe.core.domain.dto.SummaryByBranchDto;
import com.kmkbe.core.domain.entity.Customer;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long>, PagingAndSortingRepository<Customer,Long> {
    Optional<Customer> findByCustEmail(String email);

    Optional<Customer> findByCustEmailOrderByCustIdDesc(String email);

    Optional<Customer> findByCustCode(UUID custCode);

    Optional<Customer> findByCustEmailAndCustPin(String email, String pin);

    boolean existsByCustEmailAndCustIdNoNot(String custEmail, String custIdNo);

    boolean existsByCustEmailIgnoreCaseAndCustIdNoNot(String email, String custIdNo);

   Optional<Customer> findByBouwheer(String bouwheer);

   Page<Customer> findAll(Pageable pageable);

   Optional<Customer> findByVendorId(String vendorId);
}
