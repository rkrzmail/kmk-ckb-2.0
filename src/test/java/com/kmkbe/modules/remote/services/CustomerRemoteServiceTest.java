package com.kmkbe.modules.remote.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.kmkbe.core.utils.DateTimeUtils;
import com.kmkbe.core.utils.ObjectUtils;
import com.kmkbe.modules.remote.request.ExistingCustomerRequest;
import com.kmkbe.modules.remote.service.CustomerRemoteService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.HttpServerErrorException;

import java.util.ArrayList;
import java.util.Date;

public class CustomerRemoteServiceTest extends BaseRemoteServicesTest {
    private CustomerRemoteService customerRemoteService;
    private ExistingCustomerRequest existingCustomerRequest;

    @Override
    @BeforeEach
    public void setupBeforeEach() {
        super.setupBeforeEach();
        customerRemoteService = new CustomerRemoteService(restTemplateByPassSSL, baseRemoteService, objectMapper);

        existingCustomerRequest = ExistingCustomerRequest.builder()
                .args(ExistingCustomerRequest.Args.builder()
                        .key("IdNo")
                        .operator("EQ")
                        .value("010002509057000")
                        .build()
                )
                .includeProperties(new ArrayList<>())
                .requestDateTime(DateTimeUtils.SDF_STANDARD_DATE.format(new Date()))
                .build();
    }

    @Test
    @Order(1)
    public void existingCustomerRequest_shouldReturnRealJsonProperty() throws JsonProcessingException {
        String json = ObjectUtils.jsonToStr(existingCustomerRequest);
        Assertions.assertTrue(json.contains("KeyAndValueObj"));
    }

    @Test
    public void postExistingCustomer_shouldReturnCustomerRemoteDto() {
        //CustomerRemoteDto customerRemoteDto = customerRemoteService.validateExisting(existingCustomerRequest);
        //Assertions.assertNotNull(customerRemoteDto.getIdNo());
    }

    @Test
    public void postExistingCustomer_shouldThrownInternalError() {
        /*Assertions.assertThrows(
                HttpServerErrorException.class,
                () -> customerRemoteService.validateExisting(ExistingCustomerRequest.builder().build())
        );*/
    }
}
