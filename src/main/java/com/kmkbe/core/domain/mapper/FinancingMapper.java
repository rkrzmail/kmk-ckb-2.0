package com.kmkbe.core.domain.mapper;

import com.kmkbe.core.domain.dto.FinancingDtlDto;
import com.kmkbe.core.domain.dto.FinancingHdrDto;
import com.kmkbe.core.domain.dto.FinancingHdrListDto;
import com.kmkbe.core.domain.entity.FinancingDtl;
import com.kmkbe.core.domain.entity.FinancingHdr;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface FinancingMapper {
    FinancingMapper INSTANCE = Mappers.getMapper(FinancingMapper.class);

    @Mapping(target = "customer", ignore = true)
    @Mapping(target = "bouwheer", ignore = true)
    FinancingHdr hdrEntityFromDto(FinancingHdrDto financingHdrDto);

    @Mapping(target = "customer", ignore = true)
    @Mapping(target = "bouwheer", ignore = true)
    @Mapping(source = "financingDtls", target = "details")
    FinancingHdrDto hdrDtoFromEntity(FinancingHdr financingHdr);

    @Mapping(source = "invoice", target = "invoice")
    FinancingDtlDto dtlDtoFromEntity(FinancingDtl financingDtl);

    List<FinancingDtlDto> dtlsDtoFromEntities(List<FinancingDtl> financingDtls);

    FinancingHdr fromDtoList(FinancingHdrListDto listDto);
}
