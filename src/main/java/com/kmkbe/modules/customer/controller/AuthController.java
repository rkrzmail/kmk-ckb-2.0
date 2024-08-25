package com.kmkbe.modules.customer.controller;

import com.kmkbe.core.exception.CommonInvalidException;
import com.kmkbe.core.domain.model.CommonResult;
import com.kmkbe.core.domain.dto.LoginDto;
import com.kmkbe.core.domain.constant.CompanyModel;
import com.kmkbe.core.domain.constant.CustomerType;
import com.kmkbe.core.domain.dto.RequestOtpDto;
import com.kmkbe.core.domain.entity.Customer;
import com.kmkbe.core.domain.entity.OtpLog;
import com.kmkbe.modules.customer.request.ForgotPinRequest;
import com.kmkbe.modules.customer.request.LoginRequest;
import com.kmkbe.modules.common.request.RefreshTokenRequest;
import com.kmkbe.modules.customer.request.SignUpRequest;
import com.kmkbe.modules.customer.service.*;
import com.kmkbe.core.domain.dto.InquiryVendorRemoteDto;
import com.kmkbe.modules.remote.service.CustomerRemoteService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.*;

import java.security.SignatureException;
import java.time.Instant;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(
        name = "Auhtentication",
        description = "Customer Authentication Endpoints"
)
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final SecurityContextLogoutHandler logoutHandler;
    private final CustomerService customerService;
    private final CustomerCompanyService customerCompanyService;
    private final CustomerPersonalService customerPersonalService;
    private final OtpService otpService;
    private final CustomerRemoteService customerRemoteService;

    @Transactional
    @PostMapping("/sign-up")
    public CommonResult<RequestOtpDto> signUp(
            @Valid @RequestBody SignUpRequest request
    ) throws Exception {
        final InquiryVendorRemoteDto vendor;
        try {
            vendor = customerRemoteService.inquiryVendor(request.getVendorCode()).getData();
        } catch (Exception e) {
            throw CommonInvalidException.builder()
                    .title("Perusahaan Tidak Ditemukan")
                    .message("Mohon maaf, saat ini Anda belum dapat menggunakan " +
                            "Dana Sakti. Harap melakukan pengecekan ulang " +
                            "dengan pihak PT. Trakindo Utama.")
                    .build();
        }

        final CustomerType type;
        if (request.getCustomerType().equalsIgnoreCase("perusahaan")) {
            type = CustomerType.Company;
            String address = "";
            if (
                    vendor.getVendorBuilding() != null
                            && !vendor.getVendorBuilding().isEmpty()
            ) {
                address = vendor.getVendorBuilding().getFirst().getAddressInfo();
            }

            request.setCompany(
                    SignUpRequest.Company.builder()
                            .companyModel(CompanyModel.PT)
                            .companyType("Perseroan Terbatas")
                            .identityType("AKTA")
                            .identityNo(request.getCustomerIdNo())
                            .identityIssuedDate(Instant.now())
                            .identityExpiredDate(Instant.now())
                            .companyAddress(address)
                            .rt("")
                            .rw("")
                            .kelurahan("")
                            .kecamatan("")
                            .city("")
                            .province("")
                            .zipCode("")
                            .area("")
                            .phone("")
                            .ownershipStatus("")
                            .staySince(Instant.now())
                            .build()
            );
        } else if (request.getCustomerType().equalsIgnoreCase("perorangan")) {
            type = CustomerType.Personal;
        } else {
            throw new Exception("Tipe Debitur is not valid or is not in list");
        }

        final Customer cust = customerService.create(request, vendor, type);
        if (type == CustomerType.Company) {
            customerCompanyService.create(cust, request.getCompany());
        } else {
            customerPersonalService.create(cust, request.getPersonal());
        }

        final OtpLog otpLog = otpService.create(cust, OtpService.OtpType.SIGNUP);

        return new CommonResult<RequestOtpDto>().success(
                new RequestOtpDto(
                        otpService.genRequestId(cust, otpLog),
                        cust.getCustEmail(),
                        otpLog.getExpiredDate()
                )
        );
    }

    @PostMapping("/sign-in")
    public CommonResult<LoginDto> signIn(
            @Valid @RequestBody LoginRequest request
    ) {
        return new CommonResult<LoginDto>().success(authService.signIn(request));
    }

    @PutMapping("/forgot-pin")
    public CommonResult<Object> forgotPin(
            @Valid @RequestBody ForgotPinRequest request
    ) {
        final String message = authService.forgotPin(request);
        return new CommonResult<>().success(null, message);
    }

    @DeleteMapping("/sign-out")
    public CommonResult<Object> signOut(
            Authentication authentication,
            HttpServletRequest request,
            HttpServletResponse response
    ) throws SignatureException, IllegalStateException {
        final String result = authService.logout(authentication);
        logoutHandler.logout(request, response, authentication);
        return new CommonResult<>().success(
                null,
                result
        );
    }

    /* Unsecure refresh token */
   /* @PostMapping("/refresh-token")
    public CommonResult<RefreshTokenDto> refreshToken(HttpServletRequest request) {
        // claims object was passing in header from middleware (JwtAuthenticationFilter)
        DefaultClaims claims = (DefaultClaims) request.getAttribute("claims");
        Map<String, Object> expectedMap = new HashMap<>(claims);
        String token = jwtService.generateRefreshToken(expectedMap, expectedMap.get("sub").toString());
        return new CommonResult<RefreshTokenDto>().success(
                new RefreshTokenDto(
                        token,
                        jwtService.getRefreshTokenExpirationTime()
                )
        );
    }*/

    @PostMapping("/refresh-token")
    public CommonResult<LoginDto> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request
    ) throws Exception {
        return new CommonResult<LoginDto>().success(
                authService.refreshToken(request)
        );
    }
}
