package com.kmkbe.modules.loan_submission.mapper;

import com.kmkbe.modules.loan_submission.dto.ProductDto;
import com.kmkbe.modules.loan_submission.entity.Product;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ProductMapper {
    ProductMapper INSTANCE = Mappers.getMapper(ProductMapper.class);

    ProductDto entityToDto(Product product);
}
