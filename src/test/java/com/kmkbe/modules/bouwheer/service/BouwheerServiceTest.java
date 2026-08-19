package com.kmkbe.modules.bouwheer.service;

import com.kmkbe.core.security.CurrentUserService;
import com.kmkbe.exception.BusinessException;
import com.kmkbe.helpers.base.BasePaginationRequest;
import com.kmkbe.helpers.base.BaseResponse;
import com.kmkbe.helpers.base.BaseResponseBuilder;
import com.kmkbe.helpers.constant.AppConstants;
import com.kmkbe.modules.common.service.AuditTrailService;
import com.kmkbe.modules.bouwheer.model.entity.Bouwheer;
import com.kmkbe.modules.bouwheer.model.request.BouwheerRequest;
import com.kmkbe.modules.bouwheer.model.response.BouwheerResponse;
import com.kmkbe.modules.bouwheer.model.response.PageBouwheerResponse;
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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BouwheerServiceTest {

  private static final UUID BOUWHEER_CODE = UUID.fromString("11111111-1111-1111-1111-111111111111");

  @Mock
  private BouwheerRepository bouwheerRepository;

  @Mock
  private CurrentUserService currentUserService;

  @Mock
  private AuditTrailService auditTrailService;

  private BouwheerService service;

  @BeforeEach
  void setUp() {
    service = new BouwheerService(bouwheerRepository, currentUserService, auditTrailService);
    lenient().when(bouwheerRepository.save(any(Bouwheer.class))).thenAnswer(invocation -> invocation.getArgument(0));
  }

  @Test
  void allReturnsMappedBouwheerResponses() {
    Bouwheer first = bouwheer(BOUWHEER_CODE, "PT FIRST");
    Bouwheer second = bouwheer(UUID.fromString("22222222-2222-2222-2222-222222222222"), "PT SECOND");
    when(bouwheerRepository.findAll()).thenReturn(List.of(first, second));

    BaseResponseBuilder<List<BouwheerResponse>> response = service.all();

    assertThat(response.isSuccess()).isTrue();
    assertThat(response.getData()).hasSize(2);
    assertThat(response.getData().get(0).getBouwheerCode()).isEqualTo(first.getBouwheerCode());
    assertThat(response.getData().get(0).getBouwheerName()).isEqualTo(first.getBouwheerName());
    assertThat(response.getData().get(0).getLegalAddress()).isEqualTo(first.getLegalAddress());
    assertThat(response.getData().get(0).getIsActive()).isEqualTo(first.getIsActive());
    assertThat(response.getData().get(0).getUsrCrt()).isEqualTo(first.getUsrCrt());
    assertThat(response.getData().get(0).getDtmCrt()).isEqualTo(first.getDtmCrt());
    assertThat(response.getData().get(0).getUsrUpd()).isEqualTo(first.getUsrUpd());
    assertThat(response.getData().get(0).getDtmUpd()).isEqualTo(first.getDtmUpd());
    assertThat(response.getData().get(1).getBouwheerCode()).isEqualTo(second.getBouwheerCode());
  }

  @Test
  void pagesUsesDefaultSortWhenSortByIsNullAndReturnsMappedPage() {
    BasePaginationRequest request = paginationRequest(null, "ASC", "bouwheerName", "PT");
    stubFindAllWithSpecification(pageOf(bouwheer(BOUWHEER_CODE, "PT PAGE")));

    BaseResponseBuilder<PageBouwheerResponse> response = service.pages(request);

    assertThat(response.getData().getContent()).hasSize(1);
    assertThat(response.getData().getContent().get(0).getBouwheerName()).isEqualTo("PT PAGE");
    assertThat(response.getData().getContent().get(0).getPicName()).isEqualTo("PIC Name");
    assertThat(response.getData().getContent().get(0).getPicEmail()).isEqualTo("pic@example.com");
    assertThat(response.getData().getContent().get(0).getPicMobilePhone()).isEqualTo("08123");
    assertThat(response.getData().getPagination().getCurrentPage()).isEqualTo(1);
  }

  @Test
  void pagesUsesRequestSortWhenSortByIsPresentAndReturnsMappedPage() {
    BasePaginationRequest request = paginationRequest("bouwheer_name", "DESC", "bouwheerName", "PT");
    stubFindAllWithSpecification(pageOf(bouwheer(BOUWHEER_CODE, "PT PAGE")));

    BaseResponseBuilder<PageBouwheerResponse> response = service.pages(request);

    assertThat(response.getData().getContent()).hasSize(1);
    assertThat(response.getData().getPagination().getTotalRecords()).isEqualTo(1);
  }

  @Test
  void pagesThrowsWhenSortByIsEmptyString() {
    BasePaginationRequest request = paginationRequest("", "ASC", "bouwheerName", "PT");

    assertThatThrownBy(() -> service.pages(request))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void createSavesUppercaseBouwheerWhenNameIsUnique() {
    BouwheerRequest request = bouwheerRequest("pt new");
    when(bouwheerRepository.findFirstByBouwheerName("PT NEW")).thenReturn(Optional.empty());
    when(currentUserService.usernameOrDefault(AppConstants.CREATOR)).thenReturn(AppConstants.CREATOR);

    BaseResponse response = service.create(request);

    assertThat(response).isInstanceOf(BaseResponseBuilder.class);
    ArgumentCaptor<Bouwheer> captor = ArgumentCaptor.forClass(Bouwheer.class);
    verify(bouwheerRepository).save(captor.capture());
    Bouwheer saved = captor.getValue();
    assertThat(saved.getBouwheerCode()).isNotNull();
    assertThat(saved.getBouwheerName()).isEqualTo("PT NEW");
    assertThat(saved.getLegalAddress()).isEqualTo(request.getLegalAddress());
    assertThat(saved.getRt()).isEqualTo(request.getRt());
    assertThat(saved.getRw()).isEqualTo(request.getRw());
    assertThat(saved.getKelurahan()).isEqualTo(request.getKelurahan());
    assertThat(saved.getKecamatan()).isEqualTo(request.getKecamatan());
    assertThat(saved.getCity()).isEqualTo(request.getCity());
    assertThat(saved.getProvince()).isEqualTo(request.getProvince());
    assertThat(saved.getZipcode()).isEqualTo(request.getZipcode());
    assertThat(saved.getArea()).isEqualTo(request.getArea());
    assertThat(saved.getPhone()).isEqualTo(request.getPhone());
    assertThat(saved.getIsSbu()).isEqualTo(request.getIsSbu());
    assertThat(saved.getPicName()).isEqualTo(request.getPicName());
    assertThat(saved.getPicEmail()).isEqualTo(request.getPicEmail());
    assertThat(saved.getPicMobilePhone()).isEqualTo(request.getPicMobilePhone());
    assertThat(saved.getIsWaActive()).isEqualTo(request.getIsWaActive());
    assertThat(saved.getTermOfPayment()).isEqualTo(request.getTermOfPayment());
    assertThat(saved.getGracePeriod()).isEqualTo(request.getGracePeriod());
    assertThat(saved.getIsActive()).isEqualTo(request.getIsActive());
    assertThat(saved.getUsrCrt()).isEqualTo(AppConstants.CREATOR);
    assertThat(saved.getAesKey()).isEqualTo(request.getAesKey());
    assertThat(saved.getSecretKey()).isEqualTo(request.getSecretKey());
    assertThat(saved.getApiKey()).isEqualTo(request.getApiKey());
    assertThat(saved.getDtmCrt()).isNotNull();
  }

  @Test
  void createThrowsWhenBouwheerNameAlreadyExists() {
    BouwheerRequest request = bouwheerRequest("pt duplicate");
    when(bouwheerRepository.findFirstByBouwheerName("PT DUPLICATE"))
        .thenReturn(Optional.of(bouwheer(BOUWHEER_CODE, "PT DUPLICATE")));

    assertThatThrownBy(() -> service.create(request))
        .isInstanceOf(BusinessException.class)
        .hasMessage("Record already exist ");
  }

  @Test
  void updateCopiesRequestToExistingBouwheerAndSaves() {
    Bouwheer existing = bouwheer(BOUWHEER_CODE, "PT OLD");
    BouwheerRequest request = bouwheerRequest("PT UPDATED");
    when(bouwheerRepository.findByBouwheerCode(BOUWHEER_CODE)).thenReturn(Optional.of(existing));
    when(currentUserService.usernameOrDefault(AppConstants.CREATOR)).thenReturn(AppConstants.CREATOR);

    BaseResponse response = service.update(BOUWHEER_CODE.toString(), request);

    assertThat(response).isInstanceOf(BaseResponseBuilder.class);
    ArgumentCaptor<Bouwheer> captor = ArgumentCaptor.forClass(Bouwheer.class);
    verify(bouwheerRepository).save(captor.capture());
    Bouwheer saved = captor.getValue();
    assertThat(saved.getBouwheerName()).isEqualTo("PT UPDATED");
    assertThat(saved.getLegalAddress()).isEqualTo(request.getLegalAddress());
    assertThat(saved.getUsrUpd()).isEqualTo(AppConstants.CREATOR);
    assertThat(saved.getDtmUpd()).isNotNull();
  }

  @Test
  void updateThrowsWhenBouwheerDoesNotExist() {
    BouwheerRequest request = bouwheerRequest("PT MISSING");
    when(bouwheerRepository.findByBouwheerCode(BOUWHEER_CODE)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.update(BOUWHEER_CODE.toString(), request))
        .isInstanceOf(BusinessException.class)
        .hasMessage("Record already exist ");
  }

  @Test
  void findByIdReturnsMappedResponseWhenBouwheerExists() {
    Bouwheer bouwheer = bouwheer(BOUWHEER_CODE, "PT FOUND");
    when(bouwheerRepository.findByBouwheerCode(BOUWHEER_CODE)).thenReturn(Optional.of(bouwheer));

    @SuppressWarnings("unchecked")
    BaseResponseBuilder<BouwheerResponse> response =
        (BaseResponseBuilder<BouwheerResponse>) service.findById(BOUWHEER_CODE.toString());

    assertThat(response.getData().getBouwheerCode()).isEqualTo(bouwheer.getBouwheerCode());
    assertThat(response.getData().getBouwheerName()).isEqualTo(bouwheer.getBouwheerName());
    assertThat(response.getData().getLegalAddress()).isEqualTo(bouwheer.getLegalAddress());
    assertThat(response.getData().getIsActive()).isEqualTo(bouwheer.getIsActive());
    assertThat(response.getData().getPicName()).isEqualTo(bouwheer.getPicName());
    assertThat(response.getData().getPicEmail()).isEqualTo(bouwheer.getPicEmail());
    assertThat(response.getData().getPicMobilePhone()).isEqualTo(bouwheer.getPicMobilePhone());
    assertThat(response.getData().getRt()).isEqualTo(bouwheer.getRt());
    assertThat(response.getData().getRw()).isEqualTo(bouwheer.getRw());
    assertThat(response.getData().getKelurahan()).isEqualTo(bouwheer.getKelurahan());
    assertThat(response.getData().getKecamatan()).isEqualTo(bouwheer.getKecamatan());
    assertThat(response.getData().getCity()).isEqualTo(bouwheer.getCity());
    assertThat(response.getData().getProvince()).isEqualTo(bouwheer.getProvince());
    assertThat(response.getData().getZipcode()).isEqualTo(bouwheer.getZipcode());
    assertThat(response.getData().getArea()).isEqualTo(bouwheer.getArea());
    assertThat(response.getData().getPhone()).isEqualTo(bouwheer.getPhone());
    assertThat(response.getData().getIsSbu()).isEqualTo(bouwheer.getIsSbu());
    assertThat(response.getData().getIsWaActive()).isEqualTo(bouwheer.getIsWaActive());
    assertThat(response.getData().getTermOfPayment()).isEqualTo(bouwheer.getTermOfPayment());
    assertThat(response.getData().getGracePeriod()).isEqualTo(bouwheer.getGracePeriod());
    assertThat(response.getData().getUsrCrt()).isEqualTo(bouwheer.getUsrCrt());
    assertThat(response.getData().getDtmCrt()).isEqualTo(bouwheer.getDtmCrt());
    assertThat(response.getData().getUsrUpd()).isEqualTo(bouwheer.getUsrUpd());
    assertThat(response.getData().getDtmUpd()).isEqualTo(bouwheer.getDtmUpd());
    assertThat(response.getData().getAesKey()).isEqualTo(bouwheer.getAesKey());
    assertThat(response.getData().getSecretKey()).isEqualTo(bouwheer.getSecretKey());
    assertThat(response.getData().getApiKey()).isEqualTo(bouwheer.getApiKey());
  }

  @Test
  void findByIdThrowsWhenBouwheerDoesNotExist() {
    when(bouwheerRepository.findByBouwheerCode(BOUWHEER_CODE)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.findById(BOUWHEER_CODE.toString()))
        .isInstanceOf(BusinessException.class)
        .hasMessage("Record already exist ");
  }

  private static Bouwheer bouwheer(UUID code, String name) {
    return Bouwheer.builder()
        .bouwheerCode(code)
        .bouwheerName(name)
        .legalAddress("Legal Address")
        .rt("001")
        .rw("002")
        .kelurahan("Kelurahan")
        .kecamatan("Kecamatan")
        .city("Jakarta")
        .province("DKI Jakarta")
        .zipcode("12345")
        .area("JKT")
        .phone("021-123")
        .isSbu(true)
        .picName("PIC Name")
        .picEmail("pic@example.com")
        .picMobilePhone("08123")
        .isWaActive(true)
        .termOfPayment(30L)
        .gracePeriod(5L)
        .aesKey("aes-key")
        .isActive(true)
        .secretKey("secret-key")
        .apiKey("api-key")
        .usrCrt("creator")
        .dtmCrt(LocalDateTime.of(2026, 8, 12, 10, 0))
        .usrUpd("updater")
        .dtmUpd(LocalDateTime.of(2026, 8, 12, 11, 0))
        .build();
  }

  private static BouwheerRequest bouwheerRequest(String name) {
    return BouwheerRequest.builder()
        .bouwheerName(name)
        .legalAddress("New Legal Address")
        .rt("003")
        .rw("004")
        .kelurahan("New Kelurahan")
        .kecamatan("New Kecamatan")
        .city("Bandung")
        .province("Jawa Barat")
        .zipcode("54321")
        .area("BDG")
        .phone("022-456")
        .isSbu(false)
        .picName("New PIC")
        .picEmail("new.pic@example.com")
        .picMobilePhone("08999")
        .isWaActive(false)
        .termOfPayment(45L)
        .gracePeriod(7L)
        .aesKey("new-aes")
        .secretKey("new-secret")
        .apiKey("new-api")
        .isActive(false)
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

  private static Page<Bouwheer> pageOf(Bouwheer bouwheer) {
    return new PageImpl<>(List.of(bouwheer), Pageable.ofSize(5), 1);
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private void stubFindAllWithSpecification(Page<Bouwheer> page) {
    when(bouwheerRepository.findAll(ArgumentMatchers.<Specification<Bouwheer>>any(), any(Pageable.class)))
        .thenAnswer(invocation -> {
          Specification<Bouwheer> specification = invocation.getArgument(0);
          Root<Bouwheer> root = mock(Root.class);
          CriteriaQuery<?> query = mock(CriteriaQuery.class);
          CriteriaBuilder builder = mock(CriteriaBuilder.class);
          Path path = mock(Path.class);
          Predicate predicate = mock(Predicate.class);

          lenient().when(root.get(anyString())).thenReturn(path);
          lenient().when(builder.like(ArgumentMatchers.<Expression<String>>any(), anyString())).thenReturn(predicate);
          lenient().when(builder.and(any(Predicate[].class))).thenReturn(predicate);

          specification.toPredicate(root, query, builder);
          return page;
        });
  }
}
