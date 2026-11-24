package com.kmkbe.modules.customer.model;

import com.kmkbe.modules.customer.model.dto.CustomerDto;
import com.kmkbe.modules.customer.model.entity.Customer;
import com.kmkbe.modules.customer.model.request.ApprovalRequest;
import com.kmkbe.modules.customer.model.request.ForgotPinRequest;
import com.kmkbe.modules.customer.model.request.LoginRequest;
import com.kmkbe.modules.customer.model.request.RequestOtpRequest;
import com.kmkbe.modules.customer.model.request.SignUpRequest;
import com.kmkbe.modules.customer.model.request.UpdateCustomerRequest;
import com.kmkbe.modules.customer.model.request.UpdateFapRequest;
import com.kmkbe.modules.customer.model.request.VerifyOtpRequest;
import com.kmkbe.modules.customer.model.response.CustomerResponse;
import com.kmkbe.modules.customer.model.response.PageCustomerResponse;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CustomerModelCoverageTest {
  @Test
  void requestAndResponseModels() {
    UUID code = UUID.randomUUID();
    assertThat(new ForgotPinRequest("e", "p", "t").email()).isEqualTo("e");
    assertThat(new LoginRequest("e", "p").pin()).isEqualTo("p");
    assertThat(new RequestOtpRequest("e").email()).isEqualTo("e");
    assertThat(new VerifyOtpRequest("r", "e", "o", "p").otp()).isEqualTo("o");
    assertThat(ApprovalRequest.builder().build()).isNotNull();
    assertThat(SignUpRequest.builder().name("Name").build().getName()).isEqualTo("Name");
    assertThat(SignUpRequest.builder().build().toString()).contains("SignUpRequest");

    UpdateFapRequest fap = new UpdateFapRequest();
    fap.setFinancingHdrCode(code);
    fap.setEmail("e@x.com");
    fap.setFapDate(LocalDateTime.of(2026, 11, 24, 10, 0));
    fap.setFapStatus("APPROVED");
    assertThat(fap.toString()).contains("APPROVED");

    UpdateCustomerRequest update = UpdateCustomerRequest.builder().custName("Name").build();
    assertThat(update.getCustName()).isEqualTo("Name");
    assertThat(update.toString()).contains("Name");

    CustomerResponse response = CustomerResponse.builder().custCode(code).custName("Name").build();
    assertThat(response.getCustName()).isEqualTo("Name");
    assertThat(response.toString()).contains("Name");
    assertThat(PageCustomerResponse.builder().content(List.of(response)).build().getContent()).hasSize(1);
  }

  @Test
  void customerEntityAndDto() {
    UUID code = UUID.randomUUID();
    Customer customer = new Customer();
    customer.setCustCode(code);
    customer.setCustEmail("e@x.com");
    customer.setCustName("Name");
    assertThat(customer.toString()).contains("Name");

    CustomerDto dto = new CustomerDto();
    dto.setCustCode(code);
    dto.setCustName("Name");
    dto.setCustEmail("e@x.com");
    assertThat(dto.getCustCode()).isEqualTo(code);
    assertThat(dto.getCustName()).isEqualTo("Name");
  }
}
