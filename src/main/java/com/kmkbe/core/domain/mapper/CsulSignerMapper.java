package com.kmkbe.core.domain.mapper;

import com.kmkbe.core.domain.dto.DebtorDto;
import com.kmkbe.core.domain.dto.SignerCsulRequest;
import com.kmkbe.core.domain.entity.CsulSigner;
import com.kmkbe.core.domain.entity.Debtor;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface CsulSignerMapper {
    CsulSignerMapper INSTANCE = Mappers.getMapper(CsulSignerMapper.class);

    SignerCsulRequest entityToDto(CsulSigner csulSigner);
}
