package com.kmkbe.modules.customer.mapper;

import com.kmkbe.core.utils.ObjectUtils;
import com.kmkbe.modules.customer.dto.AddressDto;
import com.kmkbe.modules.customer.dto.CustomerCompanyDto;
import com.kmkbe.modules.customer.dto.CustomerDto;
import com.kmkbe.modules.customer.dto.CustomerPersonalDto;
import com.kmkbe.modules.customer.entity.Customer;
import com.kmkbe.modules.customer.entity.CustomerCompany;
import com.kmkbe.modules.customer.entity.CustomerPersonal;
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
