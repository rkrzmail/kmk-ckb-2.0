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

    @Query("SELECT " +
            "c.custName, " +
            "c.custIdNo, " +
            "b.bouwheerName, " +
            "f.mstBranch.branchCode, " +
            "a.agreementCode, " +
            "ph.goliveDate, " +
            "(SELECT COUNT(*) FROM Cwr c WHERE LENGTH(c.cwrCode) > 1), " +
            "f.financingAmt, " +
            "(ph.totalInvAmt - ph.interestAmt), " +
            "f.effectiveRate, " +
            "ph.retentionAmt, " +
            "ph.lcAmt, " +
            "ph.dueDate, " +
            "ph.settlementDte, " +
            "f.financingStatus " +
            "FROM Customer c " +
            "JOIN FinancingHdr f ON f.customer.custCode = c.custCode " +
            "JOIN Bouwheer b ON f.bouwheer.bouwheerCode = b.bouwheerCode " +
            "JOIN Agreement a ON a.financingHdr.financingHdrCode = f.financingHdrCode " +
            "JOIN PaymentReceiveHistory ph ON ph.agreementCode = a.agreementCode " +
            "WHERE DATE(ph.dueDate) BETWEEN :startDate AND :endDate")
//            "WHERE c.isActive = TRUE")
    Page<Object[]> findDueDate(Pageable pageable, @Param("startDate") String startDate, @Param("endDate") String endDate);

}
