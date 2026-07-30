package com.kmkbe.modules.master.service;

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
import com.kmkbe.modules.user.utils.Utils;
import io.netty.util.internal.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class MasterService {
  private final MstRemoteService mstRemoteService;

  public MasterService(MstRemoteService mstRemoteService) {
    this.mstRemoteService = mstRemoteService;
  }

  /**
   *
   * @param name
   * @param type
   * @return
   */
  public CommonResult<List<InputOptionsRemoteDto>> getOptions(BaseRemoteService.RefMasterTypeCode name, String type) {
    RefMasterRequest.ModelDebiturMappingCode modelDebiturMappingCode = null;

    if (type != null) {
      if (!type.equalsIgnoreCase("PERSONAL") && !type.equalsIgnoreCase("COMPANY")) {
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


    if (result2 != null) {
      return new CommonResult<List<AreaRemoteDto>>().successWithCount(result2.getData(), Utils.getInt(String.valueOf(result2.getCount())));
    } else {
      result2 = mstRemoteService.dummyZipCode();
      return new CommonResult<List<AreaRemoteDto>>().successWithCount(result2.getData(), Utils.getInt(String.valueOf(result2.getCount())));
    }

  }
}
