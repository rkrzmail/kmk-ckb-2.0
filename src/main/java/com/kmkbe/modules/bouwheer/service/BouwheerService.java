package com.kmkbe.modules.bouwheer.service;

import com.kmkbe.helpers.base.BaseResponseBuilder;
import com.kmkbe.helpers.constant.AppConstants;
import com.kmkbe.modules.bouwheer.model.entity.Bouwheer;
import com.kmkbe.modules.bouwheer.model.response.BouwheerResponse;
import com.kmkbe.modules.bouwheer.repository.BouwheerRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class BouwheerService {
  private final BouwheerRepository bouwheerRepository;

  public BouwheerService(BouwheerRepository bouwheerRepository) {
    this.bouwheerRepository = bouwheerRepository;
  }

  /**
   * Get all Bouwheers
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
      .build()));

    return new BaseResponseBuilder<>( true, AppConstants.CODE_OK, AppConstants.PROCESS_SUCCESSFULLY,bouwheerResponses);
  }
}
