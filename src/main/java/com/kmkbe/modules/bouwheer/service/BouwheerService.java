package com.kmkbe.modules.bouwheer.service;

import com.kmkbe.exception.BusinessException;
import com.kmkbe.helpers.base.BasePaginationRequest;
import com.kmkbe.helpers.base.BaseResponse;
import com.kmkbe.helpers.base.BaseResponseBuilder;
import com.kmkbe.helpers.constant.AppConstants;
import com.kmkbe.helpers.constant.ErrorConstant;
import com.kmkbe.helpers.utils.CommonUtils;
import com.kmkbe.helpers.utils.PageableUtil;
import com.kmkbe.modules.bouwheer.model.entity.Bouwheer;
import com.kmkbe.modules.bouwheer.model.request.BouwheerRequest;
import com.kmkbe.modules.bouwheer.model.response.BouwheerResponse;
import com.kmkbe.modules.bouwheer.model.response.PageBouwheerResponse;
import com.kmkbe.modules.bouwheer.repository.BouwheerRepository;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class BouwheerService {
  private final BouwheerRepository bouwheerRepository;

  public BouwheerService(BouwheerRepository bouwheerRepository) {
    this.bouwheerRepository = bouwheerRepository;
  }

  /**
   * Get all Bouwheers
   *
   * @return
   */
  public BaseResponseBuilder<List<BouwheerResponse>> all() {
    Iterable<Bouwheer> products = bouwheerRepository.findAll();
    List<BouwheerResponse> bouwheerResponses = new ArrayList<>();
    products.forEach(bouwheer -> bouwheerResponses.add(BouwheerResponse.builder()
      .bouwheerCode(bouwheer.getBouwheerCode())
      .bouwheerName(bouwheer.getBouwheerName())
      .legalAddress(bouwheer.getLegalAddress())
      .isActive(bouwheer.getIsActive())
      .usrCrt(bouwheer.getUsrCrt())
      .dtmCrt(bouwheer.getDtmCrt())
      .usrUpd(bouwheer.getUsrUpd())
      .dtmUpd(bouwheer.getDtmUpd())
      .build()));

    return new BaseResponseBuilder<>(true, AppConstants.CODE_OK, AppConstants.PROCESS_SUCCESSFULLY, bouwheerResponses);
  }

  /**
   *
   * @param request
   * @return
   */
  public BaseResponseBuilder<PageBouwheerResponse> pages(
    BasePaginationRequest request) {
    String sortBy = request.getSortBy() != null && !request.getSortBy().isEmpty() ? request.getSortBy() : "bouwheerName";
    Pageable pageable = PageableUtil.createPageRequest(request, request.getPageSize(), request.getPageNo(),
      sortBy, request.getSortType());

    Page<Bouwheer> page = bouwheerRepository.findAll((Root<Bouwheer> root, CriteriaQuery<?> query, CriteriaBuilder builder) ->
      builder.and(builder.like(root.get(request.getSearchBy()), '%' + request.getSearchValue() + '%')), pageable);

    List<BouwheerResponse> responses = page.getContent().stream().map(bouwheer -> BouwheerResponse.builder()
      .bouwheerCode(bouwheer.getBouwheerCode())
      .bouwheerName(bouwheer.getBouwheerName())
      .legalAddress(bouwheer.getLegalAddress())
      .isActive(bouwheer.getIsActive())
      .picName(bouwheer.getPicName())
      .picEmail(bouwheer.getPicEmail())
      .picMobilePhone(bouwheer.getPicMobilePhone())
      .usrCrt(bouwheer.getUsrCrt())
      .dtmCrt(bouwheer.getDtmCrt())
      .usrUpd(bouwheer.getUsrUpd())
      .dtmUpd(bouwheer.getDtmUpd())
      .build()).toList();

    return new BaseResponseBuilder<>(true, AppConstants.CODE_OK, AppConstants.PROCESS_SUCCESSFULLY, PageBouwheerResponse.builder()
      .content(responses)
      .pagination(PageableUtil.pageToPagination(page))
      .build());
  }

  /**
   * @param request
   * @return
   */
  public BaseResponse create(BouwheerRequest request) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    Optional<Bouwheer> bouwheerOptional = bouwheerRepository.findFirstByBouwheerName(request.getBouwheerName().toUpperCase());
    if (bouwheerOptional.isPresent()) {
      log.info(ErrorConstant.ERROR_MESSAGE_84 + "{}", request.getBouwheerName());
      throw new BusinessException(HttpStatus.CONFLICT, ErrorConstant.ERROR_CODE_84, ErrorConstant.ERROR_MESSAGE_84);
    }

    bouwheerRepository.save(Bouwheer.builder()
      .bouwheerCode(UUID.randomUUID())
      .bouwheerName(request.getBouwheerName().toUpperCase())
      .legalAddress(request.getLegalAddress())
      .rt(request.getRt())
      .rw(request.getRw())
      .kelurahan(request.getKelurahan())
      .kecamatan(request.getKecamatan())
      .city(request.getCity())
      .province(request.getProvince())
      .zipcode(request.getZipcode())
      .area(request.getArea())
      .phone(request.getPhone())
      .isSbu(request.getIsSbu())
      .picName(request.getPicName())
      .picEmail(request.getPicEmail())
      .picMobilePhone(request.getPicMobilePhone())
      .isWaActive(request.getIsWaActive())
      .termOfPayment(request.getTermOfPayment())
      .gracePeriod(request.getGracePeriod())
      .isActive(request.getIsActive())
      .usrCrt(authentication.getName())
      .aesKey(request.getAesKey())
      .secretKey(request.getSecretKey())
      .apiKey(request.getApiKey())
      .dtmCrt(LocalDateTime.now())
      .build());

    return new BaseResponseBuilder<>(true, AppConstants.CODE_OK, AppConstants.PROCESS_SUCCESSFULLY);
  }

  /**
   * @param request
   * @return
   */
  public BaseResponse update(String id, BouwheerRequest request) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    Optional<Bouwheer> bouwheerOptional = bouwheerRepository.findByBouwheerCode(UUID.fromString(id));
    if (bouwheerOptional.isEmpty()) {
      log.info(ErrorConstant.ERROR_MESSAGE_84 + "{}", request.getBouwheerName());
      throw new BusinessException(HttpStatus.CONFLICT, ErrorConstant.ERROR_CODE_84, ErrorConstant.ERROR_MESSAGE_84);
    }

    Bouwheer bouwheer = bouwheerOptional.get();
    BeanUtils.copyProperties(request, bouwheer);
    bouwheer.setUsrUpd(authentication.getName());
    bouwheer.setDtmUpd(LocalDateTime.now());
    bouwheerRepository.save(bouwheer);

    return new BaseResponseBuilder<>(true, AppConstants.CODE_OK, AppConstants.PROCESS_SUCCESSFULLY);
  }

  /**
   *
   * @param id
   * @return
   */
  public BaseResponse findById(String id) {
    Optional<Bouwheer> bouwheerOptional = bouwheerRepository.findByBouwheerCode(UUID.fromString(id));
    if (bouwheerOptional.isEmpty()) {
      log.info(ErrorConstant.ERROR_MESSAGE_84 + "{}", id);
      throw new BusinessException(HttpStatus.CONFLICT, ErrorConstant.ERROR_CODE_84, ErrorConstant.ERROR_MESSAGE_84);
    }

    Bouwheer bouwheer = bouwheerOptional.get();

    return new BaseResponseBuilder<>(true, AppConstants.CODE_OK, AppConstants.PROCESS_SUCCESSFULLY, BouwheerResponse.builder()
      .bouwheerCode(bouwheer.getBouwheerCode())
      .bouwheerName(bouwheer.getBouwheerName())
      .legalAddress(bouwheer.getLegalAddress())
      .isActive(bouwheer.getIsActive())
      .picName(bouwheer.getPicName())
      .picEmail(bouwheer.getPicEmail())
      .picMobilePhone(bouwheer.getPicMobilePhone())
      .rt(bouwheer.getRt())
      .rw(bouwheer.getRw())
      .kelurahan(bouwheer.getKelurahan())
      .kecamatan(bouwheer.getKecamatan())
      .city(bouwheer.getCity())
      .province(bouwheer.getProvince())
      .zipcode(bouwheer.getZipcode())
      .area(bouwheer.getArea())
      .phone(bouwheer.getPhone())
      .isSbu(bouwheer.getIsSbu())
      .isWaActive(bouwheer.getIsWaActive())
      .termOfPayment(bouwheer.getTermOfPayment())
      .gracePeriod(bouwheer.getGracePeriod())
      .usrCrt(bouwheer.getUsrCrt())
      .dtmCrt(bouwheer.getDtmCrt())
      .usrUpd(bouwheer.getUsrUpd())
      .dtmUpd(bouwheer.getDtmUpd())
      .aesKey(bouwheer.getAesKey())
      .secretKey(bouwheer.getSecretKey())
      .apiKey(bouwheer.getApiKey())
      .build());
  }

}
