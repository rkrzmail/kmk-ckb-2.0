package com.kmkbe.core.domain.mapper;

import com.kmkbe.core.domain.dto.InvoiceDto;
import com.kmkbe.core.domain.entity.Invoice;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface InvoiceMapper {
    InvoiceMapper INSTANCE = Mappers.getMapper(InvoiceMapper.class);

    //@Mapping(target = "bouwheer", ignore = true)
    InvoiceDto dtoFromEntity(Invoice invoice);

    Invoice entityFromDto(InvoiceDto invoiceDto);
}
