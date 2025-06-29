package com.kmkbe.core.domain.mapper;

import com.kmkbe.core.domain.dto.DebtorDto;
import com.kmkbe.core.domain.dto.ProductDto;
import com.kmkbe.core.domain.entity.Debtor;
import com.kmkbe.core.domain.entity.Product;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface DebtorMapper {
    DebtorMapper INSTANCE = Mappers.getMapper(DebtorMapper.class);

    DebtorDto entityToDto(Debtor debtor);
}
