package com.kmkbe.modules.customer.mapper;

import com.kmkbe.modules.customer.dto.CustomerCompanyDto;
import com.kmkbe.modules.customer.dto.CustomerPersonalDto;
import com.kmkbe.modules.customer.entity.Customer;
import com.kmkbe.modules.customer.dto.CustomerDto;
import com.kmkbe.modules.customer.entity.CustomerCompany;
import com.kmkbe.modules.customer.entity.CustomerPersonal;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface CustomerMapper {
    CustomerMapper INSTANCE = Mappers.getMapper(CustomerMapper.class);

    CustomerDto custDtoFromEntity(Customer customer);

    Customer custEntityFromDto(CustomerDto customerDto);

    CustomerPersonalDto personalDtoFromEntity(CustomerPersonal personal);

    CustomerPersonal personalEntityFromDto(CustomerPersonalDto personalDto);

    CustomerCompanyDto companyDtoFromEntity(CustomerCompany company);

    CustomerCompany companyEntityFromDto(CustomerCompanyDto companyDto);
}
