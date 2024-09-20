package com.kmkbe.core.domain.repository;

import com.kmkbe.core.domain.entity.Customer;
import com.kmkbe.core.domain.entity.Cwr;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface CwrRepository extends JpaRepository<Cwr, String>, JpaSpecificationExecutor<Cwr> {
    Page<Cwr> findAllByCustomer(Customer customer, Pageable pageable);

    @Query(
            value = """
                    select * from public.cwr where cwr_code = :cwrCode order by cwr_id desc limit 1
                    """,
            nativeQuery = true
    )
    Optional<Cwr> findTopByCwrCode(
            @Param("cwrCode") String cwrCode
    );

    Optional<Cwr> findTopByCustomerOrderByCwrEndDateDesc(Customer customer);
}
