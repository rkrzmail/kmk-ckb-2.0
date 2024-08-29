package com.kmkbe.core.domain.mapper;

import com.kmkbe.core.domain.dto.CwrDto;
import com.kmkbe.core.domain.entity.Cwr;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface CwrMapper {
    CwrMapper INSTANCE = Mappers.getMapper(CwrMapper.class);

    @Mapping(target = "bouwheer.bouwheerCode", source = "bouwheerCode")
    @Mapping(target = "customer.custCode", source = "custCode")
    Cwr toEntity(CwrDto cwrDto);

    @Mapping(target = "bouwheerCode", source = "bouwheer.bouwheerCode")
    @Mapping(target = "bouwheerName", source = "bouwheer.bouwheerName")
    CwrDto toDto(Cwr cwr);
}
