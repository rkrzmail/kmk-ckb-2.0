package com.kmkbe.core.domain.mapper;

import com.kmkbe.core.domain.dto.AgreementFileSigningDto;
import com.kmkbe.core.domain.entity.AgreementFileSigning;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface AgreementFileSigningMapper {
    AgreementFileSigningMapper INSTANCE = Mappers.getMapper(AgreementFileSigningMapper.class);

    AgreementFileSigningDto entityToDto(AgreementFileSigning agreementFileSigning);
}
