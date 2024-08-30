package com.kmkbe.modules.customer.service;

import com.kmkbe.core.domain.dto.RequestOtpDto;
import com.kmkbe.core.domain.entity.Customer;
import com.kmkbe.core.domain.entity.OtpLog;
import com.kmkbe.core.domain.repository.CustomerRepository;
import com.kmkbe.core.domain.repository.OtpRepository;
import com.kmkbe.modules.common.service.EmailService;
import com.kmkbe.modules.customer.request.RequestOtpRequest;
import com.kmkbe.modules.customer.request.VerifyOtpRequest;
import jakarta.annotation.Nullable;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpService {
    private final OtpRepository otpRepository;
    private final CustomerRepository customerRepository;
    private final EmailService emailService;
    private final CustomerService customerService;
    private final BCryptPasswordEncoder bcryptEncoder;

    public OtpLog create(@NonNull Customer customer, @NonNull OtpType type) throws Exception {
        final Instant now = Instant.now();
        final OtpLog otpLog = new OtpLog();
        otpLog.setEmail(customer.getCustEmail());
        otpLog.setMobilePhone(customer.getCustMobilePhone());
        otpLog.setGeneratedDate(now);
        otpLog.setExpiredDate(now.plus(5, ChronoUnit.MINUTES));
        otpLog.setUsrCrt(customer.getCustName());
        otpLog.setDtmCrt(Instant.now());
        otpLog.setIsUsed(false);
        otpLog.setOtpCode(genOtp());
        //otpLog.setOtpCode("1111");
        otpRepository.save(otpLog);

        if (type == OtpType.SIGNUP) {
            emailService.sendOtp(customer, otpLog.getOtpCode());
        } else if (type == OtpType.CHANGE_PIN) {
            emailService.sendOtpChangePin(customer, otpLog.getOtpCode());
        }

        return otpLog;
    }

    @Transactional
    public String verifySignUp(VerifyOtpRequest verifyOtpRequest) throws Exception {
        final FindCustomerOtp findCustomerOtp = new FindCustomerOtp(
                customerRepository,
                otpRepository,
                verifyOtpRequest.email(),
                verifyOtpRequest.otp(),
                OtpType.SIGNUP
        );

        final Customer customer = findCustomerOtp.getCustomer();
        customerService.activated(customer);
        emailService.sendNotificationActive(customer);

        final OtpLog otp = findCustomerOtp.getOtpLog();
        if (Instant.now().isAfter(otp.getExpiredDate())) {
            throw new IllegalStateException("Otp is Expired");
        }

        otp.setIsUsed(true);
        otp.setUsrUpd(customer.getCustName());
        otp.setDtmUpd(Instant.now());

        otpRepository.save(otp);
        return "Sign up successfully";
    }

    public RequestOtpDto sendForgotPin(String email) throws Exception {
        final Optional<Customer> find = customerRepository.findByCustEmail(email);
        if (find.isEmpty()) {
            throw new EntityNotFoundException("Customer not found, enter an valid email");
        }

        final Customer cust = find.get();
        final OtpLog otpLog = create(cust, OtpType.CHANGE_PIN);

        return new RequestOtpDto(
                genRequestId(cust, otpLog),
                cust.getCustEmail(),
                otpLog.getExpiredDate()
        );
    }

    @Transactional
    public String verifyForgotPin(VerifyOtpRequest request) {
        final FindCustomerOtp findCustomerOtp = new FindCustomerOtp(
                customerRepository,
                otpRepository,
                request.email(),
                request.otp(),
                OtpType.CHANGE_PIN
        );

        final Customer customer = findCustomerOtp.getCustomer();
        final OtpLog otp = findCustomerOtp.getOtpLog();

        if (!bcryptEncoder.matches((customer.getCustEmail() + otp.getOtpLogId()), genRequestId(customer, otp))) {
            throw new IllegalStateException("Request id not valid, try to enter valid requestId");
        }

        if (Instant.now().isAfter(otp.getExpiredDate())) {
            throw new IllegalStateException("Otp is expired, try to resend again");
        }

        otp.setIsUsed(true);
        otp.setUsrUpd(customer.getCustName());
        otp.setDtmUpd(Instant.now());
        otpRepository.save(otp);

        return "Otp verified, try to enter new pin";
    }

    public RequestOtpDto resend(RequestOtpRequest request, OtpType type) throws Exception {
        final FindCustomerOtp findCustomerOtp = new FindCustomerOtp(
                customerRepository,
                otpRepository,
                request.email(),
                type
        );

        final Customer cust = findCustomerOtp.getCustomer();
        final OtpLog otpLog = create(cust, type);

        return new RequestOtpDto(
                genRequestId(cust, otpLog),
                cust.getCustEmail(),
                otpLog.getExpiredDate()
        );
    }

    private String genOtp() {
        return new DecimalFormat("0000").format(new Random().nextInt(9999));
    }

    public String genRequestId(Customer cust, OtpLog otpLog) {
        return bcryptEncoder.encode(cust.getCustEmail() + otpLog.getOtpLogId());
    }

    public enum OtpType {
        SIGNUP, CHANGE_PIN;

        @Override
        public String toString() {
            return switch (this) {
                case SIGNUP -> "Sign Up";
                case CHANGE_PIN -> "Forgot Pin";
            };
        }
    }

    private static class FindCustomerOtp {
        private final CustomerRepository customerRepository;
        private final OtpRepository otpRepository;

        @Getter
        private Customer customer;
        @Getter
        private OtpLog otpLog;

        public FindCustomerOtp(
                @NonNull CustomerRepository customerRepository,
                @NonNull OtpRepository otpRepository,
                @NonNull String email,
                @Nullable String otpCode,
                OtpType type
        ) {
            this.customerRepository = customerRepository;
            this.otpRepository = otpRepository;
            fetch(email, otpCode, type);
        }

        public FindCustomerOtp(
                @NonNull CustomerRepository customerRepository,
                @NonNull OtpRepository otpRepository,
                @NonNull String email,
                OtpType type
        ) {
            this.customerRepository = customerRepository;
            this.otpRepository = otpRepository;
            fetch(email, null, type);
        }

        private void fetch(String email, @Nullable String otpCode, OtpType type) {
            final Optional<Customer> findCust = customerRepository.findByCustEmailOrderByCustIdDesc(email);
            if (findCust.isEmpty()) {
                throw new EntityNotFoundException("User doesn't exists");
            }

            this.customer = findCust.get();

            /*final Long todayRequest = otpRepository.countTodayRequestByEmail(customer.getCustEmail());
            if (todayRequest > 5) {
                throw new IllegalStateException("Request limit exceeded for today, try again tomorrow");
            }*/

            if (otpCode != null && !otpCode.isEmpty()) {
                final Optional<OtpLog> findOtp = otpRepository.findTopByEmailAndOtpCodeOrderByDtmCrtDesc(
                        customer.getCustEmail(),
                        otpCode
                );

                if (findOtp.isEmpty()) {
                    throw new IllegalStateException("Otp not valid, try to check email to entry right Otp");
                }

                this.otpLog = findOtp.get();
                if (this.otpLog.getIsUsed()) {
                    throw new IllegalStateException("Otp for " + type.toString() + " already used");
                }
            } else {
                this.otpLog = otpRepository.findTopByEmail(this.customer.getCustEmail());
            }
        }
    }
}
