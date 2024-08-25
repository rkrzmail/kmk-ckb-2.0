package com.kmkbe.modules.customer.services;

import com.kmkbe.core.domain.entity.OtpLog;
import com.kmkbe.core.domain.repository.CustomerRepository;
import com.kmkbe.core.domain.repository.OtpRepository;
import com.kmkbe.modules.customer.request.VerifyOtpRequest;
import com.kmkbe.modules.customer.service.OtpService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.testng.annotations.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;


@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
public class OtpServicesTest {
    @Mock
    private OtpRepository otpRepository;

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private OtpService otpService;

    @Test
    @DisplayName("Should return true if otp expired date is after +5 minutes")
    void checkExpirationTime() {
        OtpLog otp = new OtpLog();
        otp.setExpiredDate(Instant.now().plus(5, ChronoUnit.MINUTES));
        Assertions.assertThat(otp.getExpiredDate()).isAfter(Instant.now());
    }

    @Test
    @DisplayName("Should return valid otp with expiration time validation")
    void verifyForgotPinTest() {
        VerifyOtpRequest verifyOtpRequest = new VerifyOtpRequest("", "vandikalvandi@gmail.com", "1111", "123456");
        String result = otpService.verifyForgotPin(verifyOtpRequest);
        Assertions.assertThat(result).isEqualTo("Otp verified, try to enter new pin");
    }
}
