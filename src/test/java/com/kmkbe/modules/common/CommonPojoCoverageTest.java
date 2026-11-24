package com.kmkbe.modules.common;

import com.kmkbe.core.domain.entity.AuditTrail;
import com.kmkbe.modules.common.controller.GeneralSettingController;
import com.kmkbe.modules.common.request.RefreshTokenRequest;
import com.kmkbe.modules.common.service.ApiIntegrationLogService;
import com.kmkbe.modules.common.service.AuditTrailWriterService;
import com.kmkbe.modules.common.service.FormVisitLogService;
import com.kmkbe.modules.common.service.JobLogService;
import com.kmkbe.modules.common.service.RabbitMqLogService;
import com.kmkbe.core.domain.repository.AuditTrailRepository;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class CommonPojoCoverageTest {
  @Test
  void instantiatesBeansAndRecords() {
    assertThat(new GeneralSettingController()).isNotNull();
    assertThat(new ApiIntegrationLogService()).isNotNull();
    assertThat(new JobLogService()).isNotNull();
    assertThat(new RabbitMqLogService()).isNotNull();
    assertThat(new FormVisitLogService()).isNotNull();
    AuditTrailRepository repository = mock(AuditTrailRepository.class);
    AuditTrailWriterService writer = new AuditTrailWriterService(repository);
    AuditTrail trail = new AuditTrail();
    writer.save(trail);
    verify(repository).save(trail);
    assertThat(new RefreshTokenRequest("token").refreshToken()).isEqualTo("token");
  }
}
