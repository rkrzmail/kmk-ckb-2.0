package com.kmkbe.modules.remote.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.kmkbe.core.model.CommonResult;
import com.kmkbe.core.service.BaseRemoteService;
import com.kmkbe.modules.remote.dto.AreaRemoteDto;
import com.kmkbe.modules.remote.dto.BaseMstRemoteResponseDto;
import com.kmkbe.modules.remote.dto.InputOptionsRemoteDto;
import com.kmkbe.modules.remote.request.AreaRequest;
import com.kmkbe.modules.remote.request.PropCriteriaGenericTypeRequest;
import com.kmkbe.modules.remote.request.RefMasterRequest;
import com.kmkbe.modules.remote.service.MstRemoteService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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
            @RequestParam()
            @NotNull(message = "area is required")
            PropCriteriaGenericTypeRequest.AreaPropName area,

            @RequestParam
            @NotNull(message = "value is required")
            @NotEmpty(message = "value shouldn't be empty")
            String value
    ) throws JsonProcessingException {
        BaseMstRemoteResponseDto<AreaRemoteDto> result = mstRemoteService.areaByCriteria(
                AreaRequest.builder()
                        .area(area)
                        .value(value)
                        .build()
        );

        return new CommonResult<List<AreaRemoteDto>>().success(result.getData());
    }
}
