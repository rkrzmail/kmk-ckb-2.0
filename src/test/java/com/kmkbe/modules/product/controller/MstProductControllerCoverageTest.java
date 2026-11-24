package com.kmkbe.modules.product.controller;

import com.kmkbe.core.domain.model.CommonResult;
import com.kmkbe.core.domain.model.PaginationResult;
import com.kmkbe.core.security.CurrentUserService;
import com.kmkbe.helpers.base.BasePaginationRequest;
import com.kmkbe.helpers.base.BaseResponseBuilder;
import com.kmkbe.modules.major_account.service.BranchAreaMappingService;
import com.kmkbe.modules.product.model.dto.ProductDto;
import com.kmkbe.modules.product.model.entity.Product;
import com.kmkbe.modules.product.service.MstProductService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MstProductControllerCoverageTest {
  @Mock MstProductService service;
  @Mock BranchAreaMappingService branchAreaMappingService;
  @Mock CurrentUserService currentUserService;

  @Test
  void delegatesAllEndpoints() throws Exception {
    MstProductController controller = new MstProductController(service, branchAreaMappingService, currentUserService);
    ProductDto dto = ProductDto.builder().productId(1L).productCode("P1").build();
    Product product = Product.builder().productId(1L).productCode("P1").build();
    when(service.all()).thenReturn(new BaseResponseBuilder<>(true, 200, "OK"));
    when(service.pages(any())).thenReturn(new BaseResponseBuilder<PaginationResult<ProductDto>>(true, 200, "OK"));
    when(service.create(any())).thenReturn(new BaseResponseBuilder<>(true, 200, "OK"));
    when(service.update(any(), any())).thenReturn(new BaseResponseBuilder<>(true, 200, "OK"));
    when(service.findById(any())).thenReturn(new BaseResponseBuilder<>(true, 200, "OK"));
    when(service.listProduct(any())).thenReturn(PaginationResult.empty(1));
    when(service.listProductItem(any(), any())).thenReturn(PaginationResult.empty(1));
    when(service.createProduct(any())).thenReturn(dto);
    when(service.getLastProductId()).thenReturn(1L);
    when(service.getProductByCode("P1")).thenReturn(ResponseEntity.ok(product));
    when(service.updateProduct("P1", product)).thenReturn(product);

    assertThat(controller.getAllProduct()).isNotNull();
    assertThat(controller.getPageProduct(new BasePaginationRequest())).isNotNull();
    assertThat(controller.postCreateProduct(dto)).isNotNull();
    assertThat(controller.putUpdateProduct(1L, dto)).isNotNull();
    assertThat(controller.getProduct(1L)).isNotNull();
    assertThat(controller.getList(new com.kmkbe.core.domain.request.PaginationRequest()).isSuccess()).isTrue();
    assertThat(controller.getListItem(new com.kmkbe.core.domain.request.PaginationRequest(), 1L).isSuccess()).isTrue();
    assertThat(controller.postUploadPlacementBranch(mock(HttpServletRequest.class),
      mock(MultipartFile.class)).isSuccess()).isTrue();
    assertThat(controller.createProduct(dto).isSuccess()).isTrue();
    ResponseEntity<Map<String, Long>> last = controller.getLastProductId();
    assertThat(last.getBody()).containsEntry("productId", 1L);
    assertThat(controller.getProductByCode("P1").getBody()).isSameAs(product);
    assertThat(controller.updateProduct("P1", product).getStatusCode().value()).isEqualTo(200);
    verify(service).uploadProduct(any(), any());
  }

  @Test
  void updateProductReturnsErrorResponse() {
    MstProductController controller = new MstProductController(service, branchAreaMappingService, currentUserService);
    Product product = Product.builder().productCode("P1").build();
    when(service.updateProduct("P1", product)).thenThrow(new RuntimeException("bad"));
    assertThat(controller.updateProduct("P1", product).getStatusCode().value()).isEqualTo(500);
  }
}
