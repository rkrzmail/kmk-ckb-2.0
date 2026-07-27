package com.kmkbe.modules.bouwheer.controller;

import com.kmkbe.modules.bouwheer.model.response.BouwheerResponse;
import com.kmkbe.modules.bouwheer.service.BouwheerService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/bouwheers")
public class BouwheerController {
  private final BouwheerService bouwheerService;

  public BouwheerController(BouwheerService bouwheerService) {
    this.bouwheerService = bouwheerService;
  }

  @GetMapping(value = "",produces = MediaType.APPLICATION_JSON_VALUE)
  public List<BouwheerResponse> getAll(){
    return bouwheerService.all();
  }

}
