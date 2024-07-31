package com.kmkbe.core.model;

import org.springframework.security.core.GrantedAuthority;

import java.io.Serializable;
import java.util.Collection;

public interface JwtSimulasiModel extends Serializable {

    String getBouwheerCode();
    String getVendorCode();
    String getSignature();
    String getCreatedDateString();

}
