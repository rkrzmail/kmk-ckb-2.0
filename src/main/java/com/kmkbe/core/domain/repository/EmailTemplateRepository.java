package com.kmkbe.core.domain.repository;

import com.kmkbe.core.domain.entity.EmailTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailTemplateRepository extends JpaRepository<EmailTemplate, Long> {
    EmailTemplate findByEmailTemplateCodeAndIsActive(String code, boolean isActive);
}
