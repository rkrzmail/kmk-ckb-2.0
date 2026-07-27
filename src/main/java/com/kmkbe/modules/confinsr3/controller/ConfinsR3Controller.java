package com.kmkbe.modules.confinsr3.controller;


import com.kmkbe.helpers.base.BaseResponse;
import com.kmkbe.modules.confinsr3.service.ConfinsR3Service;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/confins")
public class ConfinsR3Controller {
  private final ConfinsR3Service confinsR3Service;

  public ConfinsR3Controller(ConfinsR3Service confinsR3Service) {
    this.confinsR3Service = confinsR3Service;
  }

  @GetMapping(value ="/zipcode",produces = MediaType.APPLICATION_JSON_VALUE)
  public BaseResponse getZipcode(@RequestParam String zipcode){
    return confinsR3Service.findZipcode(zipcode);
  }
}
