package com.kmkbe.feign.client;

import com.kmkbe.core.domain.dto.*;
import com.kmkbe.modules.remote.request.*;
import java.util.List;
import java.util.Map;
import com.kmkbe.feign.config.ConfinsR3AuthInterceptor;
import com.kmkbe.feign.config.ConfinsR3ErrorDecoder;
import com.kmkbe.feign.model.dto.*;
import com.kmkbe.feign.model.request.*;
import com.kmkbe.feign.model.response.ConfinsR3ApiResponseWrapper;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
  name = "confinsR3FouFeignClient",
  url = "${feign.confins.url}",
  configuration = { ConfinsR3AuthInterceptor.class, ConfinsR3ErrorDecoder.class }
)
public interface ConfinsR3FeignClient {


  @PostMapping(value = "/api/mou/v1/Generic/GetPagingObjectBySQL",
    consumes = "application/json",
    produces = "application/json")
  ConfinsR3ApiResponseWrapper<ConfinsR3GetCwrCustomerDto> getCwrByCustomer(@RequestBody ConfinsR3GetPagingObjectBySQLRequest request);

  @PostMapping(value = "/api/fou/v1/RefZipcode/GetRefZipcodeByZipCode",
    consumes = "application/json",
    produces = "application/json")
  ConfinsR3GetZipCodeDto getZipcode(@RequestBody CsulGetZipCodeRequest request);

  @PostMapping(value = "/api/fou/v2/Generic/GetPagingObjectBySQL",
    consumes = "application/json",
    produces = "application/json")
  ConfinsR3ApiResponseWrapper<ConfinsR3GetCustomerDto> getByCustomer(@RequestBody ConfinsR3GetPagingObjectBySQLRequest request);

  @PostMapping(value = "/api/fou/v1/Cust/GetCustByCustNo",
    consumes = "application/json",
    produces = "application/json")
  ConfinsR3GetCustomerNoDto getByCustomerNo(@RequestBody ConfinsR3GetCustomerNoRequest request);

  @PostMapping(value = "/api/fou/v1/RefMaster/GetListKeyValueActiveByCode",
    consumes = "application/json",
    produces = "application/json")
  ConfinsR3GetKeyValueActiveByCodeDto getKyValueByCode(@RequestBody ConfinsR3GetKeyValueActiveByCodeRequest request);

  @PostMapping(value = "/api/fou/v1/Cust/GetCustCompanyForUpdateByCustNo",
    consumes = "application/json",
    produces = "application/json")
  ConfinsR3GetCustomerNoCompanyDto getByCustomerNoCompany(@RequestBody ConfinsR3GetCustomerNoRequest request);

  @PostMapping(value = "/api/fou/v1/Cust/GetCustPersonalForUpdateByCustNo",
    consumes = "application/json",
    produces = "application/json")
  ConfinsR3GetCustomerNoPersonalDto getByCustomerNoPersonal(@RequestBody ConfinsR3GetCustomerNoRequest request);

  @PostMapping(value = "/api/fou/v1/Generic/GetPagingObjectBySQL",
    consumes = "application/json",
    produces = "application/json"
  )
  ConfinsR3ApiResponseWrapper<ConfinsR3GetZipCodeDto> getAllZipcode(@RequestBody ConfinsR3GetPagingObjectBySQLRequest request);

  @PostMapping(value = "/api/fou/v1/CustObj/GetObjectByKeyAndValue",
    consumes = "application/json",
    produces = "application/json")
  CustomerRemoteDto getCustomerByKeyAndValue(@RequestBody ConfinsR3KeyAndValueObjRequest request);

  @PostMapping(value = "/api/los/v1/Application/GetAppByAppNo", consumes = "application/json", produces = "application/json")
  AppResponse getAppByAppNo(@RequestBody AppRequest request);

  @PostMapping(value = "/api/los/v1/DisbInfo/GetDisburseFctrByAppNo", consumes = "application/json", produces = "application/json")
  RekDebiturResponse getRekDebitur(@RequestBody RekDebiturRequest request);

  @PostMapping(value = "/api/corelos/v1/AppFctr/GetAppFctrByAppId", consumes = "application/json", produces = "application/json")
  AppFactoringResponse getAppFactoringData(@RequestBody AppFactoringRequest request);

  @PostMapping(value = "/api/corelos/v1/AgrmntFinData/GetFinancialDataByAgrmntNoForView", consumes = "application/json", produces = "application/json")
  FinancialDataResponse getFinancialData(@RequestBody FinancialDataRequest request);

  @PostMapping(value = "/api/mou/v1/CwrBouwheer/GetListCwrBouwheerByCwrNo", consumes = "application/json", produces = "application/json")
  CwrBwhrResponse getCwrBwhr(@RequestBody CwrBwhrRequest request);

  @PostMapping(value = "/api/mou/v1/CwrBouwheerProject/GetListCwrBouwheerProjectByCwrNoAndCwrBouwheerCustNo", consumes = "application/json", produces = "application/json")
  CwrListBwhrResponse getListCwrBwhr(@RequestBody CwrListBwhrRequest request);

  @PostMapping(value = "/api/mou/v1/CwrSigner/GetListCwrSignerForUpdatebyCustNoAndCwrNo", consumes = "application/json", produces = "application/json")
  SignerApiResponse getKaryawan(@RequestBody SignerRequestDto request);

  @PostMapping(value = "/api/mou/v1/CwrSigner/GetListCwrSignerForUpdatebyCustNoAndCwrNo", consumes = "application/json", produces = "application/json")
  ExternalApiResponse getSigners(@RequestBody SignerRequestDto request);

  @PostMapping(value = "/api/mou/v1/Generic/GetPagingObjectBySQL", consumes = "application/json", produces = "application/json")
  BaseMstRemoteResponseDto<List<InquiryCwrRemoteDto>> inquiryCwr(@RequestBody InquiryCwrCriteriaRemoteRequest request);

  @PostMapping(value = "/api/los/v1/Generic/GetPagingObjectBySQL", consumes = "application/json", produces = "application/json")
  BaseMstRemoteResponseDto<List<InquiryAgreementCwrDto>> inquiryAgreement(@RequestBody InquiryAgreementCriteriaRemoteRequest request);

  @PostMapping(value = "/api/los/v1/Agrmnt/GetListAgrmntDetailForCwrByCwrNo", consumes = "application/json", produces = "application/json")
  BaseMstRemoteResponseDto<List<InquiryAgreementByNoCwrRemoteDto>> inquiryAgreementByCwr(@RequestBody Map<String, Object> request);

  @PostMapping(value = "/api/fou/v1/Generic/GetPagingObjectBySQL", consumes = "application/json", produces = "application/json")
  BaseMstRemoteResponseDto<List<AreaRemoteDto>> areaQuery(@RequestBody AreaCriteriaRemoteRequest request);

  @PostMapping(value = "/api/fou/v1/CustObj/GetObjectByKeyAndValue", consumes = "application/json", produces = "application/json")
  CustomerRemoteDto validateExisting(@RequestBody ExistingCustomerRequest request);

}
