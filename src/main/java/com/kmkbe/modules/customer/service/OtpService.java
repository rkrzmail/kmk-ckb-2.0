package com.kmkbe.modules.customer.service;

import com.kmkbe.modules.common.service.EmailService;
import com.kmkbe.modules.customer.entity.Customer;
import com.kmkbe.modules.customer.entity.OtpLog;
import com.kmkbe.modules.customer.repository.CustomerRepository;
import com.kmkbe.modules.customer.repository.OtpRepository;
import com.kmkbe.modules.customer.request.VerifyOtpRequest;
import jakarta.annotation.Nullable;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpService {
    private final OtpRepository otpRepository;
    private final CustomerRepository customerRepository;
    private final EmailService emailService;

    public OtpLog create(String custName, String mobilePhone, String email) {
        final OffsetDateTime now = OffsetDateTime.now();
        final OtpLog otpLog = new OtpLog();
        otpLog.setEmail(email);
        otpLog.setMobilePhone(mobilePhone);
        otpLog.setGeneratedDate(now);
        otpLog.setExpiredDate(now.plusMinutes(5000));
        otpLog.setUsrCrt(custName);
        otpLog.setDtmCrt(now);
        otpLog.setIsUsed(false);
        otpLog.setOtpCode(genOtp());

        otpRepository.save(otpLog);
        return otpLog;
    }

    @Transactional
    public Customer verifySignUp(VerifyOtpRequest verifyOtpRequest) {
        final FindCustomerOtp findCustomerOtp = new FindCustomerOtp(
                customerRepository,
                otpRepository,
                UUID.fromString(verifyOtpRequest.custCode()),
                verifyOtpRequest.otpCode()
        );

        final Customer customer = findCustomerOtp.getCustomer();
        customer.setIsEmailValid(true);
        customer.setIsActive(true);
        customer.setUsrUpd(customer.getCustName());
        customer.setDtmUpd(OffsetDateTime.now());

        final OtpLog otp = findCustomerOtp.getOtpLog();
        if (OffsetDateTime.now().isAfter(otp.getExpiredDate())) {
            throw new IllegalStateException("Otp is Expired");
        }

        otp.setIsUsed(true);
        otp.setUsrUpd(customer.getCustName());
        otp.setDtmUpd(OffsetDateTime.now());

        otpRepository.save(otp);
        customerRepository.save(customer);

        return customer;
    }

    public void resendSignUp(String custCode) {
        final FindCustomerOtp findCustomerOtp = new FindCustomerOtp(
                customerRepository,
                otpRepository,
                UUID.fromString(custCode)
        );

        final Customer customer = findCustomerOtp.getCustomer();
        emailService.sendOtp(customer, genOtp());
    }


    private static class FindCustomerOtp {
        private final CustomerRepository customerRepository;
        private final OtpRepository otpRepository;

        private Customer customer;
        private OtpLog otpLog;

        public FindCustomerOtp(
                @NonNull CustomerRepository customerRepository,
                @NonNull OtpRepository otpRepository,
                @NonNull UUID custCode,
                @Nullable String otpCode
        ) {
            this.customerRepository = customerRepository;
            this.otpRepository = otpRepository;
            fetch(custCode, otpCode);
        }

        public FindCustomerOtp(
                @NonNull CustomerRepository customerRepository,
                @NonNull OtpRepository otpRepository,
                @NonNull UUID custCode
        ) {
            this.customerRepository = customerRepository;
            this.otpRepository = otpRepository;
            fetch(custCode, null);
        }

        private void fetch(UUID custCode, @Nullable String otpCode) {
            final Optional<Customer> findCust = customerRepository.findByCustCode(custCode);
            if (findCust.isEmpty()) {
                throw new EntityNotFoundException("Customer doesn't exists");
            }

            this.customer = findCust.get();

            if (otpCode != null && !otpCode.isEmpty()) {
                final Optional<OtpLog> findOtp = otpRepository.findByEmailAndOtpCode(
                        customer.getCustEmail(),
                        otpCode
                );

                if (findOtp.isEmpty()) {
                    throw new IllegalStateException("Otp not valid");
                }

                this.otpLog = findOtp.get();
            } else {
                this.otpLog = otpRepository.findByEmail(this.customer.getCustEmail());
            }
        }

        public Customer getCustomer() {
            return customer;
        }

        public OtpLog getOtpLog() {
            return otpLog;
        }
    }

    private String genOtp() {
        return new DecimalFormat("0000").format(new Random().nextInt(9999));
    }
}
