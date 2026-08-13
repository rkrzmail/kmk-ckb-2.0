package com.kmkbe.modules.branch_admin.service;

import com.kmkbe.core.domain.dto.ExternalSigningRequest;
import com.kmkbe.core.domain.dto.ExternalSigningResponse;
import com.kmkbe.core.service.ExternalApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ExternalSigningClient implements SigningClient {
    private final ExternalApiService externalApiService;

    @Override
    public ExternalSigningResponse sendDocumentSigning(ExternalSigningRequest request) {
        return externalApiService.callEsignApi(request);
    }
}
