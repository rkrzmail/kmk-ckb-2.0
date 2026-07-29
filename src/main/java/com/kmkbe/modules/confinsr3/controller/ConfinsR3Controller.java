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

  @GetMapping(value ="/zipcode/{zipcode}",produces = MediaType.APPLICATION_JSON_VALUE)
  public BaseResponse getZipcode(@PathVariable("zipcode") String zipcode){
    return confinsR3Service.findZipcode(zipcode);
  }

  @GetMapping(value ="/cwr-customers",produces = MediaType.APPLICATION_JSON_VALUE)
  public BaseResponse getCwrByCustomer(@RequestParam String value,Integer pageNo,Integer pageSize){
    return confinsR3Service.findCwrByCustomer(pageNo,pageSize,value);
  }

  @GetMapping(value ="/customers",produces = MediaType.APPLICATION_JSON_VALUE)
  public BaseResponse getByCustomer(@RequestParam String value,Integer pageNo,Integer pageSize){
    return confinsR3Service.findByCustomer(pageNo,pageSize,value);
  }

  @GetMapping(value ="/customers/{customerNo}",produces = MediaType.APPLICATION_JSON_VALUE)
  public BaseResponse getByCustomerNo(@PathVariable("customerNo") String customerNo){
    return confinsR3Service.findByCustomerNo(customerNo);
  }

  @GetMapping(value ="/masters/key-value/{typeCode}",produces = MediaType.APPLICATION_JSON_VALUE)
  public BaseResponse getKeyValueByCode(@PathVariable("typeCode") String typeCode){
    return confinsR3Service.findKeyValueByCode(typeCode);
  }

  @GetMapping(value ="/customers/company/{customerNo}",produces = MediaType.APPLICATION_JSON_VALUE)
  public BaseResponse getByCustomerNoCompany(@PathVariable("customerNo") String customerNo){
    return confinsR3Service.findByCustomerNoCompany(customerNo);
  }

  @GetMapping(value ="/customers/personal/{customerNo}",produces = MediaType.APPLICATION_JSON_VALUE)
  public BaseResponse getByCustomerNoPersonal(@PathVariable("customerNo") String customerNo){
    return confinsR3Service.findByCustomerNoPersonal(customerNo);
  }

}
