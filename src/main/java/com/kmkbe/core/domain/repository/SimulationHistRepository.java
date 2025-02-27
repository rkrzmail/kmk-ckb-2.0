package com.kmkbe.core.domain.repository;

import com.kmkbe.core.domain.entity.FinancingHdr;
import com.kmkbe.core.domain.entity.SimulationHist;
import jakarta.validation.constraints.NotNull;
import lombok.NonNull;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SimulationHistRepository extends JpaRepository<SimulationHist, UUID> {
    Optional<List<SimulationHist>> findAllByFinancingHdr(@NotNull(message = "financingHdr null") FinancingHdr financingHdr);

    Optional<SimulationHist> findFirstByFinancingHdrOrderBySimulationHistIdDesc(FinancingHdr financingHdr);

    Page<SimulationHist> findAll(@NonNull Specification<SimulationHist> spec,  @NonNull Pageable pageable);

    Optional<SimulationHist> findTopBySimulationHistCode(UUID simulationHistCode);

    Page<SimulationHist> findAllByFinancingHdrEqualsIgnoreCase(@NotNull(message = "financingHdr null") FinancingHdr financingHdr, @NonNull Specification<SimulationHist> spec,  @NonNull Pageable pageable);

}
