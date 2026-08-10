package com.kmkbe.core.security;

import com.kmkbe.core.exception.CommonInvalidException;
import com.kmkbe.modules.customer.model.entity.Customer;
import com.kmkbe.modules.user.entity.MstUser;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.security.SignatureException;

@Service
public class CurrentUserService {

    public MstUser internalUser() throws SignatureException {
        Authentication authentication = authentication();
        if (authentication.getPrincipal() instanceof MstUser user) {
            return user;
        }

        throw CommonInvalidException.cannotAccessResource();
    }

    public Customer customer() throws SignatureException {
        Authentication authentication = authentication();
        if (authentication.getPrincipal() instanceof Customer customer) {
            return customer;
        }

        throw new SignatureException("You are not authorized to access this resource");
    }

    public void authenticatedInternalUser() throws SignatureException {
        internalUser();
    }

    private Authentication authentication() throws SignatureException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
            throw new SignatureException();
        }

        return authentication;
    }
}
