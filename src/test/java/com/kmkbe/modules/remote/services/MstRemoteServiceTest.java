package com.kmkbe.modules.remote.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.kmkbe.core.service.BaseRemoteService;
import com.kmkbe.core.utils.ObjectUtils;
import com.kmkbe.core.domain.dto.BaseMstRemoteResponseDto;
import com.kmkbe.core.domain.dto.InputOptionsRemoteDto;
import com.kmkbe.modules.remote.request.RefMasterRequest;
import com.kmkbe.modules.remote.service.MstRemoteService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

public class MstRemoteServiceTest extends BaseRemoteServicesTest {
    private MstRemoteService mstRemoteService;

    @Override
    @BeforeEach
    public void setupBeforeEach() {
        super.setupBeforeEach();

        mstRemoteService = new MstRemoteService(
                objectMapper,
                restTemplateByPassSSL,
                baseRemoteService
        );
    }

    @Test
    public void refMasterRequest_shouldReturnValidRequestOfPropertyDeclared() {
        RefMasterRequest request = RefMasterRequest.builder()
                .refMasterTypeCode(BaseRemoteService.RefMasterTypeCode.JenisKelamin)
                .requestDateTime("2024-08-20")
                .build();

        Map<String, Object> json = new HashMap<>();
        try {
            String str = ObjectUtils.jsonToStr(request);
            json = ObjectUtils.strToJson(str);
        } catch (JsonProcessingException ignored) {

        }

        String expectDt = "2024-08-20";
        Assertions.assertEquals(json.get("RequestDateTime").toString(), expectDt);
    }

    @Test
    public void refMasterInputOptionTipePerusahaan_shouldPerformSuccessAndReturnInputOptionResponse() {
        BaseMstRemoteResponseDto<InputOptionsRemoteDto> response = mstRemoteService.refMasterInputOption(BaseRemoteService.RefMasterTypeCode.TipePerusahaan, null);
        Assertions.assertNotNull(response.getData());
        Assertions.assertFalse(response.getData().isEmpty());
    }
}
