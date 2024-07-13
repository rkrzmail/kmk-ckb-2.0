package com.kmkbe.modules.loan_submission.mapper;

import com.kmkbe.modules.loan_submission.entity.FinancingHdr;
import com.kmkbe.modules.loan_submission.request.CreateFinancingRequest;
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
