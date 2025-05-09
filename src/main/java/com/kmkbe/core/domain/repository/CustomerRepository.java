package com.kmkbe.core.domain.repository;

import com.kmkbe.core.domain.dto.ProyeksiReportDto;
import com.kmkbe.core.domain.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByCustEmail(String email);

    Optional<Customer> findByCustEmailOrderByCustIdDesc(String email);

    Optional<Customer> findByCustCode(UUID custCode);

    Optional<Customer> findByCustEmailAndCustPin(String email, String pin);

    @Query("SELECT new com.kmkbe.core.domain.dto.ProyeksiReportDto(c.custName, " +
            "CASE WHEN c.existingCust IS NULL THEN NULL END, " +
            "b.bouwheerName, i.custInvNo, i.invoiceAmt, f.financingAmt, i.invoiceDueDate, f.financingDate) " +
            "FROM Customer c " +
            "JOIN Invoice i ON c.custCode = i.customer.custCode " +
            "JOIN FinancingHdr f ON i.customer.custCode = f.customer.custCode " +
            "JOIN Bouwheer b ON f.bouwheer.bouwheerCode = b.bouwheerCode " +
            "WHERE c.isActive = true")
    Page<ProyeksiReportDto> findActiveCustomersWithInvoiceDetails(Pageable pageable);

}
