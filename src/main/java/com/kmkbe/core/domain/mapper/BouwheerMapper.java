package com.kmkbe.core.domain.mapper;

import com.kmkbe.core.domain.dto.BouwheerDto;
import com.kmkbe.modules.bouwheer.model.entity.Bouwheer;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface BouwheerMapper {
    BouwheerMapper INSTANCE = Mappers.getMapper(BouwheerMapper.class);

    BouwheerDto fromEntity(Bouwheer entity);
}
