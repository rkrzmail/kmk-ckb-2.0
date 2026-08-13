package com.kmkbe.modules.branch_admin.service;

import com.kmkbe.core.domain.dto.ExternalSigningRequest;
import com.kmkbe.core.domain.dto.ExternalSigningResponse;

public interface SigningClient {
    ExternalSigningResponse sendDocumentSigning(ExternalSigningRequest request);
}
