package com.kmkbe.core.utils;

import com.kmkbe.core.middleware.HttpServletRequestCopier;
import com.kmkbe.core.middleware.HttpServletResponseCopier;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.util.WebUtils;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class HttpUtils {

    public static final int DEFAULT_MAX_PAYLOAD_LENGTH = 50000;

    public static Map<String, Object> createRequestLog(
            HttpServletRequest request,
            int payloadLength
    ) {
        final Map<String, Object> result = new HashMap<>();

        String queryString = request.getQueryString(),
                client = request.getRemoteAddr(),
                sessionId = null,
                user = request.getRemoteUser();

        String payload = null;
        HttpHeaders headers = new ServletServerHttpRequest(request).getHeaders();

        HttpSession session = request.getSession(false);
        if (session != null) {
            sessionId = session.getId();
        }

        if (
                request.getContentType() != null
                        && !request.getContentType().equals("multipart/form-data")
        ) {
            HttpServletRequestCopier wrapper = WebUtils.getNativeRequest(request, HttpServletRequestCopier.class);
            if (wrapper != null) {
                byte[] buf = wrapper.getContentAsByteArray();
                if (buf.length > 0) {
                    int length = Math.min(buf.length, payloadLength);
                    try {
                        payload = new String(buf, 0, length, wrapper.getCharacterEncoding());
                    } catch (UnsupportedEncodingException ignored) {
                    }
                }
            }
        }

        result.put("queryString", queryString);
        result.put("headers", headers);
        result.put("payload", ObjectUtils.strToJson(payload));
        result.put("client", client);
        result.put("sessionId", sessionId);
        result.put("user", user);

        return result;
    }

    public static Map<String, Object> createResponseLog(
            HttpServletResponse response,
            int payloadLength
    ) {
        final Map<String, Object> result = new HashMap<>();

        final int statusCode = response.getStatus();
        final String contentType = response.getContentType();
        String payload = null;

        if (
                response.getContentType() != null
                        && (
                                response.getContentType().equals("application/octet-stream")
                                        || response.getContentType().equals("application/json")
                )
        ) {
            HttpServletResponseCopier wrapper = WebUtils.getNativeResponse(response, HttpServletResponseCopier.class);
            if (wrapper != null) {
                byte[] buf = wrapper.getContentAsByteArray();
                if (buf.length > 0) {
                    int length = Math.min(buf.length, payloadLength);
                    try {
                        payload = new String(buf, 0, length, wrapper.getCharacterEncoding());
                    } catch (UnsupportedEncodingException ignored) {
                    }
                }
            }
        }

        result.put("statusCode", statusCode);
        result.put("contentType", contentType);
        result.put("payload", ObjectUtils.strToJson(payload));

        return result;
    }
}
