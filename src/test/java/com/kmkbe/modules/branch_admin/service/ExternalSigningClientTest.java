package com.kmkbe.modules.branch_admin.service;

import com.kmkbe.core.domain.dto.ExternalSigningRequest;
import com.kmkbe.core.domain.dto.ExternalSigningResponse;
import com.kmkbe.core.service.ExternalApiService;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ExternalSigningClientTest {

    @Test
    void sendDocumentSigningDelegatesToExternalApiService() {
        ExternalApiService externalApiService = mock(ExternalApiService.class);
        ExternalSigningClient client = new ExternalSigningClient(externalApiService);
        ExternalSigningRequest request = ExternalSigningRequest.builder().tenantCode("CSUL").build();
        ExternalSigningResponse expected = ExternalSigningResponse.builder().build();
        when(externalApiService.callEsignApi(request)).thenReturn(expected);

        ExternalSigningResponse actual = client.sendDocumentSigning(request);

        assertThat(actual).isSameAs(expected);
        verify(externalApiService).callEsignApi(request);
    }
}
