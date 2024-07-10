package com.kmkbe.modules.kredit.mapper;

import com.kmkbe.modules.kredit.dto.ProductDto;
import com.kmkbe.modules.kredit.entity.Product;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ProductMapper {
    ProductMapper INSTANCE = Mappers.getMapper(ProductMapper.class);

    ProductDto entityToDto(Product product);
}
