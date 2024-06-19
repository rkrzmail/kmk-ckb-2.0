package com.kmkbe.modules.user.repository;

import com.kmkbe.modules.user.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, Integer> {
}
