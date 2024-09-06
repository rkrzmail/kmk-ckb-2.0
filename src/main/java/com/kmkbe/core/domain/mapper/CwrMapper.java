package com.kmkbe.core.domain.mapper;

import com.kmkbe.core.domain.dto.CwrListDto;
import com.kmkbe.core.domain.entity.Cwr;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface CwrMapper {
    CwrMapper INSTANCE = Mappers.getMapper(CwrMapper.class);

    @Mapping(target = "bouwheer.bouwheerCode", source = "bouwheerCode")
    @Mapping(target = "bouwheer.bouwheerName", source = "bouwheerName")
    @Mapping(target = "customer.custCode", source = "custCode")
    @Mapping(target = "customer.custName", source = "custName")
    Cwr toEntity(CwrListDto cwrListDto);

    @Mapping(target = "bouwheerCode", source = "bouwheer.bouwheerCode")
    @Mapping(target = "bouwheerName", source = "bouwheer.bouwheerName")
    @Mapping(target = "custCode", source = "customer.custCode")
    @Mapping(target = "custName", source = "customer.custName")
    @Mapping(target = "no", ignore = true)
    CwrListDto toDto(Cwr cwr);
}
