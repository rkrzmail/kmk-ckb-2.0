package com.kmkbe.modules.loan_submission.mapper;

import com.kmkbe.modules.loan_submission.dto.BouwheerDto;
import com.kmkbe.modules.loan_submission.entity.Bouwheer;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface BouwheerMapper {
    BouwheerMapper INSTANCE = Mappers.getMapper(BouwheerMapper.class);

    BouwheerDto fromEntity(Bouwheer entity);
}
