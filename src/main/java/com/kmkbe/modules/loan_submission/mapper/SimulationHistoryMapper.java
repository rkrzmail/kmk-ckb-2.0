package com.kmkbe.modules.loan_submission.mapper;

import com.kmkbe.modules.loan_submission.dto.SimulationHistDto;
import com.kmkbe.modules.loan_submission.entity.SimulationHist;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface SimulationHistoryMapper {
    SimulationHistoryMapper INSTANCE = Mappers.getMapper(SimulationHistoryMapper.class);

    SimulationHist toEntity(SimulationHistDto dto);

    SimulationHistDto toDto(SimulationHist entity);
}
