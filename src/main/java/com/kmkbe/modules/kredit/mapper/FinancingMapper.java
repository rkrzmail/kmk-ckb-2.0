package com.kmkbe.modules.kredit.mapper;

import com.kmkbe.modules.kredit.entity.FinancingHdr;
import com.kmkbe.modules.kredit.request.CreateFinancingRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface FinancingMapper {
    FinancingMapper INSTANCE = Mappers.getMapper(FinancingMapper.class);

    @Mapping(target = "custCode", ignore = true)
    @Mapping(target = "bouwheerCode", ignore = true)
    FinancingHdr headerFromCreateRequest(CreateFinancingRequest request);
}
