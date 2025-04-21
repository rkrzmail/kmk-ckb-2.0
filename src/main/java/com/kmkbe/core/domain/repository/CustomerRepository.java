package com.kmkbe.core.domain.repository;

import com.kmkbe.core.domain.dto.DuedateDto;
import com.kmkbe.core.domain.dto.ProyeksiDto;
import com.kmkbe.core.domain.dto.VisitorDto;
import com.kmkbe.core.domain.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import com.kmkbe.core.domain.entity.ExistingCustomer;
import com.kmkbe.core.domain.entity.CustomerCompany;
import com.kmkbe.core.domain.entity.Cwr;
import com.kmkbe.core.domain.entity.Bouwheer;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByCustEmail(String email);

    Optional<Customer> findByCustEmailOrderByCustIdDesc(String email);

    Optional<Customer> findByCustCode(UUID custCode);

    Optional<Customer> findByCustEmailAndCustPin(String email, String pin);

    @Query("SELECT new com.kmkbe.core.domain.dto.VisitorDto(c.custName, e.isExisting, b.bouwheerName) " +
            "FROM ExistingCustomer e " +
            "JOIN CustomerCompany cc ON e.identityNo = cc.identityNo " +
            "JOIN Customer c ON cc.customer.custCode = c.custCode " +
            "JOIN Cwr cwr ON cc.customer.custCode = cwr.customer.custCode " +
            "JOIN Bouwheer b ON cwr.bouwheer.bouwheerCode = b.bouwheerCode")
    List<VisitorDto> findVisitorData();

}
