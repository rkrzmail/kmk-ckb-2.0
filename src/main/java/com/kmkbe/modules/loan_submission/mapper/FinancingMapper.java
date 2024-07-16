package com.kmkbe.modules.loan_submission.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface FinancingMapper {
    FinancingMapper INSTANCE = Mappers.getMapper(FinancingMapper.class);


}
