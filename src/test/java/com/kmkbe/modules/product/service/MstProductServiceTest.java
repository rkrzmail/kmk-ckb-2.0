package com.kmkbe.modules.product.service;

import com.kmkbe.core.security.CurrentUserService;
import com.kmkbe.helpers.base.BasePaginationRequest;
import com.kmkbe.modules.bouwheer.repository.BouwheerRepository;
import com.kmkbe.modules.product.model.entity.Product;
import com.kmkbe.modules.product.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MstProductServiceTest {

  private final ProductRepository productRepository = mock(ProductRepository.class);
  private final MstProductService service = new MstProductService(
    productRepository,
    mock(BouwheerRepository.class),
    mock(ProductExcelParser.class),
    mock(CurrentUserService.class)
  );

  @Test
  void pagesSortsBouwheerNameUsingBouwheerRelation() {
    when(productRepository.findAll(any(Specification.class), any(Pageable.class)))
      .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 10), 0));

    service.pages(new BasePaginationRequest(10, 1, "bouwheerName", "DESC", null, null));

    ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
    verify(productRepository).findAll(any(Specification.class), pageableCaptor.capture());

    assertThat(pageableCaptor.getValue().getSort().getOrderFor("bouwheer.bouwheerName"))
      .isNotNull()
      .satisfies(order -> assertThat(order.isDescending()).isTrue());
  }
}
