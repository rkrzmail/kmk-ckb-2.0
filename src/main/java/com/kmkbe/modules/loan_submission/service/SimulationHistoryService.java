package com.kmkbe.modules.loan_submission.service;

import com.kmkbe.core.domain.entity.Customer;
import com.kmkbe.core.domain.dto.SimulationHistDto;
import com.kmkbe.core.domain.entity.FinancingHdr;
import com.kmkbe.core.domain.entity.SimulationHist;
import com.kmkbe.core.domain.mapper.SimulationHistoryMapper;
import com.kmkbe.core.domain.repository.SimulationHistRepository;
import com.kmkbe.core.utils.DateTimeUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SimulationHistoryService {
    private final SimulationHistRepository simulationHistRepository;

    public SimulationHistDto findLastBy(FinancingHdr financingHdr) {
        try {
            SimulationHist entity = simulationHistRepository.findFirstByFinancingHdrOrderBySimulationHistIdDesc(financingHdr)
                    .orElse(null);

            if (entity == null) {
                return null;
            }

            return SimulationHistoryMapper.INSTANCE.toDto(entity);
        } catch (Exception e) {
            log.error("findLastBy, error {}", e.getMessage());
            throw e;
        }
    }

    public List<SimulationHistDto> findAllBy(
            FinancingHdr financingHdr
    ) {
        try {
            final List<SimulationHist> entities = simulationHistRepository.findAllByFinancingHdr(financingHdr).orElse(new ArrayList<>());
            List<SimulationHistDto> result = new ArrayList<>();
            if (!entities.isEmpty()) {
                result = entities.stream().map(SimulationHistoryMapper.INSTANCE::toDto).toList();
            }

            return result;
        } catch (Exception e) {
            log.error("findAll, error {}", e.getMessage());
            throw e;
        }
    }

    public SimulationHist create(
            Customer customer,
            FinancingHdr financingHdr,
            double totalInvoiceAmt,
            double retention,
            double adminAmt,
            double financingAmt
    ) {
        try {
            SimulationHist history = SimulationHist.builder()
                    .financingHdr(financingHdr)
                    .totalInvoiceAmt(totalInvoiceAmt)
                    .retention(retention)
                    .adminAmt(adminAmt)
                    .financingAmt(financingAmt)
                    .usrCrt(customer.getCustName())
                    .dtmCrt(DateTimeUtils.now())
                    .build();

            return simulationHistRepository.save(history);
        } catch (Exception e) {
            log.error("create, error {}", e.getMessage());
            throw e;
        }
    }
}
