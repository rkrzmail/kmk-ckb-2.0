package com.kmkbe.modules.common.repository;

import com.kmkbe.modules.common.entity.EmailTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailTemplateRepository extends JpaRepository<EmailTemplate, Long> {
    EmailTemplate findByEmailTemplateCodeAndIsActive(String code, boolean isActive);
}
