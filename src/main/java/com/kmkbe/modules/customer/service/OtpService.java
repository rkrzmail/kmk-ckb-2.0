package com.kmkbe.modules.customer.service;

import com.kmkbe.core.domain.dto.RequestOtpDto;
import com.kmkbe.modules.customer.model.entity.Customer;
import com.kmkbe.core.domain.entity.OtpLog;
import com.kmkbe.modules.customer.repository.CustomerRepository;
import com.kmkbe.core.domain.repository.OtpRepository;
import com.kmkbe.modules.common.service.EmailService;
import com.kmkbe.modules.customer.model.request.RequestOtpRequest;
import com.kmkbe.modules.customer.model.request.VerifyOtpRequest;
import com.kmkbe.modules.user.utils.Utils;
import jakarta.annotation.Nullable;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpService {
  private final OtpRepository otpRepository;
  private final CustomerRepository customerRepository;
  private final EmailService emailService;
  private final CustomerService customerService;
  private final BCryptPasswordEncoder bcryptEncoder;
  private final Clock clock;
  private final OtpGenerator otpGenerator;

  public OtpLog create(@NonNull Customer customer, @NonNull OtpType type) throws Exception {
    final LocalDateTime now = now();
    final OtpLog otpLog = new OtpLog();
    otpLog.setEmail(customer.getCustEmail());
    otpLog.setMobilePhone(customer.getCustMobilePhone());
    otpLog.setGeneratedDate(now);
    otpLog.setExpiredDate(now.plus(5, ChronoUnit.MINUTES));
    otpLog.setUsrCrt(customer.getCustName());
    otpLog.setDtmCrt(now());
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
    if (now().isAfter(otp.getExpiredDate())) {
      throw new IllegalStateException("Otp is Expired");
    }

    otp.setIsUsed(true);
    otp.setUsrUpd(customer.getCustName());
    otp.setDtmUpd(now());

    otpRepository.save(otp);
    return "Sign up successfully";
  }

  public RequestOtpDto sendForgotPin(String email) throws Exception {
    final Optional<Customer> find = customerRepository.findByCustEmail(email);
    if (find.isEmpty()) {
      //throw new EntityNotFoundException("Customer not found, enter an valid email");

      return new RequestOtpDto(
        genRequestId(email, Utils.RND()),
        email,
        now().plus(5, ChronoUnit.MINUTES)
      );
    } else {
      final Customer cust = find.get();
      final OtpLog otpLog = create(cust, OtpType.CHANGE_PIN);

      return new RequestOtpDto(
        genRequestId(cust, otpLog),
        cust.getCustEmail(),
        otpLog.getExpiredDate()
      );
    }


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
        /*if (!otp.getOtpCode().equalsIgnoreCase(request.otp())){
            throw new IllegalStateException("Request id not valid, try to enter valid requestId");
        }*/


    if (now().isAfter(otp.getExpiredDate())) {
      throw new IllegalStateException("Otp is expired, try to resend again");
    }

    otp.setIsUsed(true);
    otp.setUsrUpd(customer.getCustName());
    otp.setDtmUpd(now());
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
    return otpGenerator.generate();
  }

  public String genRequestId(Customer cust, OtpLog otpLog) {
    return bcryptEncoder.encode(cust.getCustEmail() + otpLog.getOtpLogId());
  }

  public String genRequestId(String email, String otpLog) {
    return bcryptEncoder.encode(email + otpLog);
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

  // New Method to generate OTP without customer lookup
  @Transactional
  public OtpLog generateOtpForEmail(String email, OtpType type) throws Exception {
    final LocalDateTime now = now();
    final OtpLog otpLog = new OtpLog();
    otpLog.setEmail(email); // Directly set the email
    otpLog.setGeneratedDate(now);
    otpLog.setExpiredDate(now.plus(5, ChronoUnit.MINUTES)); // OTP expires in 5 minutes
    otpLog.setIsUsed(false);
    otpLog.setOtpCode(genOtp()); // Generate OTP

    // Set a default value for 'usr_crt' field (e.g., "system")
    otpLog.setUsrCrt("system");  // Set a default value for user who created the OTP
    otpLog.setDtmCrt(now);

    otpRepository.save(otpLog);

    // Send OTP based on the type (SIGNUP or CHANGE_PIN)
    if (type == OtpType.SIGNUP) {
      emailService.sendOtp2(email, otpLog.getOtpCode()); // Send OTP to the provided email
    } else if (type == OtpType.CHANGE_PIN) {
      emailService.sendOtpChangePin2(email, otpLog.getOtpCode()); // Send PIN change OTP email
    }

    return otpLog;
  }

  // Resend OTP functionality (same as create, but without customer validation)
  public RequestOtpDto resendOtpForEmail(String email, OtpType type) throws Exception {
    final OtpLog otpLog = generateOtpForEmail(email, type);  // Directly generate OTP without customer validation

    return new RequestOtpDto(
      genRequestId2(email, otpLog),  // Generate request ID for OTP
      email,
      otpLog.getExpiredDate()  // OTP expiration date
    );
  }

  public String genRequestId2(String email, OtpLog otpLog) {
    return bcryptEncoder.encode(email + otpLog.getOtpLogId()); // Generating request ID
  }

  @Transactional
  public String verifyOtp(String email, String otpCode) throws Exception {
    final Optional<OtpLog> otpLogOptional = otpRepository.findTopByEmailAndOtpCodeOrderByDtmCrtDesc(email, otpCode);

    if (otpLogOptional.isEmpty()) {
      throw new IllegalStateException("No OTP found for this email.");
    }

    OtpLog otpLog = otpLogOptional.get();

    // Check if the OTP is expired
    if (now().isAfter(otpLog.getExpiredDate())) {
      throw new IllegalStateException("OTP has expired.");
    }

    // Compare the entered OTP with the OTP stored in the database
    if (!otpLog.getOtpCode().equals(otpCode)) {
      throw new IllegalStateException("Incorrect OTP.");
    }

    // Mark the OTP as used
    otpLog.setIsUsed(true);
    otpLog.setUsrUpd("system"); // Or any user identifier
    otpLog.setDtmUpd(now());

    otpRepository.save(otpLog); // Save the updated OTP log

    return "OTP verification successful!";
  }

  private LocalDateTime now() {
    return LocalDateTime.now(clock);
  }
}
