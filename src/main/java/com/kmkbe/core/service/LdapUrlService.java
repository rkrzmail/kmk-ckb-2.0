package com.kmkbe.core.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class LdapUrlService {
    @Value("${csul.sisca.v1.internal}")
    public String siscaBaseUrlPrivate;

    @Value("${csul.sisca.v1.auth.username}")
    public String authHeaderUsername;

    @Value("${csul.sisca.v1.auth.password}")
    public String authHeaderPassword;

    public LdapUrlService() {

    }

    public LdapUrlService(
            @Value("${csul.sisca.v1.internal}")
            String siscaBaseUrlPrivate,
            @Value("${csul.sisca.v1.auth.username}")
            String authHeaderUsername,
            @Value("${csul.sisca.v1.auth.password}")
            String authHeaderPassword
    ) {
        this.siscaBaseUrlPrivate = siscaBaseUrlPrivate;
        this.authHeaderUsername = authHeaderUsername;
        this.authHeaderPassword = authHeaderPassword;
    }


    public String auth_requestJwt() {
        return siscaBaseUrlPrivate + "/auth/req-jwt";
    }

    public String activeDirectory_validation() {
        return siscaBaseUrlPrivate + "/activedirectory/validation";
    }
}
