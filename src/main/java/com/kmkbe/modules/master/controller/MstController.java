package com.kmkbe.modules.master.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.kmkbe.core.domain.dto.AreaRemoteDto;
import com.kmkbe.core.domain.dto.InputOptionsRemoteDto;
import com.kmkbe.core.domain.model.CommonResult;
import com.kmkbe.core.service.BaseRemoteService;
import com.kmkbe.helpers.base.BaseResponse;
import com.kmkbe.modules.confinsr3.model.request.ConfinsR3ZipcodeCriteriaRequest;
import com.kmkbe.modules.confinsr3.service.ConfinsR3Service;
import com.kmkbe.modules.master.service.MasterService;
import com.kmkbe.modules.remote.request.ZipCodeRequest;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/v1/mst")
public class MstController {
  private final MasterService masterService;
  private final ConfinsR3Service confinsR3Service;

  public MstController(MasterService masterService, ConfinsR3Service confinsR3Service) {
    this.masterService = masterService;
    this.confinsR3Service = confinsR3Service;
  }

  @GetMapping("/input-options/{name}")
  public CommonResult<List<InputOptionsRemoteDto>> getOptions(
    @PathVariable("name")
    BaseRemoteService.RefMasterTypeCode name,
    String type
  ) {
    return masterService.getOptions(name, type);
  }


  @GetMapping(value = "/area/pages", produces = MediaType.APPLICATION_JSON_VALUE)
  public BaseResponse getPageArea(Integer pageNo, Integer pageSize, List<ConfinsR3ZipcodeCriteriaRequest> requests) {
    return confinsR3Service.pageAllZipcode(pageNo, pageSize,requests);
  }

  @GetMapping(value = "/areas", produces = MediaType.APPLICATION_JSON_VALUE)
  public BaseResponse getAllArea(Integer pageNo, Integer pageSize) {
    return confinsR3Service.allZipcode(pageNo, pageSize);
  }

  @GetMapping("/area")
  public CommonResult<List<AreaRemoteDto>> getArea(ZipCodeRequest request) throws JsonProcessingException {
    return masterService.getArea(request);
  }
}
