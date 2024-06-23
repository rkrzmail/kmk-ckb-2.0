package com.kmkbe.modules.customer.service;

import com.kmkbe.modules.common.service.EmailService;
import com.kmkbe.modules.customer.entity.Customer;
import com.kmkbe.modules.customer.entity.OtpLog;
import com.kmkbe.modules.customer.repository.CustomerRepository;
import com.kmkbe.modules.customer.repository.OtpRepository;
import com.kmkbe.modules.customer.request.ForgotPinRequest;
import com.kmkbe.modules.customer.request.VerifyOtpRequest;
import com.kmkbe.modules.customer.response.RequestOtpResponse;
import jakarta.annotation.Nullable;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    public void create(Customer customer) {
        final OffsetDateTime now = OffsetDateTime.now();
        final OtpLog otpLog = new OtpLog();
        otpLog.setEmail(customer.getCustEmail());
        otpLog.setMobilePhone(customer.getCustMobilePhone());
        otpLog.setGeneratedDate(now);
        otpLog.setExpiredDate(now.plusMinutes(5000));
        otpLog.setUsrCrt(customer.getCustName());
        otpLog.setDtmCrt(now);
        otpLog.setIsUsed(false);
        otpLog.setOtpCode(genOtp());

        otpRepository.save(otpLog);
        emailService.sendOtp(customer, otpLog.getOtpCode());
    }

    @Transactional
    public Customer verifySignUp(VerifyOtpRequest verifyOtpRequest) {
        final FindCustomerOtp findCustomerOtp = new FindCustomerOtp(
                customerRepository,
                otpRepository,
                verifyOtpRequest.email(),
                verifyOtpRequest.otpCode()
        );

        final Customer customer = findCustomerOtp.getCustomer();
        customerService.activated(customer);

        final OtpLog otp = findCustomerOtp.getOtpLog();
        if (OffsetDateTime.now().isAfter(otp.getExpiredDate())) {
            throw new IllegalStateException("Otp is Expired");
        }

        otp.setIsUsed(true);
        otp.setUsrUpd(customer.getCustName());
        otp.setDtmUpd(OffsetDateTime.now());

        otpRepository.save(otp);
        return customer;
    }

    public String resend(String email) {
        final FindCustomerOtp findCustomerOtp = new FindCustomerOtp(
                customerRepository,
                otpRepository,
                email
        );

        emailService.sendOtp(findCustomerOtp.getCustomer(), genOtp());

        return "Otp Send";
    }

    public RequestOtpResponse sendForgotPin(String email) {
        final Optional<Customer> find = customerRepository.findByCustEmail(email);
        if (find.isEmpty()) {
            throw new EntityNotFoundException("Customer not found");
        }

        final Customer cust = find.get();
        create(cust);

        return new RequestOtpResponse(cust.getCustEmail());
    }

    @Transactional
    public String verifyForgotPin(VerifyOtpRequest request) {
        final FindCustomerOtp findCustomerOtp = new FindCustomerOtp(
                customerRepository,
                otpRepository,
                request.email(),
                request.otpCode()
        );

        final Customer customer = findCustomerOtp.getCustomer();
        final OtpLog otp = findCustomerOtp.getOtpLog();

        if (OffsetDateTime.now().isAfter(otp.getExpiredDate())) {
            throw new IllegalStateException("Otp is Expired");
        }

        otp.setIsUsed(true);
        otp.setUsrUpd(customer.getCustName());
        otp.setDtmUpd(OffsetDateTime.now());
        otpRepository.save(otp);

        return "Otp Verified";
    }

    private String genOtp() {
        return new DecimalFormat("0000").format(new Random().nextInt(9999));
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
                @Nullable String otpCode
        ) {
            this.customerRepository = customerRepository;
            this.otpRepository = otpRepository;
            fetch(email, otpCode);
        }

        public FindCustomerOtp(
                @NonNull CustomerRepository customerRepository,
                @NonNull OtpRepository otpRepository,
                @NonNull String email
        ) {
            this.customerRepository = customerRepository;
            this.otpRepository = otpRepository;
            fetch(email, null);
        }

        private void fetch(String email, @Nullable String otpCode) {
            final Optional<Customer> findCust = customerRepository.findByCustEmailOrderByCustIdDesc(email);
            if (findCust.isEmpty()) {
                throw new EntityNotFoundException("User doesn't exists");
            }

            this.customer = findCust.get();

            if (otpCode != null && !otpCode.isEmpty()) {
                final Optional<OtpLog> findOtp = otpRepository.findByEmailAndOtpCode(
                        customer.getCustEmail(),
                        otpCode
                );

                if (findOtp.isEmpty()) {
                    throw new IllegalStateException("Otp not valid, try to check email to entry right Otp");
                }

                this.otpLog = findOtp.get();
            } else {
                this.otpLog = otpRepository.findByEmail(this.customer.getCustEmail());
            }
        }

    }
}
