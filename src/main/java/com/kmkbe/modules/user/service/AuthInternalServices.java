package com.kmkbe.modules.user.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.kmkbe.core.exception.CommonInvalidException;
import com.kmkbe.core.service.JwtService;
import com.kmkbe.modules.common.dto.LoginDto;
import com.kmkbe.modules.common.model.RefreshToken;
import com.kmkbe.modules.common.service.refresh_token.IRefreshTokenServices;
import com.kmkbe.modules.common.request.RefreshTokenRequest;
import com.kmkbe.modules.remote.dto.BaseLdapRemoteResponseDto;
import com.kmkbe.modules.remote.dto.UserInternalRemoteDto;
import com.kmkbe.modules.remote.request.ActiveDirectoryRemoteRequest;
import com.kmkbe.modules.remote.service.UserInternalRemoteService;
import com.kmkbe.modules.user.entity.MstEmployee;
import com.kmkbe.modules.user.entity.MstUser;
import com.kmkbe.modules.user.repository.MstEmployeeRepository;
import com.kmkbe.modules.user.repository.MstUserRepository;
import com.kmkbe.modules.user.request.LoginInternalRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthInternalServices {
    private final MstUserRepository mstUserRepository;
    private final MstEmployeeRepository mstEmployeeRepository;
    private final UserInternalRemoteService userInternalRemoteService;

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Qualifier("DbRefreshTokenServices")
    //@Qualifier("CacheRefreshTokenServices")
    private final IRefreshTokenServices refreshTokenServices;


    public LoginDto signIn(LoginInternalRequest request) throws JsonProcessingException {
        try {
            MstUser user;
            Optional<MstUser> findUser = mstUserRepository.findByUsername(request.getUsername());
            if (findUser.isEmpty()) {
                MstEmployee findEmployee = mstEmployeeRepository
                        .findByEmail(request.getUsername())
                        .orElseThrow(CommonInvalidException::invalidInternalUser);

                user = mstUserRepository
                        .findByEmployeeCode(findEmployee)
                        .orElseThrow(CommonInvalidException::invalidInternalUser);
            } else {
                user = findUser.get();
            }

            /*if (!user.getPassword().equals(bcryptEncoder.encode(request.getPassword()))) {
                throw new RuntimeException("Check your email or username or password");
            }

            if (user.getIsUserNonad()) {

            }

            if (user.getIsUserAd()) {

            }*/

            BaseLdapRemoteResponseDto<UserInternalRemoteDto> userResponse = userInternalRemoteService.validateActiveDirectory(
                    ActiveDirectoryRemoteRequest.builder()
                            .loginID(user.getUsername())
                            .password(request.getPassword())
                            .build()
            );

            final RefreshToken refreshTokenResult = refreshTokenServices.create(
                    IRefreshTokenServices.User.builder()
                            .userCode(user.getUserCode())
                            .build()
            );

            if (userResponse.getData().getUserValid()) {
               /* Authentication authentication = authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                request.getEmailOrUsername(),
                                request.getPassword()
                        )
                );

                SecurityContextHolder.getContext().setAuthentication(authentication);*/

                return new LoginDto(
                        jwtService.generateToken(user),
                        refreshTokenResult.getRefreshToken().toString(),
                        jwtService.getExpirationTime()
                );
            }

            throw CommonInvalidException.invalidInternalUser();
        } catch (Exception e) {
            log.error("signIn, error {}", e.getMessage());
            throw e;
        }
    }

    @Transactional
    public LoginDto refreshToken(RefreshTokenRequest request) throws Exception {
        try {
            final RefreshToken payload = refreshTokenServices.verify(request.refreshToken());
            refreshTokenServices.invalidate(payload.getRefreshToken().toString());

            MstUser user = mstUserRepository.findByUserCode(payload.getUserCode())
                    .orElseThrow(() -> new IllegalStateException("Invalid Refresh Token, Entire Internal User doesn't exists. Try to login again."));

            final RefreshToken refreshTokenResult = refreshTokenServices.create(
                    IRefreshTokenServices.User.builder()
                            .userCode(user.getUserCode())
                            .build()
            );

            return new LoginDto(
                    jwtService.generateToken(user),
                    refreshTokenResult.getRefreshToken().toString(),
                    jwtService.getExpirationTime()
            );
        } catch (Exception e) {
            log.error("refreshToken: {}", e.getMessage());
            throw e;
        }
    }
}
