package com.kmkbe.core.domain.repository;

import com.kmkbe.core.domain.entity.ExistingCustomer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExistingCustomerRepository extends JpaRepository<ExistingCustomer, Long> {
    // Anda bisa menambahkan query khusus jika diperlukan
}
