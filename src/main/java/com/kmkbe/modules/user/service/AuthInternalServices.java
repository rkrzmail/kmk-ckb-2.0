package com.kmkbe.modules.user.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.kmkbe.core.domain.constant.AuditActorType;
import com.kmkbe.core.domain.entity.RedisAttack;
import com.kmkbe.core.domain.entity.RedisLog;
import com.kmkbe.core.domain.repository.RedisAttackRepository;
import com.kmkbe.core.domain.repository.RedisRepository;
import com.kmkbe.core.exception.CommonInvalidException;
import com.kmkbe.core.service.JwtService;
import com.kmkbe.core.domain.dto.LoginDto;
import com.kmkbe.core.domain.model.RefreshToken;
import com.kmkbe.core.utils.DateTimeUtils;
import com.kmkbe.modules.common.service.refresh_token.IRefreshTokenServices;
import com.kmkbe.modules.common.service.AuditTrailService;
import com.kmkbe.modules.common.request.RefreshTokenRequest;
import com.kmkbe.core.domain.dto.BaseLdapRemoteResponseDto;
import com.kmkbe.core.domain.dto.UserInternalRemoteDto;
import com.kmkbe.modules.remote.request.ActiveDirectoryRemoteRequest;
import com.kmkbe.modules.remote.service.UserInternalRemoteService;
import com.kmkbe.modules.user.entity.MstAppRoleFormUser;
import com.kmkbe.modules.user.entity.MstEmployee;
import com.kmkbe.modules.user.entity.MstUser;
import com.kmkbe.modules.user.repository.MstAppRoleFormUserRepository;
import com.kmkbe.modules.user.repository.MstEmployeeRepository;
import com.kmkbe.modules.user.repository.MstUserRepository;
import com.kmkbe.modules.user.request.LoginInternalRequest;
import com.kmkbe.helpers.utils.Utils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthInternalServices {
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final MstUserRepository mstUserRepository;
    private final MstEmployeeRepository mstEmployeeRepository;
    private final UserInternalRemoteService userInternalRemoteService;
    private final RedisRepository redisRepository;
    private final RedisAttackRepository redisAttackRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final MstAppRoleFormUserRepository mstAppRoleFormUserRepository;
    private final AuditTrailService auditTrailService;


    @Qualifier("DbRefreshTokenServices")
    //@Qualifier("CacheRefreshTokenServices")
    private final IRefreshTokenServices refreshTokenServices;


    public LoginDto signIn(LoginInternalRequest request) throws JsonProcessingException {
        try {
            //validate dan bruce attack
            String key = "InternalsignIn:"+request.getUsername();
            int counter = 0;
            RedisAttack redisAttack ;

            // Define a formatter for parsing the dates
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime startDate;
            Optional<RedisAttack>  redisAttacks = redisAttackRepository.findTopByRedis(key);
            if (redisAttacks.isEmpty()){
                counter = 1;
                redisAttack = RedisAttack.builder()
                        .redis(key)
                        .session("")
                        .countAttack(counter)
                        .modifiedDate(DateTimeUtils.nowDate())
                        .build();
                startDate =   DateTimeUtils.nowLocal();
                redisAttackRepository.save(redisAttack);
            }else{
                redisAttack = redisAttacks.get();
                startDate = LocalDateTime.parse(Utils.formatDate(redisAttack.getModifiedDate())  , formatter);

                counter = redisAttack.getCountAttack()+1;
                redisAttack.setCountAttack(counter);
                redisAttack.setModifiedDate(DateTimeUtils.nowDate());
                redisAttackRepository.save(redisAttack);
            }


            if (counter>5){

                // Parse the input dates
                LocalDateTime endDate =  DateTimeUtils.nowLocal();

                // Calculate the duration between the dates
                Duration duration = Duration.between(startDate, endDate);
                if (duration.toMinutes() < 15){
                    throw CommonInvalidException.invalidAttack();
                }
                redisAttack.setCountAttack(1);//update 1

            }



            MstUser user;
            Optional<MstUser> findUser = mstUserRepository.findByUsername(request.getUsername());
            if (findUser.isEmpty()) {
                MstEmployee findEmployee = mstEmployeeRepository
                        .findByEmail(request.getUsername())
                        .orElseThrow(CommonInvalidException::invalidInternalUser);

                user = mstUserRepository
                        .findByEmployee(findEmployee)
                        .orElseThrow(CommonInvalidException::invalidInternalUser);
            } else {
                user = findUser.get();
            }
            String jwt ;
            LoginDto loginDto;

            if (user.getIsUserAd()) {
                if (user.getEmployee() == null) {
                    throw CommonInvalidException.invalidInternalUserEmployee();
                }

                BaseLdapRemoteResponseDto<UserInternalRemoteDto> userResponse = userInternalRemoteService.validateActiveDirectory(
                        ActiveDirectoryRemoteRequest.builder()
                                .loginID(user.getUsername())
                                .password(request.getPassword())
                                .build()
                );

                if (userResponse.getData().getUserValid()) {
                    final RefreshToken refreshTokenResult = refreshTokenServices.create(
                            IRefreshTokenServices.User.builder()
                                    .userCode(user.getUserCode())
                                    .build()
                    );


                    Optional<MstAppRoleFormUser> findPermission = mstAppRoleFormUserRepository
                            .findTopByUserOrderByAppRoleFormUserId(user);
                    MstAppRoleFormUser permission = findPermission
                            .orElseGet(() -> MstAppRoleFormUser.builder().build());
                    if (findPermission.isPresent()){
                        if (!findPermission.get().getIsActive()) {
                            throw CommonInvalidException.invalidRole();
                        }
                    }else{
                        throw CommonInvalidException.invalidRole();
                    }


                    loginDto =  new LoginDto(
                            jwtService.generateToken(user),
                            refreshTokenResult.getRefreshToken().toString(),
                            jwtService.getExpirationTime()
                    );
                    jwt = refreshTokenResult.getRefreshToken().toString();
                }else{
                    throw CommonInvalidException.invalidPassword();
                }


            }else{
                if (!bCryptPasswordEncoder.matches(request.getPassword(), user.getPassword())) {
                    throw CommonInvalidException.invalidPassword();
                }

                final RefreshToken refreshTokenResult = refreshTokenServices.create(
                        IRefreshTokenServices.User.builder()
                                .userCode(user.getUserCode())
                                .build()
                );


                Optional<MstAppRoleFormUser> findPermission = mstAppRoleFormUserRepository
                        .findTopByUserOrderByAppRoleFormUserId(user);
                MstAppRoleFormUser permission = findPermission
                        .orElseGet(() -> MstAppRoleFormUser.builder().build());
                if (findPermission.isPresent()){
                    if (!findPermission.get().getIsActive()) {
                        throw CommonInvalidException.invalidRole();
                    }
                }else{
                    throw CommonInvalidException.invalidRole();
                }

                loginDto = new LoginDto(
                        jwtService.generateToken(user),
                        refreshTokenResult.getRefreshToken().toString(),
                        jwtService.getExpirationTime()
                );
                jwt = refreshTokenResult.getRefreshToken().toString();
            }




            //benar
            redisAttack.setCountAttack(0);
            redisAttack.setModifiedDate(DateTimeUtils.nowDate());
            redisAttackRepository.save(redisAttack);

            RedisLog redis =  RedisLog.builder()
                    .redis(request.getUsername())
                    .session(jwt)
                    .build();
            redisRepository.save(redis);

            auditTrailService.recordAuthentication(
                    "INTERNAL_AUTH",
                    AuditActorType.INTERNAL,
                    user.getUsername(),
                    user.getUserCode(),
                    true,
                    null
            );

            return loginDto;
        } catch (Exception e) {
            auditTrailService.recordAuthentication(
                    "INTERNAL_AUTH",
                    AuditActorType.INTERNAL,
                    request.getUsername(),
                    null,
                    false,
                    e.getMessage()
            );
            log.error("signIn, error {}", e.getMessage());
            throw e;
        }
    }

    @Transactional
    public LoginDto refreshToken(RefreshTokenRequest request) throws Exception {
        try {
            final RefreshToken payload = refreshTokenServices.verify(request.refreshToken());
            refreshTokenServices.invalidate(payload.getRefreshToken().toString());

            /*Optional<RedisLog> redisLog = redisRepository.findFirstBySession(payload.getRefreshToken().toString());
            if (redisLog.isEmpty()){
                throw new BadCredentialsException("Invalid token, Multi Login");
            }*/

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
