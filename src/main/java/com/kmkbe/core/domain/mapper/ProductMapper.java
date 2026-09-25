package com.kmkbe.core.domain.mapper;

import com.kmkbe.modules.product.model.dto.ProductDto;
import com.kmkbe.modules.product.model.entity.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ProductMapper {
    ProductMapper INSTANCE = Mappers.getMapper(ProductMapper.class);

    @Mapping(source = "bouwheer.bouwheerCode", target = "bouwheerCode")
    @Mapping(source = "bouwheer.bouwheerName", target = "bouwheerName")
    ProductDto entityToDto(Product product);
}
