package com.kmkbe.core.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ConfinsUrlService {
    @Value("${csul.confins.fou.v1}")
    public String confinsFouBaseUrl;

    @Value("${csul.confins.mou.v1}")
    public String confinsMouBaseUrl;

    public ConfinsUrlService() {
    }

    public ConfinsUrlService(
            @Value("${csul.confins.fou.v1}")
            String confinsFouBaseUrl,
            @Value("${csul.confins.mou.v1}")
            String confinsMouBaseUrl
    ) {
        this.confinsFouBaseUrl = confinsFouBaseUrl;
        this.confinsMouBaseUrl = confinsMouBaseUrl;
    }

    public String CustObj_GetListKeyValueActiveByCode() {
        return confinsFouBaseUrl + "/CustObj/GetObjectByKeyAndValue";
    }

    public String RefMaster_GetListKeyValueActiveByCode() {
        return confinsFouBaseUrl + "/RefMaster/GetListKeyValueActiveByCode";
    }

    public String RefMaster_GetListActiveRefMasterWithMappingCodeAll() {
        return confinsFouBaseUrl + "/RefMaster/GetListActiveRefMasterWithMappingCodeAll";
    }

    public String Generic_GetPagingObjectBySQL() {
        return confinsMouBaseUrl + "/Generic/GetPagingObjectBySQL";
    }

}
