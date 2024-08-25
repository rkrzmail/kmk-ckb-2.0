package com.kmkbe.modules.remote.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.kmkbe.core.domain.dto.AreaRemoteDto;
import com.kmkbe.core.domain.dto.BaseMstRemoteResponseDto;
import com.kmkbe.core.domain.dto.InputOptionsRemoteDto;
import com.kmkbe.core.domain.model.CommonResult;
import com.kmkbe.core.service.BaseRemoteService;
import com.kmkbe.modules.remote.request.AreaRemoteRequest;
import com.kmkbe.modules.remote.request.PropCriteriaGenericTypeRequest;
import com.kmkbe.modules.remote.request.RefMasterRequest;
import com.kmkbe.modules.remote.service.MstRemoteService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.EnumUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/v1/mst")
@Tag(
        name = "Master Endpoint",
        description = "Endpoint yg berisi properti umum master atau static data"
)
@RequiredArgsConstructor
public class MstController {
    private final MstRemoteService mstRemoteService;

    @GetMapping("/input-options/{name}")
    public CommonResult<List<InputOptionsRemoteDto>> getOptions(
            @PathVariable("name")
            BaseRemoteService.RefMasterTypeCode name,
            String type
    ) {
        RefMasterRequest.ModelDebiturMappingCode modelDebiturMappingCode = null;

        if (type != null) {
            if (!type.equalsIgnoreCase("PERSONAL")
                    && !type.equalsIgnoreCase("COMPANY")) {
                throw new RuntimeException("Arguments not in list, currently in list is: personal, company");
            }

            modelDebiturMappingCode = RefMasterRequest.ModelDebiturMappingCode.valueOf(type.toUpperCase());
        }


        BaseMstRemoteResponseDto<InputOptionsRemoteDto> result = mstRemoteService.refMasterInputOption(
                name,
                modelDebiturMappingCode
        );

        return new CommonResult<List<InputOptionsRemoteDto>>().success(result.getData());
    }


    @GetMapping("/area")
    public CommonResult<List<AreaRemoteDto>> getArea(
            /*@RequestParam()
            @NotNull(message = "area is required")*/
            String area,

            /*@RequestParam
            @NotNull(message = "value is required")
            @NotEmpty(message = "value shouldn't be empty")*/
            String value
    ) throws JsonProcessingException {
        PropCriteriaGenericTypeRequest.AreaPropName areaProp = null;
        if (EnumUtils.isValidEnum(PropCriteriaGenericTypeRequest.AreaPropName.class, area)) {
            areaProp = PropCriteriaGenericTypeRequest.AreaPropName.valueOf(area);
        }

        BaseMstRemoteResponseDto<AreaRemoteDto> result = mstRemoteService.areaByCriteria(
                AreaRemoteRequest.builder()
                        .area(areaProp)
                        .value(value)
                        .build()
        );

        return new CommonResult<List<AreaRemoteDto>>().success(result.getData());
    }
}
