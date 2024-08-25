package com.kmkbe.core.domain.repository;

import com.kmkbe.core.domain.entity.FinancingHdr;
import com.kmkbe.core.domain.entity.SimulationHist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SimulationHistRepository extends JpaRepository<SimulationHist, UUID> {
    Optional<List<SimulationHist>> findAllByFinancingHdr(FinancingHdr financingHdr);

    Optional<SimulationHist> findFirstByFinancingHdrOrderBySimulationHistIdDesc(FinancingHdr financingHdr);
}
