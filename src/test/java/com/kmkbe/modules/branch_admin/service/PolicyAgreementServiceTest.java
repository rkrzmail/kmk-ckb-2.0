package com.kmkbe.modules.branch_admin.service;

import com.kmkbe.core.domain.dto.PolicyAgreementDto;
import com.kmkbe.core.domain.entity.PolicyAgreement;
import com.kmkbe.core.domain.entity.PolicyAgreementHistory;
import com.kmkbe.core.domain.model.CommonResult;
import com.kmkbe.core.domain.repository.PolicyAgreementHistoryRepository;
import com.kmkbe.core.domain.repository.PolicyAgreementRepository;
import com.kmkbe.core.security.CurrentUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PolicyAgreementServiceTest {

    @Mock
    private PolicyAgreementRepository policyAgreementRepository;

    @Mock
    private PolicyAgreementHistoryRepository policyAgreementHistoryRepository;

    @Mock
    private CurrentUserService currentUserService;

    private PolicyAgreementService service;

    @BeforeEach
    void setUp() {
        service = new PolicyAgreementService(policyAgreementRepository, policyAgreementHistoryRepository, currentUserService);
    }

    @Test
    void createPolicyAgreementSavesEntityAndReturnsInputDto() {
        PolicyAgreementDto request = dto("Policy", "Description", "Content", 1, true);
        when(currentUserService.usernameOrDefault("UNKNOWN")).thenReturn("creator");

        CommonResult<PolicyAgreementDto> result = service.createPolicyAgreement(request);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getData()).isSameAs(request);
        ArgumentCaptor<PolicyAgreement> captor = ArgumentCaptor.forClass(PolicyAgreement.class);
        verify(policyAgreementRepository).save(captor.capture());
        PolicyAgreement saved = captor.getValue();
        assertThat(saved.getPolicyCode()).isNotBlank();
        assertThat(saved.getPolicyName()).isEqualTo("Policy");
        assertThat(saved.getPolicyDescription()).isEqualTo("Description");
        assertThat(saved.getPolicyContent()).isEqualTo("Content");
        assertThat(saved.getVersion()).isEqualTo(1);
        assertThat(saved.getIsActive()).isTrue();
        assertThat(saved.getUsrCrt()).isEqualTo("creator");
        assertThat(saved.getDtmCrt()).isNotNull();
    }

    @Test
    void getPolicyAgreementListMapsEntitiesToDtos() {
        PolicyAgreement policy = policy(1L, "POL001", "Policy", "Description", "Content", 1, true);
        when(policyAgreementRepository.findAll()).thenReturn(List.of(policy));

        List<PolicyAgreementDto> result = service.getPolicyAgreementList();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPolicyId()).isEqualTo(policy.getPolicyId());
        assertThat(result.get(0).getPolicyCode()).isEqualTo(policy.getPolicyCode());
        assertThat(result.get(0).getPolicyName()).isEqualTo(policy.getPolicyName());
        assertThat(result.get(0).getPolicyDescription()).isEqualTo(policy.getPolicyDescription());
        assertThat(result.get(0).getPolicyContent()).isEqualTo(policy.getPolicyContent());
        assertThat(result.get(0).getVersion()).isEqualTo(policy.getVersion());
        assertThat(result.get(0).getIsActive()).isEqualTo(policy.getIsActive());
        assertThat(result.get(0).getUsrCrt()).isEqualTo(policy.getUsrCrt());
        assertThat(result.get(0).getDtmCrt()).isEqualTo(policy.getDtmCrt());
        assertThat(result.get(0).getUsrUpd()).isEqualTo(policy.getUsrUpd());
        assertThat(result.get(0).getDtmUpd()).isEqualTo(policy.getDtmUpd());
    }

    @Test
    void getPolicyAgreementHistoryByCodeReturnsLatestVersion() {
        PolicyAgreementHistory oldHistory = history("POL001", "old", 1);
        PolicyAgreementHistory latestHistory = history("POL001", "latest", 3);
        PolicyAgreementHistory middleHistory = history("POL001", "middle", 2);
        when(policyAgreementHistoryRepository.findByPolicyCode("POL001"))
                .thenReturn(List.of(oldHistory, latestHistory, middleHistory));

        CommonResult<PolicyAgreementDto> result = service.getPolicyAgreementHistoryByCode("POL001");

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getData().getPolicyCode()).isEqualTo("POL001");
        assertThat(result.getData().getPolicyContent()).isEqualTo("latest");
        assertThat(result.getData().getVersion()).isEqualTo(3);
        assertThat(result.getData().getUsrCrt()).isEqualTo(latestHistory.getUsrCrt());
        assertThat(result.getData().getDtmCrt()).isEqualTo(latestHistory.getDtmCrt());
    }

    @Test
    void getPolicyAgreementHistoryByCodeReturnsFailWhenHistoryMissing() {
        when(policyAgreementHistoryRepository.findByPolicyCode("POL001")).thenReturn(List.of());

        CommonResult<PolicyAgreementDto> result = service.getPolicyAgreementHistoryByCode("POL001");

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getCode()).isEqualTo(400);
        assertThat(result.getMessage()).isEqualTo("Policy Agreement History not found");
    }

    @Test
    @SuppressWarnings("unchecked")
    void getPolicyAgreementHistoryByCodeThrowsWhenHistoryListIsReportedNonEmptyButStreamsNoData() {
        List<PolicyAgreementHistory> histories = mock(List.class);
        when(histories.isEmpty()).thenReturn(false);
        when(histories.stream()).thenReturn(Stream.empty());
        when(policyAgreementHistoryRepository.findByPolicyCode("POL001")).thenReturn(histories);

        assertThatThrownBy(() -> service.getPolicyAgreementHistoryByCode("POL001"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("No policy history found");
    }

    @Test
    void getPolicyAgreementByIdReturnsPolicyWhenFound() {
        PolicyAgreement policy = policy(1L, "POL001", "Policy", "Description", "Content", 1, true);
        when(policyAgreementRepository.findById(1L)).thenReturn(Optional.of(policy));

        CommonResult<PolicyAgreementDto> result = service.getPolicyAgreementById(1L);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getData().getPolicyName()).isEqualTo("Policy");
        assertThat(result.getData().getPolicyDescription()).isEqualTo("Description");
        assertThat(result.getData().getPolicyContent()).isEqualTo("Content");
        assertThat(result.getData().getVersion()).isEqualTo(1);
        assertThat(result.getData().getIsActive()).isTrue();
    }

    @Test
    void getPolicyAgreementByIdReturnsFailWhenPolicyMissing() {
        when(policyAgreementRepository.findById(1L)).thenReturn(Optional.empty());

        CommonResult<PolicyAgreementDto> result = service.getPolicyAgreementById(1L);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getCode()).isEqualTo(400);
        assertThat(result.getMessage()).isEqualTo("Policy Agreement not found");
    }

    @Test
    void updatePolicyAgreementCreatesHistoryUpdatesPolicyAndReturnsDto() {
        PolicyAgreement existing = policy(1L, "POL001", "Old", "Old Desc", "Old Content", 2, true);
        PolicyAgreementDto request = dto("New", "New Desc", "New Content", 99, false);
        when(policyAgreementRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(currentUserService.usernameOrDefault("UNKNOWN")).thenReturn("updater");

        CommonResult<PolicyAgreementDto> result = service.updatePolicyAgreement(1L, request);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getData().getPolicyId()).isEqualTo(1L);
        assertThat(result.getData().getPolicyName()).isEqualTo("New");
        assertThat(result.getData().getVersion()).isEqualTo(3);
        assertThat(result.getData().getIsActive()).isFalse();
        ArgumentCaptor<PolicyAgreementHistory> historyCaptor = ArgumentCaptor.forClass(PolicyAgreementHistory.class);
        verify(policyAgreementHistoryRepository).save(historyCaptor.capture());
        assertThat(historyCaptor.getValue().getPolicyCode()).isEqualTo("POL001");
        assertThat(historyCaptor.getValue().getPolicyContent()).isEqualTo("Old Content");
        assertThat(historyCaptor.getValue().getVersion()).isEqualTo(2);
        verify(policyAgreementRepository).save(existing);
        assertThat(existing.getUsrUpd()).isEqualTo("updater");
        assertThat(existing.getDtmUpd()).isNotNull();
    }

    @Test
    void updatePolicyAgreementReturnsFailWhenPolicyMissing() {
        when(policyAgreementRepository.findById(1L)).thenReturn(Optional.empty());

        CommonResult<PolicyAgreementDto> result = service.updatePolicyAgreement(1L, dto("New", "Desc", "Content", 1, true));

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getCode()).isEqualTo(400);
        assertThat(result.getMessage()).isEqualTo("Policy Agreement not found");
    }

    private static PolicyAgreementDto dto(String name, String description, String content, Integer version, Boolean active) {
        PolicyAgreementDto dto = new PolicyAgreementDto();
        dto.setPolicyName(name);
        dto.setPolicyDescription(description);
        dto.setPolicyContent(content);
        dto.setVersion(version);
        dto.setIsActive(active);
        return dto;
    }

    private static PolicyAgreement policy(
            Long id,
            String code,
            String name,
            String description,
            String content,
            Integer version,
            Boolean active
    ) {
        PolicyAgreement policy = new PolicyAgreement();
        policy.setPolicyId(id);
        policy.setPolicyCode(code);
        policy.setPolicyName(name);
        policy.setPolicyDescription(description);
        policy.setPolicyContent(content);
        policy.setVersion(version);
        policy.setIsActive(active);
        policy.setUsrCrt("creator");
        policy.setDtmCrt(LocalDateTime.of(2026, 8, 12, 10, 0));
        policy.setUsrUpd("updater");
        policy.setDtmUpd(LocalDateTime.of(2026, 8, 12, 11, 0));
        return policy;
    }

    private static PolicyAgreementHistory history(String code, String content, Integer version) {
        PolicyAgreementHistory history = new PolicyAgreementHistory();
        history.setPolicyCode(code);
        history.setPolicyContent(content);
        history.setVersion(version);
        history.setUsrCrt("creator-" + version);
        history.setDtmCrt(LocalDateTime.of(2026, 8, 12, 10, 0).plusHours(version));
        return history;
    }
}
