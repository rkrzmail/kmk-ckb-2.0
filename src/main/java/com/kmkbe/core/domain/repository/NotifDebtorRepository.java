package com.kmkbe.core.domain.repository;

import com.kmkbe.core.domain.entity.NotifDebtor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotifDebtorRepository extends JpaRepository<NotifDebtor, Long> {
    List<NotifDebtor> findByCustCode(String custCode);
    void deleteByCustCode(String custCode);
}
