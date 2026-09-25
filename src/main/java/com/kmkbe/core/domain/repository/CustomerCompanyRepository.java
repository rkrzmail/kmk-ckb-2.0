package com.kmkbe.core.domain.repository;

import com.kmkbe.modules.customer.model.entity.Customer;
import com.kmkbe.core.domain.entity.CustomerCompany;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CustomerCompanyRepository extends JpaRepository<CustomerCompany, UUID> {
    Optional<CustomerCompany> findByCustomer(Customer cust);
}
