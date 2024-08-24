package com.kmkbe.modules.loan_submission.repository;

import com.kmkbe.modules.loan_submission.entity.FinancingHdr;
import com.kmkbe.modules.loan_submission.entity.SimulationHist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SimulationHistRepository extends JpaRepository<SimulationHist, UUID> {
    Optional<List<SimulationHist>> findAllByFinancingHdr(FinancingHdr financingHdr);

    Optional<SimulationHist> findFirstByFinancingHdrOrderBySimulationHistIdDesc(FinancingHdr financingHdr);
}
