package com.kmkbe.modules.customer.service;

import com.kmkbe.core.domain.constant.AuditAction;
import com.kmkbe.core.domain.entity.OtpLog;
import com.kmkbe.core.domain.repository.OtpRepository;
import com.kmkbe.modules.common.service.AuditTrailService;
import com.kmkbe.modules.common.service.refresh_token.IRefreshTokenServices;
import com.kmkbe.modules.customer.model.entity.Customer;
import com.kmkbe.modules.customer.model.request.ForgotPinRequest;
import com.kmkbe.modules.customer.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceForgotPinReplayTest {

    @Mock private CustomerRepository customerRepository;
    @Mock private OtpRepository otpRepository;
    @Mock private BCryptPasswordEncoder bcryptEncoder;
    @Mock private ChangePasswordLogService changePasswordLogService;
    @Mock private AuditTrailService auditTrailService;
    @Mock private IRefreshTokenServices refreshTokenServices;

    private Clock clock;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        clock = Clock.fixed(Instant.parse("2026-10-19T10:00:00Z"), ZoneId.of("Asia/Jakarta"));
        authService = new AuthService(
                customerRepository,
                null,
                null,
                null,
                changePasswordLogService,
                bcryptEncoder,
                null,
                otpRepository,
                null,
                null,
                auditTrailService,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }

    @Test
    @DisplayName("H8: forgotPin successfully marks OtpLog as used and updates customer PIN")
    void forgotPinMarksOtpAsUsed() {
        String email = "debitur@example.com";
        String token = "123456";
        String newPin = "654321";

        Customer customer = Customer.builder()
                .custCode(UUID.randomUUID())
                .custEmail(email)
                .custPin("old-hashed-pin")
                .build();

        OtpLog otpLog = new OtpLog();
        otpLog.setOtpLogId(101L);
        otpLog.setEmail(email);
        otpLog.setOtpCode(token);
        otpLog.setIsUsed(false);
        // Expiry 5 minutes in the future relative to fixed clock
        otpLog.setExpiredDate(LocalDateTime.now(clock).plusMinutes(5));

        when(customerRepository.findByCustEmail(email)).thenReturn(Optional.of(customer));
        when(otpRepository.findTopByEmailAndOtpCodeOrderByDtmCrtDesc(email, token)).thenReturn(Optional.of(otpLog));
        when(bcryptEncoder.encode(newPin)).thenReturn("new-hashed-pin");
        when(customerRepository.save(any(Customer.class))).thenReturn(customer);

        ForgotPinRequest request = new ForgotPinRequest(email, newPin, token);
        String message = authService.forgotPin(request);

        assertThat(message).contains("Forgot pin successfully");
        assertThat(otpLog.getIsUsed()).isTrue();
        assertThat(otpLog.getDtmUpd()).isNotNull();
        verify(otpRepository).save(otpLog);
        verify(customerRepository).save(customer);
    }

    @Test
    @DisplayName("H8: forgotPin rejects already-used OTP (mitigating replay attack)")
    void forgotPinRejectsAlreadyUsedOtp() {
        String email = "debitur@example.com";
        String token = "123456";

        Customer customer = Customer.builder()
                .custCode(UUID.randomUUID())
                .custEmail(email)
                .build();

        OtpLog usedOtp = new OtpLog();
        usedOtp.setEmail(email);
        usedOtp.setOtpCode(token);
        usedOtp.setIsUsed(true); // Already used!
        usedOtp.setExpiredDate(LocalDateTime.now(clock).plusMinutes(5));

        when(customerRepository.findByCustEmail(email)).thenReturn(Optional.of(customer));
        when(otpRepository.findTopByEmailAndOtpCodeOrderByDtmCrtDesc(email, token)).thenReturn(Optional.of(usedOtp));

        ForgotPinRequest request = new ForgotPinRequest(email, "654321", token);

        assertThatThrownBy(() -> authService.forgotPin(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already been used");

        verify(customerRepository, never()).save(any());
    }

    @Test
    @DisplayName("H8: forgotPin rejects expired OTP")
    void forgotPinRejectsExpiredOtp() {
        String email = "debitur@example.com";
        String token = "123456";

        Customer customer = Customer.builder()
                .custCode(UUID.randomUUID())
                .custEmail(email)
                .build();

        OtpLog expiredOtp = new OtpLog();
        expiredOtp.setEmail(email);
        expiredOtp.setOtpCode(token);
        expiredOtp.setIsUsed(false);
        // Expired 1 hour ago relative to system clock
        expiredOtp.setExpiredDate(LocalDateTime.now().minusHours(1));

        when(customerRepository.findByCustEmail(email)).thenReturn(Optional.of(customer));
        when(otpRepository.findTopByEmailAndOtpCodeOrderByDtmCrtDesc(email, token)).thenReturn(Optional.of(expiredOtp));

        ForgotPinRequest request = new ForgotPinRequest(email, "654321", token);

        assertThatThrownBy(() -> authService.forgotPin(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("expired");

        verify(customerRepository, never()).save(any());
    }
}
