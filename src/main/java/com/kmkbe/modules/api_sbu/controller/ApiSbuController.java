package com.kmkbe.modules.api_sbu.controller;


import com.kmkbe.helpers.base.BasePaginationRequest;
import com.kmkbe.helpers.base.BaseResponse;
import com.kmkbe.modules.api_sbu.model.request.ApiSbuRequest;
import com.kmkbe.modules.api_sbu.service.ApiSbuService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

/**
 * @author hyvercode
 * @date 8/6/26
 */

@RestController
@RequestMapping("/api/v1/api-sbu")
public class ApiSbuController {
  private final ApiSbuService apiSbuService;

  public ApiSbuController(ApiSbuService apiSbuService) {
    this.apiSbuService = apiSbuService;
  }

  @GetMapping(value ="",produces = MediaType.APPLICATION_JSON_VALUE)
  public BaseResponse getAllApiSbu(){
    return apiSbuService.all();
  }

  @GetMapping(value = "/pages", produces = MediaType.APPLICATION_JSON_VALUE)
  public BaseResponse getPageApiSbu(@Valid BasePaginationRequest request) {
    return apiSbuService.pages(request);
  }

  @GetMapping(value = "/pages/{bowheerCode}", produces = MediaType.APPLICATION_JSON_VALUE)
  public BaseResponse getPageApiSbu(@PathVariable("bowheerCode") String bowheerCode, @Valid BasePaginationRequest request) {
    return apiSbuService.pagesByBowheerCode(bowheerCode,request);
  }

  @PostMapping(value = "",consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  public BaseResponse postApiSbu(@Valid @RequestBody ApiSbuRequest request) {
    return apiSbuService.create(request);
  }

  @PutMapping(value = "/{id}",consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  public BaseResponse putApiSbu(@PathVariable("id") String id, @Valid @RequestBody ApiSbuRequest request) {
    return apiSbuService.update(id,request);
  }

  @GetMapping(value = "/{id}",produces = MediaType.APPLICATION_JSON_VALUE)
  public BaseResponse getApiSbu(@PathVariable("id") String id) {
    return apiSbuService.findById(id);
  }

  @DeleteMapping(value = "/{id}",produces = MediaType.APPLICATION_JSON_VALUE)
  public BaseResponse deleteApiSbu(@PathVariable("id") String id) {
    return apiSbuService.delete(id);
  }

}
