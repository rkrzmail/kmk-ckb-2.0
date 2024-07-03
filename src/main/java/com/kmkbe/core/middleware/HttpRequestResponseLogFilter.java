package com.kmkbe.core.middleware;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kmkbe.core.model.HttpLoggerPayload;
import com.kmkbe.core.service.LoggingService;
import com.kmkbe.core.utils.JsonUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.NonNull;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.AbstractRequestLoggingFilter;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.WebUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class HttpRequestResponseLogFilter extends AbstractRequestLoggingFilter {
    private final ApplicationContext applicationContext;
    private final LoggingService loggingService;

    public HttpRequestResponseLogFilter(LoggingService loggingService, ApplicationContext applicationContext) {
        setIncludeClientInfo(true);
        setIncludeQueryString(true);
        setIncludeHeaders(true);
        setIncludePayload(true);
        setMaxPayloadLength(50000);

        this.loggingService = loggingService;
        this.applicationContext = applicationContext;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    )
            throws ServletException, IOException {

        // check if this is the first request
        boolean isFirstRequest = !isAsyncDispatch(request);

        // prepare the request to use
        HttpServletRequest requestToUse = request;

        // when required, if request is not cached, create a request cache
        // wrapper to retrieve request content
        if (isIncludePayload() && isFirstRequest && !(request instanceof ContentCachingRequestWrapper)) {
            requestToUse = new HttpServletRequestCopier(request);
        }

        // when required, print request info before processing
        boolean shouldLog = shouldLog(requestToUse);
        if (shouldLog && isFirstRequest) {
            logger.info(createMessage(requestToUse, "API Request [", "]"));
        }

        // prepare response wrapper
        HttpServletResponseCopier responseToUse = new HttpServletResponseCopier(response);

        try {
            // do processing
            filterChain.doFilter(requestToUse, responseToUse);
        } finally {
            // when required, print response after processing
            if (shouldLog && !isAsyncStarted(requestToUse)) {
                logger.info(createResponseLog(responseToUse));
            }

            final Map<String, Object> requestLog = createRequestLog(requestToUse);
            final Map<String, Object> responseLog = createResponseLog(responseToUse);

            if (requestLog.get("payload") == null) {
                requestLog.put("payload", ((HttpServletRequestCopier) requestToUse).getPayload());
            }

            loggingService.create(
                    HttpLoggerPayload.builder()
                            .uri(request.getRequestURI())
                            .statusCode(response.getStatus())
                            .httpMethod(request.getMethod())
                            .request(requestLog)
                            .response(responseLog)
                            .build()
            );
        }
    }

    @Override
    protected boolean shouldLog(HttpServletRequest request) {
        return logger.isInfoEnabled();
    }

    @Override
    protected void beforeRequest(HttpServletRequest request, String message) {
        logger.info(message);
    }


    @Override
    protected void afterRequest(HttpServletRequest request, String message) {
        logger.info(message);
    }

    protected Map<String, Object> createRequestLog(HttpServletRequest request) {
        final Map<String, Object> result = new HashMap<>();

        String queryString = null,
                client = null,
                sessionId = null,
                user = null;

        String payload = null;
        HttpHeaders headers = null;

        if (isIncludeQueryString()) {
            queryString = request.getQueryString();
        }

        if (isIncludeClientInfo()) {
            client = request.getRemoteAddr();

            HttpSession session = request.getSession(false);
            if (session != null) {
                sessionId = session.getId();
            }

            user = request.getRemoteUser();
        }

        if (isIncludeHeaders()) {
            headers = new ServletServerHttpRequest(request).getHeaders();
        }

        if (isIncludePayload()) {
            HttpServletRequestCopier wrapper = WebUtils.getNativeRequest(request, HttpServletRequestCopier.class);
            if (wrapper != null) {
                byte[] buf = wrapper.getContentAsByteArray();
                if (buf.length > 0) {
                    int length = Math.min(buf.length, getMaxPayloadLength());
                    try {
                        payload = new String(buf, 0, length, wrapper.getCharacterEncoding());
                    } catch (UnsupportedEncodingException ignored) {
                    }
                }
            }
        }

        result.put("queryString", queryString);
        result.put("headers", headers);
        result.put("payload", JsonUtils.strToJson(payload));
        result.put("client", client);
        result.put("sessionId", sessionId);
        result.put("user", user);

        return result;
    }

    protected Map<String, Object> createResponseLog(HttpServletResponse response) {
        final Map<String, Object> result = new HashMap<>();

        int statusCode = response.getStatus();
        String contentType = response.getContentType(),
                payload = null;

        boolean isIncludePayload = isIncludePayload();

        if ("application/octet-stream".equals(response.getContentType())) {
            isIncludePayload = false;
        } else if ("image/gif".equals(response.getContentType())) {
            isIncludePayload = false;
        }

        if (isIncludePayload) {
            HttpServletResponseCopier wrapper = WebUtils.getNativeResponse(response, HttpServletResponseCopier.class);
            if (wrapper != null) {
                byte[] buf = wrapper.getContentAsByteArray();
                if (buf.length > 0) {
                    int length = Math.min(buf.length, getMaxPayloadLength());
                    try {
                        payload = new String(buf, 0, length, wrapper.getCharacterEncoding());
                    } catch (UnsupportedEncodingException ignored) {
                    }
                }
            }
        }

        result.put("statusCode", statusCode);
        result.put("contentType", contentType);
        result.put("payload", JsonUtils.strToJson(payload));

        return result;
    }
}
