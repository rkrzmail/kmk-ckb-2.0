package com.kmkbe.modules.customer.service;

import com.kmkbe.modules.common.service.EmailService;
import com.kmkbe.modules.customer.entity.Customer;
import com.kmkbe.modules.customer.entity.OtpLog;
import com.kmkbe.modules.customer.repository.CustomerRepository;
import com.kmkbe.modules.customer.repository.OtpRepository;
import com.kmkbe.modules.customer.request.RequestOtpRequest;
import com.kmkbe.modules.customer.request.VerifyOtpRequest;
import com.kmkbe.modules.customer.dto.RequestOtpDto;
import jakarta.annotation.Nullable;
import jakarta.mail.MessagingException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.time.OffsetDateTime;
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

    public OtpLog create(@NonNull Customer customer, @NonNull OtpType type) throws MessagingException {
        final OffsetDateTime now = OffsetDateTime.now();
        final OtpLog otpLog = new OtpLog();
        otpLog.setEmail(customer.getCustEmail());
        otpLog.setMobilePhone(customer.getCustMobilePhone());
        otpLog.setGeneratedDate(now);
        otpLog.setExpiredDate(now.plusMinutes(5000));
        otpLog.setUsrCrt(customer.getCustName());
        otpLog.setDtmCrt(OffsetDateTime.now());
        otpLog.setIsUsed(false);
        otpLog.setOtpCode(genOtp());

        otpRepository.save(otpLog);

        if (type == OtpType.SIGNUP) {
            emailService.sendOtp(customer, otpLog.getOtpCode());
        } else if (type == OtpType.CHANGE_PIN) {
            emailService.sendOtpChangePin(customer, otpLog.getOtpCode());
        }

        return otpLog;
    }

    @Transactional
    public String verifySignUp(VerifyOtpRequest verifyOtpRequest) throws MessagingException {
        final FindCustomerOtp findCustomerOtp = new FindCustomerOtp(
                customerRepository,
                otpRepository,
                verifyOtpRequest.email(),
                verifyOtpRequest.otpCode(),
                OtpType.SIGNUP
        );

        final Customer customer = findCustomerOtp.getCustomer();
        customerService.activated(customer);
        emailService.sendNotificationActive(customer);

        final OtpLog otp = findCustomerOtp.getOtpLog();
        if (OffsetDateTime.now().isAfter(otp.getExpiredDate())) {
            throw new IllegalStateException("Otp is Expired");
        }

        otp.setIsUsed(true);
        otp.setUsrUpd(customer.getCustName());
        otp.setDtmUpd(OffsetDateTime.now());

        otpRepository.save(otp);
        return "Sign up successfully, try to login now";
    }

    public RequestOtpDto sendForgotPin(String email) throws MessagingException {
        final Optional<Customer> find = customerRepository.findByCustEmail(email);
        if (find.isEmpty()) {
            throw new EntityNotFoundException("Customer not found, enter an valid email");
        }

        final Customer cust = find.get();
        final OtpLog otpLog = create(cust, OtpType.CHANGE_PIN);

        return new RequestOtpDto(
                genRequestId(cust, otpLog),
                cust.getCustEmail(),
                OffsetDateTime.now()
        );
    }

    @Transactional
    public String verifyForgotPin(VerifyOtpRequest request) {
        final FindCustomerOtp findCustomerOtp = new FindCustomerOtp(
                customerRepository,
                otpRepository,
                request.email(),
                request.otpCode(),
                OtpType.CHANGE_PIN
        );

        final Customer customer = findCustomerOtp.getCustomer();
        final OtpLog otp = findCustomerOtp.getOtpLog();

        if (!bcryptEncoder.matches((customer.getCustEmail() + otp.getOtpLogId()), genRequestId(customer, otp))) {
            throw new IllegalStateException("Request id not valid, try to enter valid requestId");
        }

        if (OffsetDateTime.now().isAfter(otp.getExpiredDate())) {
            throw new IllegalStateException("Otp is expired, try to resend again");
        }

        otp.setIsUsed(true);
        otp.setUsrUpd(customer.getCustName());
        otp.setDtmUpd(OffsetDateTime.now());
        otpRepository.save(otp);

        return "Otp verified, try to enter new pin";
    }

    public String resend(RequestOtpRequest request, OtpType type) throws MessagingException {
        final FindCustomerOtp findCustomerOtp = new FindCustomerOtp(
                customerRepository,
                otpRepository,
                request.email(),
                type
        );

        emailService.sendOtp(findCustomerOtp.getCustomer(), genOtp());

        return "Resend Otp " + type.toString() + " Successfully";
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

            if (otpCode != null && !otpCode.isEmpty()) {
                final Optional<OtpLog> findOtp = otpRepository.findTopByEmailAndOtpCode(
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
                this.otpLog = otpRepository.findByEmail(this.customer.getCustEmail());
            }
        }
    }
}
