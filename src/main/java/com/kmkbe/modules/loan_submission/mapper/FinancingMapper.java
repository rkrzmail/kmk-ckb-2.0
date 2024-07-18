package com.kmkbe.modules.loan_submission.mapper;

import com.kmkbe.modules.loan_submission.dto.FinancingDtlDto;
import com.kmkbe.modules.loan_submission.dto.FinancingHdrDto;
import com.kmkbe.modules.loan_submission.entity.FinancingDtl;
import com.kmkbe.modules.loan_submission.entity.FinancingHdr;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface FinancingMapper {
    FinancingMapper INSTANCE = Mappers.getMapper(FinancingMapper.class);

    @Mapping(target = "customer", ignore = true)
    @Mapping(target = "bouwheer", ignore = true)
    @Mapping(source = "financingDtls", target = "details")
    FinancingHdrDto hdrDtoFromEntity(FinancingHdr financingHdr);

    @Mapping(source = "invoiceCode", target = "invoice")
    FinancingDtlDto dtlDtoFromEntity(FinancingDtl financingDtl);
    
    List<FinancingDtlDto> dtlsDtoFromEntities(List<FinancingDtl> financingDtls);
}
