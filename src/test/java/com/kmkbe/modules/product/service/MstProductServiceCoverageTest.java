package com.kmkbe.modules.product.service;

import com.kmkbe.modules.product.model.dto.ProductDto;
import com.kmkbe.modules.product.model.entity.Product;
import com.kmkbe.modules.product.repository.ProductRepository;
import com.kmkbe.core.security.CurrentUserService;
import com.kmkbe.helpers.base.BasePaginationRequest;
import com.kmkbe.modules.bouwheer.model.entity.Bouwheer;
import com.kmkbe.modules.bouwheer.repository.BouwheerRepository;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MstProductServiceCoverageTest {
  @Mock ProductRepository repository;
  @Mock BouwheerRepository bouwheerRepository;
  @Mock ProductExcelParser parser;
  @Mock CurrentUserService currentUserService;
  MstProductService service;

  @BeforeEach
  void setUp() {
    service = new MstProductService(repository, bouwheerRepository, parser, currentUserService);
  }

  @Test
  void allAndPagesBranches() {
    when(repository.findAll()).thenReturn(List.of(product()));
    assertThat(service.all().getCode()).isNotNull();

    when(repository.findAll(any(Specification.class), any(Pageable.class)))
      .thenReturn(new PageImpl<>(List.of(product())));
    BasePaginationRequest request = new BasePaginationRequest();
    request.setPageNo(2);
    request.setPageSize(5);
    request.setSortBy("bouwheerName");
    assertThat(service.pages(request)).isNotNull();

    request.setSearchValue("value");
    when(repository.findAll(any(Specification.class), any(Pageable.class)))
      .thenAnswer(call -> {
        evaluate(call.getArgument(0));
        return new PageImpl<>(List.of());
      });
    for (String field : List.of("bouwheerName", "bouwheerCode", "productId", "branchCode",
      "productCode", "productName")) {
      request.setSearchBy(field);
      request.setSearchValue("bouwheerCode".equals(field) ? UUID.randomUUID().toString()
        : "productId".equals(field) ? "1" : "value");
      assertThatCode(() -> service.pages(request)).doesNotThrowAnyException();
    }
    request.setSearchValue(null);
    assertThatCode(() -> service.pages(request)).doesNotThrowAnyException();
  }

  @Test
  void crudAndListBranches() {
    ProductDto dto = ProductDto.builder().productCode("P1").productName("Product")
      .branchCode("BR1").isActive(true).build();
    when(repository.findByProductCodeIgnoreCase("P1")).thenReturn(Optional.empty());
    when(currentUserService.usernameOrDefault(anyString())).thenReturn("admin");
    when(repository.save(any(Product.class))).thenAnswer(call -> call.getArgument(0));
    service.create(dto);
    assertThat(service.createProduct(dto).getProductCode()).isEqualTo("P1");

    Product existing = product();
    when(repository.findById(1L)).thenReturn(Optional.of(existing));
    service.update(1L, dto);
    assertThat(service.findById(1L)).isNotNull();
    when(repository.findById(2L)).thenReturn(Optional.empty());
    assertThatThrownBy(() -> service.update(2L, dto)).isInstanceOf(RuntimeException.class);
    assertThatThrownBy(() -> service.findById(2L)).isInstanceOf(RuntimeException.class);

    when(repository.findByProductCodeIgnoreCase("P1")).thenReturn(Optional.of(product()));
    assertThatThrownBy(() -> service.create(dto)).isInstanceOf(RuntimeException.class);

    UUID code = UUID.randomUUID();
    dto.setBouwheerCode(code);
    when(repository.findByProductCodeIgnoreCase("P1")).thenReturn(Optional.empty());
    when(bouwheerRepository.findByBouwheerCode(code)).thenReturn(Optional.empty());
    assertThatThrownBy(() -> service.create(dto)).isInstanceOf(RuntimeException.class);

    com.kmkbe.core.domain.request.PaginationRequest page = new com.kmkbe.core.domain.request.PaginationRequest();
    page.setPageNo(2);
    page.setPageSize(5);
    when(repository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(product())));
    assertThat(service.listProduct(page).getList()).hasSize(1);
    when(repository.findAllByProductId(any(Pageable.class), org.mockito.ArgumentMatchers.eq(1L)))
      .thenReturn(new PageImpl<>(List.of(product())));
    assertThat(service.listProductItem(page, 1L).getList()).hasSize(1);
  }

  @Test
  void uploadLastIdAndProductByCodeBranches() {
    MultipartFile file = mock(MultipartFile.class);
    when(parser.parse(file)).thenReturn(List.of(new ProductExcelParser.ProductRow(1L, "P1", "BR1",
      "Product", java.time.LocalDateTime.now(), 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, true)));
    service.uploadProduct(mock(jakarta.servlet.http.HttpServletRequest.class), file);
    verify(repository).saveAll(any());
    doThrow(new RuntimeException("parse failed")).when(parser).parse(file);
    assertThatThrownBy(() -> service.uploadProduct(mock(jakarta.servlet.http.HttpServletRequest.class), file))
      .isInstanceOf(RuntimeException.class);

    when(repository.findTopByOrderByProductIdDesc()).thenReturn(product());
    assertThat(service.getLastProductId()).isEqualTo(1L);
    when(repository.findTopByOrderByProductIdDesc()).thenReturn(null);
    assertThat(service.getLastProductId()).isNull();

    Product product = product();
    when(repository.findByProductCode("P1")).thenReturn(Optional.of(product), Optional.empty());
    assertThat(service.getProductByCode("P1").getStatusCode().value()).isEqualTo(200);
    assertThat(service.getProductByCode("P1").getStatusCode().value()).isEqualTo(404);
    when(repository.findByProductCode("P1")).thenReturn(Optional.of(product));
    assertThat(service.updateProduct("P1", product).getProductName()).isEqualTo("Product");
    assertThatThrownBy(() -> service.updateProduct("MISSING", product)).isInstanceOf(RuntimeException.class);
  }

  @Test
  void privateHelpers() {
    assertThat((Object) ReflectionTestUtils.invokeMethod(service, "toDto", product())).isNotNull();
    assertThat((Object) ReflectionTestUtils.invokeMethod(service, "resolveBouwheer", (Object) null)).isNull();
    assertThatCode(() -> ReflectionTestUtils.invokeMethod(service, "resolveProductSortBy", "bouwheerName"))
      .doesNotThrowAnyException();
  }


  @Test
  void remainingBranches() throws Exception {
    BasePaginationRequest empty = new BasePaginationRequest();
    when(repository.findAll(any(Specification.class), any(Pageable.class)))
      .thenReturn(new PageImpl<>(List.of()));
    assertThat(service.pages(empty)).isNotNull();
    empty.setSearchValue(" ");
    empty.setSearchBy(null);
    when(repository.findAll(any(Specification.class), any(Pageable.class)))
      .thenAnswer(call -> {
        evaluate(call.getArgument(0));
        return new PageImpl<>(List.of());
      });
    assertThat(service.pages(empty)).isNotNull();

    com.kmkbe.core.domain.request.PaginationRequest page = new com.kmkbe.core.domain.request.PaginationRequest();
    when(repository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of()));
    assertThat(service.listProduct(page).getList()).isEmpty();
    page.setPageNo(0);
    assertThat(service.listProduct(page).getList()).isEmpty();
    when(repository.findAllByProductId(any(Pageable.class), any())).thenReturn(new PageImpl<>(List.of()));
    assertThat(service.listProductItem(page, 1L).getList()).isEmpty();
    page.setPageNo(null);
    assertThat(service.listProductItem(page, 1L).getList()).isEmpty();

    ProductDto blank = ProductDto.builder().productCode(" ").build();
    when(repository.save(any(Product.class))).thenAnswer(call -> call.getArgument(0));
    assertThat(service.create(blank)).isNotNull();
    blank.setProductCode(null);
    assertThat(service.create(blank)).isNotNull();

    Product inactive = product();
    inactive.setIsActive(false);
    assertThat((Object) ReflectionTestUtils.invokeMethod(service, "toDto", inactive)).isNotNull();

    ProductDto dto = ProductDto.builder().productCode("P1").build();
    Product target = product();
    Product conflictSame = product();
    Product conflictOther = product();
    conflictOther.setProductId(2L);
    when(repository.findById(1L)).thenReturn(Optional.of(target));
    when(repository.findByProductCodeIgnoreCase("P1"))
      .thenReturn(Optional.of(conflictSame), Optional.of(conflictOther));
    assertThatCode(() -> service.update(1L, dto)).doesNotThrowAnyException();
    assertThatThrownBy(() -> service.update(1L, dto)).isInstanceOf(RuntimeException.class);
  }

  @Test
  void excelParserPrivateGetBranches() {
    ProductExcelParser realParser = new ProductExcelParser();
    Row row = mock(Row.class);

    Cell errorCell = mock(Cell.class);
    when(errorCell.getCellType()).thenReturn(CellType.ERROR);
    when(errorCell.getErrorCellValue()).thenReturn((byte) 1);
    when(row.getCell(0)).thenReturn(errorCell);
    assertThat((Object) ReflectionTestUtils.invokeMethod(realParser, "get", row, 0)).isNotNull();

    Cell formulaCell = mock(Cell.class);
    when(formulaCell.getCellType()).thenReturn(CellType.FORMULA);
    when(formulaCell.getCellFormula()).thenReturn("1+1");
    when(row.getCell(0)).thenReturn(formulaCell);
    assertThat((Object) ReflectionTestUtils.invokeMethod(realParser, "get", row, 0)).isEqualTo("1+1");

    Cell noneCell = mock(Cell.class);
    when(noneCell.getCellType()).thenReturn(CellType._NONE);
    when(row.getCell(0)).thenReturn(noneCell);
    assertThat((Object) ReflectionTestUtils.invokeMethod(realParser, "get", row, 0)).isEqualTo("");

    Cell blankCell = mock(Cell.class);
    when(blankCell.getCellType()).thenReturn(CellType.BLANK);
    when(row.getCell(0)).thenReturn(blankCell);
    assertThat((Object) ReflectionTestUtils.invokeMethod(realParser, "get", row, 0)).isNull();

    Cell stringCell = mock(Cell.class);
    when(stringCell.getCellType()).thenReturn(CellType.STRING);
    when(stringCell.getStringCellValue()).thenReturn("'abc");
    when(row.getCell(0)).thenReturn(stringCell);
    assertThat((Object) ReflectionTestUtils.invokeMethod(realParser, "get", row, 0)).isEqualTo("abc");
  }

  private void evaluate(Specification<Product> spec) {
    Root root = mock(Root.class);
    CriteriaBuilder builder = mock(CriteriaBuilder.class);
    Path path = mock(Path.class);
    Predicate predicate = mock(Predicate.class);
    lenient().when(root.get(anyString())).thenReturn(path);
    lenient().when(root.join(anyString(), any())).thenReturn(mock(jakarta.persistence.criteria.Join.class));
    lenient().when(builder.conjunction()).thenReturn(predicate);
    lenient().when(builder.like(any(), anyString())).thenReturn(predicate);
    lenient().when(builder.equal(any(), any())).thenReturn(predicate);
    lenient().when(builder.lower(any())).thenReturn(mock(jakarta.persistence.criteria.Expression.class));
    assertThatCode(() -> spec.toPredicate(root,
      mock(jakarta.persistence.criteria.CriteriaQuery.class), builder)).doesNotThrowAnyException();
  }

  private static Product product() {
    Product product = Product.builder().productId(1L).productCode("P1").productName("Product")
      .branchCode("BR1").isActive(true).build();
    product.setBouwheer(Bouwheer.builder().bouwheerCode(UUID.randomUUID()).bouwheerName("Bouwheer").build());
    return product;
  }
}
