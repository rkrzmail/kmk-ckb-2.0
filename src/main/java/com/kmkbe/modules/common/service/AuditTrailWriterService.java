package com.kmkbe.modules.common.service;

import com.kmkbe.core.domain.entity.AuditTrail;
import com.kmkbe.core.domain.repository.AuditTrailRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuditTrailWriterService {
  private final AuditTrailRepository auditTrailRepository;

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void save(AuditTrail auditTrail) {
    auditTrailRepository.save(auditTrail);
  }
}
