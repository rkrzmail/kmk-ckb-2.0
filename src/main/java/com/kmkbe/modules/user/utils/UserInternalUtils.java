package com.kmkbe.modules.user.utils;

import com.kmkbe.core.exception.CommonInvalidException;
import com.kmkbe.modules.user.entity.MstUser;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.security.SignatureException;

public class UserInternalUtils {

    public static MstUser authenticateUser(Authentication authentication) throws SignatureException {
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            if (authentication.getPrincipal() instanceof MstUser) {
                return (MstUser) authentication.getPrincipal();
            }

            throw CommonInvalidException.cannotAccessResource();
        }

        throw new SignatureException();
    }
    public static void authenticated(Authentication authentication) throws SignatureException {
        authenticateUser(authentication);
    }


}
