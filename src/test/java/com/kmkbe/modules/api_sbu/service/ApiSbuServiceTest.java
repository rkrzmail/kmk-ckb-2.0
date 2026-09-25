package com.kmkbe.modules.api_sbu.service;

import com.kmkbe.core.security.CurrentUserService;
import com.kmkbe.core.service.JwtGeneratorService;
import com.kmkbe.exception.BusinessException;
import com.kmkbe.helpers.base.BasePaginationRequest;
import com.kmkbe.helpers.base.BaseResponse;
import com.kmkbe.helpers.base.BaseResponseBuilder;
import com.kmkbe.helpers.constant.AppConstants;
import com.kmkbe.modules.api_sbu.model.entity.ApiSbu;
import com.kmkbe.modules.api_sbu.model.request.ApiSbuRequest;
import com.kmkbe.modules.api_sbu.model.response.ApiSbuResponse;
import com.kmkbe.modules.api_sbu.model.response.PageApiSbuResponse;
import com.kmkbe.modules.api_sbu.repository.ApiSbuRepository;
import com.kmkbe.modules.bouwheer.model.entity.Bouwheer;
import com.kmkbe.modules.bouwheer.repository.BouwheerRepository;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApiSbuServiceTest {

  private static final UUID BOUWHEER_CODE = UUID.fromString("11111111-1111-1111-1111-111111111111");
  private static final Date EXPIRED_DATE = Date.from(
      LocalDateTime.of(2099, 8, 12, 0, 0)
          .atZone(ZoneId.systemDefault())
          .toInstant()
  );

  @Mock
  private ApiSbuRepository apiSbuRepository;

  @Mock
  private BouwheerRepository bouwheerRepository;

  @Mock
  private JwtGeneratorService jwtGeneratorService;

  @Mock
  private CurrentUserService currentUserService;

  private ApiSbuService service;

  @BeforeEach
  void setUp() {
    service = new ApiSbuService(apiSbuRepository, bouwheerRepository, jwtGeneratorService, currentUserService);
  }

  @Test
  void allReturnsMappedApiSbuResponses() {
    ApiSbu first = apiSbu(1L, "First App");
    ApiSbu second = apiSbu(2L, "Second App");
    when(apiSbuRepository.findAll()).thenReturn(List.of(first, second));

    BaseResponseBuilder<List<ApiSbuResponse>> response = service.all();

    assertThat(response.isSuccess()).isTrue();
    assertThat(response.getData()).hasSize(2);
    assertThat(response.getData().get(0).getSesId()).isEqualTo(first.getSesId());
    assertThat(response.getData().get(0).getAppName()).isEqualTo(first.getAppName());
    assertThat(response.getData().get(0).getBouwheerCode()).isEqualTo(first.getBouwheerCode());
    assertThat(response.getData().get(0).getSesStatus()).isEqualTo(first.getSesStatus());
    assertThat(response.getData().get(0).getAppPath()).isEqualTo(first.getAppPath());
    assertThat(response.getData().get(0).getExpiredDate()).isEqualTo(first.getExpiredDate());
    assertThat(response.getData().get(0).getUsrCrt()).isEqualTo(first.getUsrCrt());
    assertThat(response.getData().get(0).getDtmCrt()).isEqualTo(first.getDtmCrt());
    assertThat(response.getData().get(0).getUsrUpd()).isEqualTo(first.getUsrUpd());
    assertThat(response.getData().get(0).getDtmUpd()).isEqualTo(first.getDtmUpd());
    assertThat(response.getData().get(1).getSesId()).isEqualTo(second.getSesId());
  }

  @Test
  void pagesUsesDefaultSortWhenSortByBlankAndReturnsMappedPage() {
    BasePaginationRequest request = paginationRequest(null, "ASC", "appName", "App");
    stubFindAllWithSpecification(pageOf(apiSbu(1L, "Paged App")));

    BaseResponseBuilder<PageApiSbuResponse> response = service.pages(request);

    assertThat(response.getData().getContent()).hasSize(1);
    assertThat(response.getData().getContent().get(0).getAppName()).isEqualTo("Paged App");
    assertThat(response.getData().getPagination().getCurrentPage()).isEqualTo(1);
  }

  @Test
  void pagesUsesRequestSortWhenSortByPresentAndReturnsMappedPage() {
    BasePaginationRequest request = paginationRequest("app_name", "DESC", "appName", "App");
    stubFindAllWithSpecification(pageOf(apiSbu(1L, "Paged App")));

    BaseResponseBuilder<PageApiSbuResponse> response = service.pages(request);

    assertThat(response.getData().getContent()).hasSize(1);
    assertThat(response.getData().getPagination().getTotalRecords()).isEqualTo(1);
  }

  @Test
  void pagesThrowsWhenSortByIsEmptyString() {
    BasePaginationRequest request = paginationRequest("", "ASC", "appName", "App");

    assertThatThrownBy(() -> service.pages(request))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void pagesByBowheerCodeAppliesSearchAndBouwheerFiltersWhenProvided() {
    BasePaginationRequest request = paginationRequest("appName", "ASC", "appName", "App");
    stubFindAllWithSpecification(pageOf(apiSbu(1L, "Filtered App")));

    BaseResponseBuilder<PageApiSbuResponse> response = service.pagesByBowheerCode(BOUWHEER_CODE.toString(), request);

    assertThat(response.getData().getContent()).hasSize(1);
    assertThat(response.getData().getContent().get(0).getAppName()).isEqualTo("Filtered App");
  }

  @Test
  void pagesByBowheerCodeSkipsSearchAndBouwheerFiltersWhenMissing() {
    BasePaginationRequest request = paginationRequest(null, "DESC", null, null);
    stubFindAllWithSpecification(pageOf(apiSbu(1L, "Unfiltered App")));

    BaseResponseBuilder<PageApiSbuResponse> response = service.pagesByBowheerCode("", request);

    assertThat(response.getData().getContent()).hasSize(1);
    assertThat(response.getData().getPagination().getTotalPages()).isEqualTo(1);
  }

  @Test
  void pagesByBowheerCodeSkipsSearchWhenSearchValueIsNullAndBouwheerCodeIsNull() {
    BasePaginationRequest request = paginationRequest("appName", "ASC", "appName", null);
    stubFindAllWithSpecification(pageOf(apiSbu(1L, "Null Search App")));

    BaseResponseBuilder<PageApiSbuResponse> response = service.pagesByBowheerCode(null, request);

    assertThat(response.getData().getContent()).hasSize(1);
  }

  @Test
  void pagesByBowheerCodeSkipsSearchWhenSearchValueIsEmpty() {
    BasePaginationRequest request = paginationRequest("appName", "ASC", "appName", "");
    stubFindAllWithSpecification(pageOf(apiSbu(1L, "Empty Search App")));

    BaseResponseBuilder<PageApiSbuResponse> response = service.pagesByBowheerCode(BOUWHEER_CODE.toString(), request);

    assertThat(response.getData().getContent()).hasSize(1);
  }

  @Test
  void pagesByBowheerCodeThrowsWhenSortByIsEmptyString() {
    BasePaginationRequest request = paginationRequest("", "ASC", "appName", "App");

    assertThatThrownBy(() -> service.pagesByBowheerCode(BOUWHEER_CODE.toString(), request))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void createSavesApiSbuWhenBouwheerExistsAndAppNameIsUnique() {
    ApiSbuRequest request = apiSbuRequest("Created App");
    when(bouwheerRepository.findByBouwheerCode(BOUWHEER_CODE)).thenReturn(Optional.of(Bouwheer.builder().build()));
    when(apiSbuRepository.findByBouwheerCodeAndAppName(BOUWHEER_CODE, request.getAppName())).thenReturn(Optional.empty());
    when(jwtGeneratorService.generateToken(anyString(), anyString(), eq(BOUWHEER_CODE.toString()), eq(EXPIRED_DATE)))
        .thenReturn("jwt-token");
    when(currentUserService.usernameOrDefault(AppConstants.CREATOR)).thenReturn(AppConstants.CREATOR);
    when(apiSbuRepository.save(any(ApiSbu.class))).thenAnswer(invocation -> {
      ApiSbu apiSbu = invocation.getArgument(0);
      apiSbu.setSesId(99L);
      return apiSbu;
    });

    BaseResponseBuilder<ApiSbuResponse> response = service.create(request);

    assertThat(response.getData().getSesId()).isEqualTo(99L);
    assertThat(response.getData().getAppName()).isEqualTo("Created App");
    assertThat(response.getData().getTokenJwt()).isEqualTo("jwt-token");
    assertThat(response.getData().getUsrCrt()).isEqualTo(AppConstants.CREATOR);
    assertThat(response.getData().getSesStatus()).isEqualTo("ACTIVE");
    assertThat(response.getData().getAppKey()).isNotBlank();
    assertThat(response.getData().getAppSecret()).isNotBlank();
  }

  @Test
  void createAllowsExistingDifferentAppName() {
    ApiSbuRequest request = apiSbuRequest("Created App");
    ApiSbu existingDifferentApp = apiSbu(1L, "Different App");
    when(bouwheerRepository.findByBouwheerCode(BOUWHEER_CODE)).thenReturn(Optional.of(Bouwheer.builder().build()));
    when(apiSbuRepository.findByBouwheerCodeAndAppName(BOUWHEER_CODE, request.getAppName()))
        .thenReturn(Optional.of(existingDifferentApp));
    when(jwtGeneratorService.generateToken(anyString(), anyString(), eq(BOUWHEER_CODE.toString()), eq(EXPIRED_DATE)))
        .thenReturn("jwt-token");
    when(currentUserService.usernameOrDefault(AppConstants.CREATOR)).thenReturn(AppConstants.CREATOR);
    when(apiSbuRepository.save(any(ApiSbu.class))).thenAnswer(invocation -> invocation.getArgument(0));

    BaseResponseBuilder<ApiSbuResponse> response = service.create(request);

    assertThat(response.getData().getAppName()).isEqualTo("Created App");
  }

  @Test
  void createThrowsWhenBouwheerDoesNotExist() {
    ApiSbuRequest request = apiSbuRequest("Created App");
    when(bouwheerRepository.findByBouwheerCode(BOUWHEER_CODE)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.create(request))
        .isInstanceOf(BusinessException.class);
  }

  @Test
  void createThrowsWhenAppNameAlreadyExistsForBouwheer() {
    ApiSbuRequest request = apiSbuRequest("Created App");
    when(bouwheerRepository.findByBouwheerCode(BOUWHEER_CODE)).thenReturn(Optional.of(Bouwheer.builder().build()));
    when(apiSbuRepository.findByBouwheerCodeAndAppName(BOUWHEER_CODE, request.getAppName()))
        .thenReturn(Optional.of(apiSbu(1L, "Created App")));

    assertThatThrownBy(() -> service.create(request))
        .isInstanceOf(BusinessException.class);
  }

  @Test
  void updateMutatesAndSavesExistingApiSbu() {
    ApiSbu existing = apiSbu(7L, "Old App");
    ApiSbuRequest request = apiSbuRequest("Updated App");
    request.setSesStatus("INACTIVE");
    request.setAppPath("/updated");
    when(apiSbuRepository.findById(7L)).thenReturn(Optional.of(existing));
    when(bouwheerRepository.findByBouwheerCode(BOUWHEER_CODE)).thenReturn(Optional.of(Bouwheer.builder().build()));

    BaseResponse response = service.update("7", request);

    assertThat(response).isInstanceOf(BaseResponseBuilder.class);
    ArgumentCaptor<ApiSbu> captor = ArgumentCaptor.forClass(ApiSbu.class);
    verify(apiSbuRepository).save(captor.capture());
    assertThat(captor.getValue().getSesStatus()).isEqualTo("INACTIVE");
    assertThat(captor.getValue().getAppPath()).isEqualTo("/updated");
    assertThat(captor.getValue().getAppName()).isEqualTo("Updated App");
  }

  @Test
  void updateThrowsWhenApiSbuDoesNotExist() {
    ApiSbuRequest request = apiSbuRequest("Updated App");
    when(apiSbuRepository.findById(7L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.update("7", request))
        .isInstanceOf(BusinessException.class);
  }

  @Test
  void updateThrowsWhenBouwheerDoesNotExist() {
    ApiSbuRequest request = apiSbuRequest("Updated App");
    when(apiSbuRepository.findById(7L)).thenReturn(Optional.of(apiSbu(7L, "Old App")));
    when(bouwheerRepository.findByBouwheerCode(BOUWHEER_CODE)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.update("7", request))
        .isInstanceOf(BusinessException.class);
  }

  @Test
  void findByIdReturnsMappedResponseWhenApiSbuExists() {
    ApiSbu apiSbu = apiSbu(5L, "Found App");
    when(apiSbuRepository.findById(5L)).thenReturn(Optional.of(apiSbu));

    BaseResponseBuilder<ApiSbuResponse> response = service.findById("5");

    assertThat(response.getData().getSesId()).isEqualTo(apiSbu.getSesId());
    assertThat(response.getData().getExpiredDate()).isEqualTo(apiSbu.getExpiredDate());
    assertThat(response.getData().getBouwheerCode()).isEqualTo(apiSbu.getBouwheerCode());
    assertThat(response.getData().getAppName()).isEqualTo(apiSbu.getAppName());
    assertThat(response.getData().getAppKey()).isEqualTo(apiSbu.getAppKey());
    assertThat(response.getData().getAppSecret()).isEqualTo(apiSbu.getAppSecret());
    assertThat(response.getData().getTokenJwt()).isEqualTo(apiSbu.getTokenJwt());
    assertThat(response.getData().getAppPath()).isEqualTo(apiSbu.getAppPath());
    assertThat(response.getData().getSesStatus()).isEqualTo(apiSbu.getSesStatus());
    assertThat(response.getData().getUsrCrt()).isEqualTo(apiSbu.getUsrCrt());
    assertThat(response.getData().getDtmCrt()).isEqualTo(apiSbu.getDtmCrt());
    assertThat(response.getData().getUsrUpd()).isEqualTo(apiSbu.getUsrUpd());
    assertThat(response.getData().getDtmUpd()).isEqualTo(apiSbu.getDtmUpd());
  }

  @Test
  void findByIdThrowsWhenApiSbuDoesNotExist() {
    when(apiSbuRepository.findById(5L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.findById("5"))
        .isInstanceOf(BusinessException.class);
  }

  @Test
  void deleteRemovesApiSbuWhenItExists() {
    ApiSbu apiSbu = apiSbu(8L, "Deleted App");
    when(apiSbuRepository.findById(8L)).thenReturn(Optional.of(apiSbu));

    BaseResponse response = service.delete("8");

    assertThat(response).isInstanceOf(BaseResponseBuilder.class);
    verify(apiSbuRepository).delete(apiSbu);
  }

  @Test
  void deleteThrowsWhenApiSbuDoesNotExist() {
    when(apiSbuRepository.findById(8L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.delete("8"))
        .isInstanceOf(BusinessException.class);
  }

  private static ApiSbu apiSbu(Long id, String appName) {
    return ApiSbu.builder()
        .sesId(id)
        .bouwheerCode(BOUWHEER_CODE)
        .tokenJwt("token-" + id)
        .usrCrt("creator")
        .dtmCrt(LocalDateTime.of(2026, 8, 12, 10, 0).plusMinutes(id))
        .usrUpd("updater")
        .dtmUpd(LocalDateTime.of(2026, 8, 12, 11, 0).plusMinutes(id))
        .expiredDate(LocalDateTime.of(2099, 8, 12, 0, 0))
        .sesStatus("ACTIVE")
        .appPath("/app-" + id)
        .appKey("key-" + id)
        .appSecret("secret-" + id)
        .appName(appName)
        .build();
  }

  private static ApiSbuRequest apiSbuRequest(String appName) {
    return ApiSbuRequest.builder()
        .bouwheerCode(BOUWHEER_CODE.toString())
        .expiredDate(EXPIRED_DATE)
        .sesStatus("ACTIVE")
        .appPath("/created")
        .appName(appName)
        .build();
  }

  private static BasePaginationRequest paginationRequest(
      String sortBy,
      String sortType,
      String searchBy,
      String searchValue
  ) {
    return new BasePaginationRequest(5, 1, sortBy, sortType, searchBy, searchValue);
  }

  private static Page<ApiSbu> pageOf(ApiSbu apiSbu) {
    return new PageImpl<>(List.of(apiSbu), Pageable.ofSize(5), 1);
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private void stubFindAllWithSpecification(Page<ApiSbu> page) {
    when(apiSbuRepository.findAll(ArgumentMatchers.<Specification<ApiSbu>>any(), any(Pageable.class)))
        .thenAnswer(invocation -> {
          Specification<ApiSbu> specification = invocation.getArgument(0);
          Root<ApiSbu> root = mock(Root.class);
          CriteriaQuery<?> query = mock(CriteriaQuery.class);
          CriteriaBuilder builder = mock(CriteriaBuilder.class);
          Path path = mock(Path.class);
          Predicate predicate = mock(Predicate.class);

          lenient().when(root.get(anyString())).thenReturn(path);
          lenient().when(builder.like(ArgumentMatchers.<Expression<String>>any(), anyString())).thenReturn(predicate);
          lenient().when(builder.equal(ArgumentMatchers.<Expression<?>>any(), any())).thenReturn(predicate);
          lenient().when(builder.and(any(Predicate[].class))).thenReturn(predicate);

          specification.toPredicate(root, query, builder);
          return page;
        });
  }
}
