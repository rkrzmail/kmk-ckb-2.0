package com.kmkbe.modules.bouwheer.controller;

import com.kmkbe.helpers.base.BasePaginationRequest;
import com.kmkbe.helpers.base.BaseResponse;
import com.kmkbe.modules.bouwheer.model.request.BouwheerRequest;
import com.kmkbe.modules.bouwheer.service.BouwheerService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class BouwheerController {
  private final BouwheerService bouwheerService;

  public BouwheerController(BouwheerService bouwheerService) {
    this.bouwheerService = bouwheerService;
  }

  @GetMapping(value ="/public/v1/bouwheers",produces = MediaType.APPLICATION_JSON_VALUE)
  public BaseResponse getPublicAllBouwheer(){
    return bouwheerService.all();
  }

  @GetMapping(value ="/v1/bouwheers",produces = MediaType.APPLICATION_JSON_VALUE)
  public BaseResponse getAllBouwheer(){
    return bouwheerService.all();
  }

  @GetMapping(value = "/v1/bouwheers/pages", produces = MediaType.APPLICATION_JSON_VALUE)
  public BaseResponse getPageBouwheer(@Valid BasePaginationRequest request) {
    return bouwheerService.pages(request);
  }

  @PostMapping(value ="/v1/bouwheers",consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  public  BaseResponse postCreateBouwheer(@Valid @RequestBody BouwheerRequest request){
    return bouwheerService.create(request);
  }

  @PutMapping(value ="/v1/bouwheers/{id}",consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  public  BaseResponse postCreateBouwheer(@PathVariable String id,@Valid @RequestBody BouwheerRequest request){
    return bouwheerService.update(id,request);
  }

  @GetMapping(value ="/v1/bouwheers/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  public  BaseResponse getBouwheer(@PathVariable String id){
    return bouwheerService.findById(id);
  }

}
