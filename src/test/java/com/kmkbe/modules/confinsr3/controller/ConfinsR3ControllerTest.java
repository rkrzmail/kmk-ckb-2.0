package com.kmkbe.modules.confinsr3.controller;

import com.kmkbe.helpers.base.BaseResponseBuilder;
import com.kmkbe.modules.confinsr3.service.ConfinsR3Service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConfinsR3ControllerTest {
  @Mock ConfinsR3Service service;

  ConfinsR3Controller controller;

  @BeforeEach
  void setUp() {
    controller = new ConfinsR3Controller(service);
  }

  @Test
  void delegatesEveryEndpoint() {
    BaseResponseBuilder response = new BaseResponseBuilder(true, 200, "Success", null);
    when(service.findZipcode("12345")).thenReturn(response);
    when(service.findCwrByCustomer(1, 10, "CUST")).thenReturn(response);
    when(service.findByCustomer(1, 10, "Customer")).thenReturn(response);
    when(service.findByCustomerNo("CUST-1")).thenReturn(response);
    when(service.findKeyValueByCode("TYPE")).thenReturn(response);
    when(service.findByCustomerNoCompany("CUST-1")).thenReturn(response);
    when(service.findByCustomerNoPersonal("CUST-1")).thenReturn(response);
    when(service.findCustomerByKeyAndValue("custNo", "CUST-1")).thenReturn(response);

    assertThat(controller.getZipcode("12345")).isSameAs(response);
    assertThat(controller.getCwrByCustomer("CUST", 1, 10)).isSameAs(response);
    assertThat(controller.getByCustomer("Customer", 1, 10)).isSameAs(response);
    assertThat(controller.getByCustomerNo("CUST-1")).isSameAs(response);
    assertThat(controller.getKeyValueByCode("TYPE")).isSameAs(response);
    assertThat(controller.getByCustomerNoCompany("CUST-1")).isSameAs(response);
    assertThat(controller.getByCustomerNoPersonal("CUST-1")).isSameAs(response);
    assertThat(controller.getCustomerByKeyAndValue("custNo", "CUST-1")).isSameAs(response);

    verify(service).findZipcode("12345");
    verify(service).findCwrByCustomer(1, 10, "CUST");
    verify(service).findByCustomer(1, 10, "Customer");
    verify(service).findByCustomerNo("CUST-1");
    verify(service).findKeyValueByCode("TYPE");
    verify(service).findByCustomerNoCompany("CUST-1");
    verify(service).findByCustomerNoPersonal("CUST-1");
    verify(service).findCustomerByKeyAndValue("custNo", "CUST-1");
  }
}
