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
import com.kmkbe.modules.remote.request.ZipCodeRequest;
import com.kmkbe.modules.remote.service.MstRemoteService;
import io.netty.util.internal.StringUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
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


        BaseMstRemoteResponseDto<List<InputOptionsRemoteDto>> result = mstRemoteService.refMasterInputOption(
                name,
                modelDebiturMappingCode
        );

        return new CommonResult<List<InputOptionsRemoteDto>>().success(result.getData());
    }


    @GetMapping("/area")
    public CommonResult<List<AreaRemoteDto>> getArea(
            ZipCodeRequest request
    ) throws JsonProcessingException {
        List<AreaRemoteRequest> requests = new ArrayList<>();

        if (!StringUtil.isNullOrEmpty(request.getCity())) {
            requests.add(AreaRemoteRequest.builder()
                    .type(PropCriteriaGenericTypeRequest.AreaPropName.kota)
                    .value(request.getCity())
                    .build());
        }
        if (!StringUtil.isNullOrEmpty(request.getKecamatan())) {
            requests.add(AreaRemoteRequest.builder()
                    .type(PropCriteriaGenericTypeRequest.AreaPropName.kecamatan)
                    .value(request.getKecamatan())
                    .build());
        }
        if (!StringUtil.isNullOrEmpty(request.getKelurahan())) {
            requests.add(AreaRemoteRequest.builder()
                    .type(PropCriteriaGenericTypeRequest.AreaPropName.kelurahan)
                    .value(request.getKelurahan())
                    .build());
        }
        if (!StringUtil.isNullOrEmpty(request.getZipCode())) {
            requests.add(AreaRemoteRequest.builder()
                    .type(PropCriteriaGenericTypeRequest.AreaPropName.zipCode)
                    .value(request.getZipCode())
                    .build());
        }

        BaseMstRemoteResponseDto<List<AreaRemoteDto>> result2 = mstRemoteService.areaQuery(
                request.getPageNo(),
                request.getRowPerPage(),
                requests
        );

        return new CommonResult<List<AreaRemoteDto>>().success(result2.getData());
    }
}
