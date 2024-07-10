package com.kmkbe.modules.kredit.repository;

import com.kmkbe.modules.customer.entity.Customer;
import com.kmkbe.modules.kredit.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {
    Optional<List<Invoice>> findByCustCode(Customer cust);
}
