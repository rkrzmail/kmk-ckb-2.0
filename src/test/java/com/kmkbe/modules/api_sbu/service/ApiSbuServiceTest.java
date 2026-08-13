package com.kmkbe.modules.api_sbu.service;

import com.kmkbe.core.security.CurrentUserService;
import com.kmkbe.core.service.JwtGeneratorService;
import com.kmkbe.exception.BusinessException;
import com.kmkbe.helpers.base.BasePaginationRequest;
import com.kmkbe.helpers.base.BaseResponse;
import com.kmkbe.helpers.base.BaseResponseBuilder;
import com.kmkbe.helpers.constant.AppConstants;
import com.kmkbe.helpers.constant.ErrorConstant;
import com.kmkbe.modules.api_sbu.model.entity.ApiSbu;
import com.kmkbe.modules.api_sbu.model.request.ApiSbuRequest;
import com.kmkbe.modules.api_sbu.model.response.ApiSbuResponse;
import com.kmkbe.modules.api_sbu.model.response.PageApiSbuResponse;
import com.kmkbe.modules.api_sbu.repository.ApiSbuRepository;
import com.kmkbe.modules.bouwheer.model.entity.Bouwheer;
import com.kmkbe.modules.bouwheer.repository.BouwheerRepository;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApiSbuServiceTest {

  @InjectMocks
  private ApiSbuService apiSbuService;

  @Mock
  private ApiSbuRepository apiSbuRepository;
  @Mock
  private BouwheerRepository bouwheerRepository;

  @Mock
  private JwtGeneratorService jwtGeneratorService;

  @Mock
  private Authentication authentication;

  @Mock
  private CurrentUserService currentUserService;

  private BasePaginationRequest basePaginationRequest;
  private ApiSbu apiSbu;
  private Bouwheer bouwheer;
  private BasePaginationRequest pageRequest;
  private Long mockID = 1l;

  @BeforeEach
  void setUp() {
    // Initialize common objects for tests
    basePaginationRequest = new BasePaginationRequest();
    basePaginationRequest.setPageNo(1);
    basePaginationRequest.setPageSize(10);

    apiSbu = ApiSbu.builder()
      .sesId(mockID)
      .bouwheerCode(UUID.fromString(UUID.randomUUID().toString()))
      .sesStatus("ACTIVE")
      .appPath("/test/path")
      .appName("NewApp")
      .expiredDate(LocalDateTime.now().plusYears(1))
      .usrCrt("user1")
      .dtmCrt(LocalDateTime.now().minusDays(1))
      .usrUpd("user2")
      .dtmUpd(LocalDateTime.now())
      .build();

    bouwheer = Bouwheer.builder()
      .bouwheerCode(UUID.fromString(UUID.randomUUID().toString()))
      .build();

    pageRequest = new BasePaginationRequest();
    pageRequest.setPageNo(1);
    pageRequest.setPageSize(10);
    pageRequest.setSortBy("appName");
    pageRequest.setSortType("ASC");
    pageRequest.setSearchBy("appName");
    pageRequest.setSearchValue("");

    // Set up security context for tests involving authentication name
    SecurityContextHolder.getContext().setAuthentication(authentication);
  }

  @Test
  void all_shouldReturnListOfApiSbuResponses() {
    // Arrange
    List<ApiSbu> apiSbuList = Arrays.asList(apiSbu, ApiSbu.builder().sesId(2L).build());
    when(apiSbuRepository.findAll()).thenReturn(apiSbuList);

    // Act
    BaseResponseBuilder<List<ApiSbuResponse>> result = apiSbuService.all();

    // Assert
    assertTrue(result.isSuccess());
    assertNotNull(result.getData());
    assertEquals(2, ((List<?>) result.getData()).size());

    // Verify structure of the first element (basic check)
    ApiSbuResponse firstResponse = (ApiSbuResponse) ((List<?>) result.getData()).get(0);
    assertEquals(1L, firstResponse.getSesId());
    assertEquals("ACTIVE", firstResponse.getSesStatus());
  }

  @Test
  void pages_shouldReturnPaginatedResult() {
    // Arrange
    Page<ApiSbu> page = new PageImpl<>(Collections.singletonList(apiSbu));

    // Mock search by default field (assuming request searchBy maps to a valid field or we mock repository behavior)
    when(apiSbuRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
    BaseResponseBuilder<PageApiSbuResponse> actual = apiSbuService.pages(
      pageRequest
    );
    // Act
    BaseResponseBuilder<PageApiSbuResponse> result = apiSbuService.pages(basePaginationRequest);

    // Assert
    assertTrue(result.isSuccess());
    PageApiSbuResponse pageApiResponse = (PageApiSbuResponse) result.getData();
    assertNotNull(pageApiResponse.getPagination());
    assertEquals(1, ((List<?>) pageApiResponse.getContent()).size());
    assertNotNull(actual);
  }

  @Test
  void pages_shouldReturnPaginatedByIDResult() {
    // Arrange
    Page<ApiSbu> page = new PageImpl<>(Collections.singletonList(apiSbu));

    // Mock search by default field (assuming request searchBy maps to a valid field or we mock repository behavior)
    when(apiSbuRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
    BaseResponseBuilder<PageApiSbuResponse> actual = apiSbuService.pagesByBowheerCode(String.valueOf(apiSbu.getBouwheerCode()),
      pageRequest
    );
    // Act
    BaseResponseBuilder<PageApiSbuResponse> result = apiSbuService.pagesByBowheerCode(String.valueOf(apiSbu.getBouwheerCode()), basePaginationRequest);

    // Assert
    assertTrue(result.isSuccess());
    PageApiSbuResponse pageApiResponse = (PageApiSbuResponse) result.getData();
    assertNotNull(pageApiResponse.getPagination());
    assertEquals(1, ((List<?>) pageApiResponse.getContent()).size());
    assertNotNull(actual);
  }

  @Test
  void pages_shouldReturnPaginatedByIDResultSearch() {
    // Arrange
    Page<ApiSbu> page = new PageImpl<>(Collections.singletonList(apiSbu));
    pageRequest.setSearchValue("appName");
    // Mock search by default field (assuming request searchBy maps to a valid field or we mock repository behavior)
    when(apiSbuRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
    BaseResponseBuilder<PageApiSbuResponse> actual = apiSbuService.pagesByBowheerCode(String.valueOf(apiSbu.getBouwheerCode()),
      pageRequest
    );
    // Act
    BaseResponseBuilder<PageApiSbuResponse> result = apiSbuService.pagesByBowheerCode(String.valueOf(apiSbu.getBouwheerCode()), basePaginationRequest);

    // Assert
    assertTrue(result.isSuccess());
    PageApiSbuResponse pageApiResponse = (PageApiSbuResponse) result.getData();
    assertNotNull(pageApiResponse.getPagination());
    assertEquals(1, ((List<?>) pageApiResponse.getContent()).size());
    assertNotNull(actual);
  }

  @Test
  void pages_shouldReturnPaginatedByIDResultSearchByBouwheerCode() {
    // Arrange
    Page<ApiSbu> page = new PageImpl<>(Collections.singletonList(apiSbu));
    pageRequest.setSearchBy("bouwheerCode");
    pageRequest.setSearchValue("3059843583476");
    // Mock search by default field (assuming request searchBy maps to a valid field or we mock repository behavior)
    when(apiSbuRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
    BaseResponseBuilder<PageApiSbuResponse> actual = apiSbuService.pagesByBowheerCode(String.valueOf(apiSbu.getBouwheerCode()),
      pageRequest
    );
    // Act
    BaseResponseBuilder<PageApiSbuResponse> result = apiSbuService.pagesByBowheerCode(String.valueOf(apiSbu.getBouwheerCode()), basePaginationRequest);

    // Assert
    assertTrue(result.isSuccess());
    PageApiSbuResponse pageApiResponse = (PageApiSbuResponse) result.getData();
    assertNotNull(pageApiResponse.getPagination());
    assertEquals(1, ((List<?>) pageApiResponse.getContent()).size());
    assertNotNull(actual);
  }

  @Test
  void create_shouldSuccessfullyCreateAndReturnApiSbuResponse() {
    // Arrange
    String bouwheerCodeStr = UUID.randomUUID().toString();
    LocalDateTime expiredDate = LocalDateTime.now().plusYears(1);
    ApiSbuRequest request = new ApiSbuRequest();
    request.setBouwheerCode(bouwheerCodeStr);
    request.setAppName("NewApp");
    request.setAppPath("/new/path");
    request.setExpiredDate(new Date());

    // Mock Bouwheer existence check
    when(bouwheerRepository.findByBouwheerCode(any())).thenReturn(Optional.of(bouwheer));
    // Mock unique AppName check (should not exist)
    when(apiSbuRepository.findByBouwheerCodeAndAppName(eq(UUID.fromString(bouwheerCodeStr)), eq("NewApp"))).thenReturn(Optional.empty());

    // Mock Repository save operation
    ApiSbu savedApiSbu = ApiSbu.builder()
      .sesId(99L)
      .bouwheerCode(UUID.fromString(bouwheerCodeStr))
      .appKey("key")
      .appSecret("secret")
      .tokenJwt("MOCKED_JWT")
      .sesStatus("ACTIVE")
      .appPath("/new/path")
      .appName("NewApp")
      .expiredDate(expiredDate.toLocalDate().atStartOfDay())
      .usrCrt("testUser") // From security context setup
      .dtmCrt(LocalDateTime.now())
      .usrUpd("testUser")
      .dtmUpd(LocalDateTime.now())
      .build();
    when(apiSbuRepository.save(any(ApiSbu.class))).thenReturn(savedApiSbu);

    // Mock JWT generation (to avoid dependency issues)
    when(jwtGeneratorService.generateToken(anyString(), anyString(), anyString(), any())).thenReturn("MOCKED_JWT");

    // Act
    BaseResponseBuilder<ApiSbuResponse> result = apiSbuService.create(request);

    // Assert
    assertTrue(result.isSuccess());
    ApiSbuResponse response = (ApiSbuResponse) result.getData();
    assertNotNull(response);
    assertEquals("MOCKED_JWT", response.getTokenJwt());
    assertEquals(99L, response.getSesId());

    verify(bouwheerRepository, times(1)).findByBouwheerCode(any());
  }


  @Test
  void create_shouldThrowBusinessExceptionIfBouwheerNotFound() {
    String bouwheerCodeStr = UUID.randomUUID().toString();
    ApiSbuRequest request = new ApiSbuRequest();
    request.setBouwheerCode(bouwheerCodeStr);
    request.setAppName("NewApp");
    request.setAppPath("/new/path");
    request.setExpiredDate(new Date());
    // Mock Bouwheer not found
    when(bouwheerRepository.findByBouwheerCode(any())).thenReturn(Optional.empty());

    var ex = assertThrows(BusinessException.class, () -> apiSbuService.create(request));
    assertEquals(ErrorConstant.ERROR_CODE_81, ex.getCode());
  }

  @Test
  void create_shouldThrowBusinessExceptionIFDuplicateName() {
    String bouwheerCodeStr = UUID.randomUUID().toString();
    ApiSbuRequest request = new ApiSbuRequest();
    request.setBouwheerCode(bouwheerCodeStr);
    request.setAppName("NewApp");
    request.setAppPath("/new/path");
    request.setExpiredDate(new Date());

    // Mock Bouwheer not found
    when(bouwheerRepository.findByBouwheerCode(any())).thenReturn(Optional.of(bouwheer));
    when(apiSbuRepository.findByBouwheerCodeAndAppName(eq(UUID.fromString(bouwheerCodeStr)), eq("NewApp"))).thenReturn(Optional.of(apiSbu));

    var ex = assertThrows(BusinessException.class, () -> apiSbuService.create(request));
    assertEquals(ErrorConstant.ERROR_CODE_84, ex.getCode());
  }

  @Test
  void update_shouldSuccessfullyUpdateApiSbu() {
    // Arrange
    String id = "1";
    String newBouwheerCodeStr = UUID.randomUUID().toString();
    ApiSbuRequest request = new ApiSbuRequest();
    request.setSesStatus("INACTIVE");
    request.setAppPath("/updated/path");
    request.setAppName("UpdatedName");
    request.setBouwheerCode(newBouwheerCodeStr);
    request.setExpiredDate(new Date());


    // Mock initial state
    when(apiSbuRepository.findById(Long.valueOf(id))).thenReturn(Optional.of(apiSbu));
    // Mock Bouwheer existence check (needs to pass)
    when(bouwheerRepository.findByBouwheerCode(any())).thenReturn(Optional.of(bouwheer));

    // Act
    BaseResponseBuilder<ApiSbuResponse> response = apiSbuService.update(id, request);

    // Assert
    assertEquals(AppConstants.CODE_OK, response.getCode());
    assertEquals(AppConstants.PROCESS_SUCCESSFULLY, response.getMessage());
    verify(apiSbuRepository, times(1)).save(any(ApiSbu.class));
  }

  @Test
  void update_shouldThrowBusinessExceptionIfNotFound() {
    String id = "1";
    String bouwheerCodeStr = UUID.randomUUID().toString();
    ApiSbuRequest request = new ApiSbuRequest();
    request.setBouwheerCode(bouwheerCodeStr);
    request.setAppName("NewApp");
    request.setAppPath("/new/path");
    request.setExpiredDate(new Date());
    // Mock Bouwheer not found
    when(apiSbuRepository.findById(any())).thenReturn(Optional.empty());

    var ex = assertThrows(BusinessException.class, () -> apiSbuService.update(id, request));
    assertEquals(ErrorConstant.ERROR_CODE_81, ex.getCode());
  }

  @Test
  void update_shouldThrowBusinessExceptionIfBouwheerNotFound() {
    String id = "1";
    String bouwheerCodeStr = UUID.randomUUID().toString();
    ApiSbuRequest request = new ApiSbuRequest();
    request.setBouwheerCode(bouwheerCodeStr);
    request.setAppName("NewApp");
    request.setAppPath("/new/path");
    request.setExpiredDate(new Date());
    // Mock Bouwheer not found
    when(apiSbuRepository.findById(any())).thenReturn(Optional.of(apiSbu));
    // Mock Bouwheer existence check (needs to pass)
    when(bouwheerRepository.findByBouwheerCode(any())).thenReturn(Optional.empty());

    var ex = assertThrows(BusinessException.class, () -> apiSbuService.update(id, request));
    assertEquals(ErrorConstant.ERROR_CODE_81, ex.getCode());
  }

  @Test
  void findById_shouldReturnApiSbuResponse() {
    // Arrange
    String id = "1";

    when(apiSbuRepository.findById(Long.valueOf(id))).thenReturn(Optional.of(apiSbu));

    // Act
    BaseResponseBuilder<ApiSbuResponse> result = apiSbuService.findById(id);

    // Assert
    assertTrue(result.isSuccess());
    assertNotNull(result.getData());
  }


  @Test
  void findById_shouldThrowBusinessExceptionIfNotFound() {
    // Arrange
    String nonExistentId = "999";
    when(apiSbuRepository.findById(Long.valueOf(nonExistentId))).thenReturn(Optional.empty());

    // Act & Assert
    var ex = assertThrows(BusinessException.class, () -> {
      apiSbuService.findById(nonExistentId);
    });
    assertEquals(ErrorConstant.ERROR_MESSAGE_81, ex.getMessage());
  }

  @Test
  void delete_shouldSuccessfullyDeleteApiSbu() {
    // Arrange
    String id = "1";

    when(apiSbuRepository.findById(Long.valueOf(id))).thenReturn(Optional.of(apiSbu));
    // Act
    BaseResponse response = apiSbuService.delete(id);

    // Assert
    assertNotNull(response);
    verify(apiSbuRepository, times(1)).delete(any(ApiSbu.class));
  }


  @Test
  void delete_shouldThrowBusinessExceptionIfNotFound() {
    // Arrange
    String nonExistentId = "999";

    when(apiSbuRepository.findById(Long.valueOf(nonExistentId))).thenReturn(Optional.empty());

    // Act & Assert
    var ex = assertThrows(BusinessException.class, () -> {
      apiSbuService.delete(nonExistentId);
    });

    assertEquals(ErrorConstant.ERROR_CODE_81, ex.getCode());
  }
}
