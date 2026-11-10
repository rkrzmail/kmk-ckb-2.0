package com.kmkbe.core.utils;

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

    public static String fileUlr(
            HttpServletRequest httpServletRequest,
            int id,
            DocType type
    ) {
        String url = (UriUtils.getBaseUrl(httpServletRequest)
                .replace("http", "https"))
                + "/api/v1"
                + "/documents/download/"
                + type.name()
                + "/"
                + id
                + "?token="
                + HttpUtils.getHeaderBearerToken(httpServletRequest);

        // rtoken hanya di-attach jika benar-benar ada pada request sumber;
        // jangan menghasilkan literal "&rtoken=null" di URL.
        String rtoken = HttpUtils.getHeaderRefreshToken(httpServletRequest);
        if (rtoken != null && !rtoken.isBlank()) {
            url = url + "&rtoken=" + rtoken;
        }

        return url;
    }

    public enum DocType{
        loan, agreement
    }
}
