package com.kmkbe.modules.api_sbu.service;


import com.kmkbe.exception.BusinessException;
import com.kmkbe.helpers.base.BasePaginationRequest;
import com.kmkbe.helpers.base.BaseResponse;
import com.kmkbe.helpers.base.BaseResponseBuilder;
import com.kmkbe.helpers.constant.AppConstants;
import com.kmkbe.helpers.constant.ErrorConstant;
import com.kmkbe.helpers.utils.CommonUtils;
import com.kmkbe.helpers.utils.PageableUtil;
import com.kmkbe.modules.api_sbu.model.entity.ApiSbu;
import com.kmkbe.modules.api_sbu.model.request.ApiSbuRequest;
import com.kmkbe.modules.api_sbu.model.response.ApiSbuResponse;
import com.kmkbe.modules.api_sbu.model.response.PageApiSbuResponse;
import com.kmkbe.modules.api_sbu.repository.ApiSbuRepository;
import com.kmkbe.modules.bouwheer.model.entity.Bouwheer;
import com.kmkbe.modules.bouwheer.repository.BouwheerRepository;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * @author hyvercode
 * @date 8/6/26
 */
@Slf4j
@Service
public class ApiSbuService {
  private final Authentication authentication;
  private final ApiSbuRepository apiSbuRepository;
  private final BouwheerRepository bouwheerRepository;

  public ApiSbuService(Authentication authentication,
                       ApiSbuRepository apiSbuRepository,
                       BouwheerRepository bouwheerRepository) {
    this.authentication = authentication;
    this.apiSbuRepository = apiSbuRepository;
    this.bouwheerRepository = bouwheerRepository;
  }

  /**
   * Get All
   *
   * @return
   */
  public BaseResponseBuilder<List<ApiSbuResponse>> all() {
    Iterable<ApiSbu> products = apiSbuRepository.findAll();
    List<ApiSbuResponse> responses = new ArrayList<>();
    products.forEach(response -> responses.add(ApiSbuResponse.builder()
      .sesId(response.getSesId())
      .bouwheerCode(response.getBouwheerCode())
      .sesStatus(response.getSesStatus())
      .appPath(response.getAppPath())
      .appName(response.getAppName())
      .expiredDate(response.getExpiredDate())
      .usrCrt(response.getUsrCrt())
      .dtmCrt(response.getDtmCrt())
      .usrUpd(response.getUsrUpd())
      .dtmUpd(response.getDtmUpd())
      .build()));

    return new BaseResponseBuilder<>(true, AppConstants.CODE_OK, AppConstants.PROCESS_SUCCESSFULLY, responses);
  }

  /**
   * Page
   *
   * @param request
   * @return
   */
  public BaseResponseBuilder<PageApiSbuResponse> pages(
    BasePaginationRequest request) {
    String sortBy = request.getSortBy() != null && !request.getSortBy().isEmpty() ? request.getSortBy() : "dtmUpd";
    Pageable pageable = PageableUtil.createPageRequest(request, request.getPageSize(), request.getPageNo(),
      sortBy, request.getSortType());

    Page<ApiSbu> page = apiSbuRepository.findAll((Root<ApiSbu> root, CriteriaQuery<?> query, CriteriaBuilder builder) ->
      builder.and(builder.like(root.get(request.getSearchBy()), '%' + request.getSearchValue() + '%')), pageable);

    List<ApiSbuResponse> responses = page.getContent().stream().map(response -> ApiSbuResponse.builder()
      .sesId(response.getSesId())
      .bouwheerCode(response.getBouwheerCode())
      .sesStatus(response.getSesStatus())
      .appPath(response.getAppPath())
      .appName(response.getAppName())
      .expiredDate(response.getExpiredDate())
      .usrCrt(response.getUsrCrt())
      .dtmCrt(response.getDtmCrt())
      .usrUpd(response.getUsrUpd())
      .dtmUpd(response.getDtmUpd())
      .build()).toList();

    return new BaseResponseBuilder<>(true, AppConstants.CODE_OK, AppConstants.PROCESS_SUCCESSFULLY, PageApiSbuResponse.builder()
      .content(responses)
      .pagination(PageableUtil.pageToPagination(page))
      .build());
  }


  /**
   * Page By BouwheerCode
   *
   * @param request
   * @return
   */
  public BaseResponseBuilder<PageApiSbuResponse> pagesByBowheerCode(String bouwheerCode,
                                                                    BasePaginationRequest request) {
    String sortBy = request.getSortBy() != null && !request.getSortBy().isEmpty() ? request.getSortBy() : "dtmUpd";
    Pageable pageable = PageableUtil.createPageRequest(request, request.getPageSize(), request.getPageNo(),
      sortBy, request.getSortType());

    Page<ApiSbu> page = apiSbuRepository.findAll((Root<ApiSbu> root, CriteriaQuery<?> query, CriteriaBuilder builder) -> {
      List<Predicate> predicates = new ArrayList<>();

      // 1. Dynamic search by selected column
      if (request.getSearchBy() != null && request.getSearchValue() != null && !request.getSearchValue().isEmpty()) {
        predicates.add(builder.like(root.get(request.getSearchBy()), "%" + request.getSearchValue() + "%"));
      }

      // 2. Filter by bouwheerCode (Convert String to UUID)
      if (bouwheerCode != null && !bouwheerCode.isEmpty()) {
        // Convert the String from the request into a java.util.UUID
        UUID bouwheerUuid = UUID.fromString(bouwheerCode);
        predicates.add(builder.equal(root.get("bouwheerCode"), bouwheerUuid));
      }

      // Combine all predicates with AND
      return builder.and(predicates.toArray(new Predicate[0]));
    }, pageable);

    List<ApiSbuResponse> responses = page.getContent().stream().map(response -> ApiSbuResponse.builder()
      .sesId(response.getSesId())
      .bouwheerCode(response.getBouwheerCode())
      .sesStatus(response.getSesStatus())
      .appPath(response.getAppPath())
      .appName(response.getAppName())
      .expiredDate(response.getExpiredDate())
      .usrCrt(response.getUsrCrt())
      .dtmCrt(response.getDtmCrt())
      .usrUpd(response.getUsrUpd())
      .dtmUpd(response.getDtmUpd())
      .build()).toList();

    return new BaseResponseBuilder<>(true, AppConstants.CODE_OK, AppConstants.PROCESS_SUCCESSFULLY, PageApiSbuResponse.builder()
      .content(responses)
      .pagination(PageableUtil.pageToPagination(page))
      .build());
  }

  /**
   *
   * @param request
   * @return
   */
  public BaseResponseBuilder<ApiSbuResponse> create(ApiSbuRequest request) {
    /**
     * Find Bouwheer Code
     */
    Optional<Bouwheer> bouwheerOptional = bouwheerRepository.findByBouwheerCode(request.getBouwheerCode());
    if (bouwheerOptional.isEmpty()) {
      log.info(ErrorConstant.ERROR_MESSAGE_81 + "{}", request.getBouwheerCode());
      throw new BusinessException(HttpStatus.CONFLICT, ErrorConstant.ERROR_CODE_81, ErrorConstant.ERROR_MESSAGE_81 + "Bowheer code " + request.getBouwheerCode());
    }

    /**
     * Duplicate Appname
     */
    Optional<ApiSbu>  apiSbuOptional = apiSbuRepository.findByBouwheerCodeAndAppName(request.getBouwheerCode(), request.getAppName());
    if (apiSbuOptional.isPresent() && apiSbuOptional.get().getAppName().equals(request.getAppName())) {
      log.info(ErrorConstant.ERROR_MESSAGE_84 + "{}", request.getBouwheerCode());
      throw new BusinessException(HttpStatus.CONFLICT, ErrorConstant.ERROR_CODE_84, ErrorConstant.ERROR_MESSAGE_84 + "App Name" + request.getAppName());
    }

    var apiSbu = apiSbuRepository.save(ApiSbu.builder()
      .bouwheerCode(request.getBouwheerCode())
      .appKey(CommonUtils.generateAESKeyString())
      .appSecret(CommonUtils.generateUUIDString())
      .sesStatus("ACTIVE")
      .appPath(request.getAppPath())
      .appName(request.getAppName())
      .expiredDate(request.getExpiredDate())
      .usrCrt(authentication.getName())
      .dtmCrt(LocalDateTime.now())
      .build()
    );

    return new BaseResponseBuilder<>(true, AppConstants.CODE_OK, AppConstants.PROCESS_SUCCESSFULLY, ApiSbuResponse.builder()
      .bouwheerCode(apiSbu.getBouwheerCode())
      .appName(apiSbu.getAppName())
      .appKey(apiSbu.getAppKey())
      .appSecret(apiSbu.getAppSecret())
      .build());
  }

  /**
   *
   * @param id
   * @param request
   * @return
   */
  public BaseResponse update(String id, ApiSbuRequest request) {
    Optional<ApiSbu> apiSbuOptional = apiSbuRepository.findById(Long.valueOf(id));
    if (apiSbuOptional.isEmpty()) {
      log.info(ErrorConstant.ERROR_MESSAGE_81 + "{}", id);
      throw new BusinessException(HttpStatus.CONFLICT, ErrorConstant.ERROR_CODE_81, ErrorConstant.ERROR_MESSAGE_81 + "ID " + id);
    }


    Optional<Bouwheer> bouwheerOptional = bouwheerRepository.findByBouwheerCode(request.getBouwheerCode());
    if (bouwheerOptional.isEmpty()) {
      log.info(ErrorConstant.ERROR_MESSAGE_81 + "{}", request.getBouwheerCode());
      throw new BusinessException(HttpStatus.CONFLICT, ErrorConstant.ERROR_CODE_81, ErrorConstant.ERROR_MESSAGE_81 + "Bowheer code " + request.getBouwheerCode());
    }

    ApiSbu apiSbu = apiSbuOptional.get();
    apiSbu.setSesStatus(request.getSesStatus());
    apiSbu.setAppPath(request.getAppPath());
    apiSbu.setAppName(request.getAppName());
    apiSbuRepository.save(apiSbu);

    return new BaseResponseBuilder<>(true, AppConstants.CODE_OK, AppConstants.PROCESS_SUCCESSFULLY);
  }

  /**
   * Find By ID
   * @param id
   * @return
   */
  public BaseResponseBuilder<ApiSbuResponse> findById(String id) {

    Optional<ApiSbu> apiSbuOptional = apiSbuRepository.findById(Long.valueOf(id));
    if (apiSbuOptional.isEmpty()) {
      log.info(ErrorConstant.ERROR_MESSAGE_81 + "{}", id);
      throw new BusinessException(HttpStatus.CONFLICT, ErrorConstant.ERROR_CODE_81, ErrorConstant.ERROR_MESSAGE_81 + "ID " + id);
    }

    ApiSbu response = apiSbuOptional.get();


    return new BaseResponseBuilder<>(true, AppConstants.CODE_OK, AppConstants.PROCESS_SUCCESSFULLY,ApiSbuResponse.builder()
      .sesId(response.getSesId())
      .bouwheerCode(response.getBouwheerCode())
      .sesStatus(response.getSesStatus())
      .appPath(response.getAppPath())
      .appName(response.getAppName())
      .expiredDate(response.getExpiredDate())
      .appKey(response.getAppKey())
      .appSecret(response.getAppSecret())
      .usrCrt(response.getUsrCrt())
      .dtmCrt(response.getDtmCrt())
      .usrUpd(response.getUsrUpd())
      .dtmUpd(response.getDtmUpd())
      .build()
    );
  }

  /**
   * Delete By ID
   * @param id
   * @return
   */
  public BaseResponse delete(String id) {
    Optional<ApiSbu> apiSbuOptional = apiSbuRepository.findById(Long.valueOf(id));
    if (apiSbuOptional.isEmpty()) {
      log.info(ErrorConstant.ERROR_MESSAGE_81 + "{}", id);
      throw new BusinessException(HttpStatus.CONFLICT, ErrorConstant.ERROR_CODE_81, ErrorConstant.ERROR_MESSAGE_81 + "ID " + id);
    }
    apiSbuRepository.delete(apiSbuOptional.get());

    return new BaseResponseBuilder<>(true, AppConstants.CODE_OK, AppConstants.PROCESS_SUCCESSFULLY);
  }
}
