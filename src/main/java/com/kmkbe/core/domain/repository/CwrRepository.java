package com.kmkbe.core.domain.repository;

import com.kmkbe.core.domain.entity.Customer;
import com.kmkbe.core.domain.entity.Cwr;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CwrRepository extends JpaRepository<Cwr, String>, JpaSpecificationExecutor<Cwr> {
    Page<Cwr> findAllByCustomer(Customer customer, Pageable pageable);
}
