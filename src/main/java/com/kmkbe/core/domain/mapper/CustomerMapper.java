package com.kmkbe.core.domain.mapper;

import com.kmkbe.core.utils.ObjectUtils;
import com.kmkbe.core.domain.dto.AddressDto;
import com.kmkbe.core.domain.dto.CustomerCompanyDto;
import com.kmkbe.modules.customer.model.dto.CustomerDto;
import com.kmkbe.core.domain.dto.CustomerPersonalDto;
import com.kmkbe.modules.customer.model.entity.Customer;
import com.kmkbe.core.domain.entity.CustomerCompany;
import com.kmkbe.core.domain.entity.CustomerPersonal;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.Map;

@Mapper
public interface CustomerMapper {
    CustomerMapper INSTANCE = Mappers.getMapper(CustomerMapper.class);

    @Mapping(target = "address", ignore = true)
    CustomerDto custDtoFromEntity(Customer customer);

    Customer custEntityFromDto(CustomerDto customerDto);

    CustomerPersonalDto personalDtoFromEntity(CustomerPersonal personal);

    CustomerPersonal personalEntityFromDto(CustomerPersonalDto personalDto);

    CustomerCompanyDto companyDtoFromEntity(CustomerCompany company);

    CustomerCompany companyEntityFromDto(CustomerCompanyDto companyDto);

    static AddressDto addressDtoFromCompany(CustomerCompany company) throws IllegalAccessException {
        Map<String, Object> obj = ObjectUtils.castObjectToMap(company);
        return ObjectUtils.castObjectFromMap(obj, new AddressDto());
    }

    static AddressDto addressDtoFromPersonal(CustomerPersonal personal) throws IllegalAccessException {
        Map<String, Object> obj = ObjectUtils.castObjectToMap(personal);
        return ObjectUtils.castObjectFromMap(obj, new AddressDto());
    }
}
