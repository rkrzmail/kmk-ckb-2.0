package com.kmkbe.core.domain.model;

import java.io.Serializable;

public interface JwtSimulasiModel extends Serializable {
    String getBouwheerCode();

    String getVendorCode();

    String getSignature();

    String getCreatedDateString();
}
