package com.kmkbe.core.domain.mapper;

import com.kmkbe.core.domain.dto.SimulationHistDto;
import com.kmkbe.core.domain.entity.SimulationHist;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface SimulationHistoryMapper {
    SimulationHistoryMapper INSTANCE = Mappers.getMapper(SimulationHistoryMapper.class);

    SimulationHist toEntity(SimulationHistDto dto);

    SimulationHistDto toDto(SimulationHist entity);
}
