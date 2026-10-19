package com.kmkbe.core.utils;

import com.kmkbe.core.domain.entity.LegalFile;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

public class UriUtils {

    public static String getBaseUrl(HttpServletRequest request) {
        return ServletUriComponentsBuilder.fromRequestUri(request)
                .replacePath(null)
                .build()
                .toUriString();



    }

    public static String getDomainUrl(HttpServletRequest request){
        return getBaseUrl(request)
                .replace("http://", "")
                .replace("https://", "");
    }

    /**
     * Membangun URL download dokumen tanpa menyematkan token di query string (C10).
     * Token via query bocor ke access-log, browser history, dan referer header.
     * Klien wajib mengakses URL ini dengan header Authorization: Bearer.
     */
    public static String fileUlr(
            HttpServletRequest httpServletRequest,
            int id,
            DocType type
    ) {
        return (UriUtils.getBaseUrl(httpServletRequest)
                .replace("http", "https"))
                + "/api/v1"
                + "/documents/download/"
                + type.name()
                + "/"
                + id;
    }

    public enum DocType{
        loan, agreement
    }
}
