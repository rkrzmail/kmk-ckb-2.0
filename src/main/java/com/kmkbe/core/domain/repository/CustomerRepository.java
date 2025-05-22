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

    @Query(nativeQuery = true, value = "SELECT DISTINCT\n" +
            "       c.cust_name AS debtor_name, \n" +
            "    c.existing_cust AS debtor_status, \n" +
            "    b.bouwheer_name AS bouwheer_name, \n" +
            "    i.cust_inv_no AS invoice_no, \n" +
            "    i.invoice_amt AS amount_invoice, \n" +
            "    f.financing_amt AS amount_financing, \n" +
            "    i.invoice_due_date AS invoice_due_date, \n" +
            "    f.financing_date AS effective_date\n" +
            "FROM \n" +
            "    financing_hdr f \n" +
            "LEFT  JOIN \n" +
            "    customer c ON f.cust_code = c.cust_code\n" +
            "LEFT JOIN \n" +
            "    financing_dtl  fd ON f.financing_hdr_code = fd.financing_hdr_code\n" +
            "LEFT JOIN \n" +
            "    invoice i ON fd.invoice_code = i.invoice_code\n" +
            "\n" +
            "LEFT JOIN \n" +
            "    bouwheer b ON f.bouwheer_code = b.bouwheer_code\n" +
            "    \n" +
            "WHERE  Date(i.invoice_due_date) BETWEEN :startDate AND :endDate    ")
    Page<ProyeksiReportDto> findActiveCustomersWithInvoiceDetails(Pageable pageable, @Param("startDate") String startDate, @Param("endDate") String endDate);

    @Query("SELECT new com.kmkbe.core.domain.dto.SummaryByBranchDto(" +
            "c.custName, " +
            "f.mstBranch.branchCode, " +
            "c.custIdNo, " +
            "b.bouwheerName, " +
            "f.disburseAmt, " +
            "cwr.plafondAmt, " +
            "f.financingAmt, " +
            "p.retentionAmt) " +
            "FROM Customer c " +
            "JOIN FinancingHdr f ON c.custCode = f.customer.custCode " +
            "JOIN Bouwheer b ON b.bouwheerCode = f.bouwheer.bouwheerCode " +
            "JOIN Cwr cwr ON c.custCode = cwr.customer.custCode " +
            "JOIN Agreement a ON a.financingHdr.financingHdrCode = f.financingHdrCode " +
            "JOIN PaymentReceiveHistory p ON p.agreementCode = a.agreementCode " +
            "WHERE c.isActive = true " +
            "AND c.custIdTypeCode = 'NPWP'")
    Page<SummaryByBranchDto> findSummaryByCustCode(Pageable pageable);

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
            "WHERE c.isActive = TRUE")
    Page<Object[]> findDueDate(Pageable pageable);

}
