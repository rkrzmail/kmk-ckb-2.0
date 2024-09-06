package com.kmkbe.core.middleware;

import com.kmkbe.core.service.LoggingService;
import com.kmkbe.core.utils.HttpUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.AbstractRequestLoggingFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.io.IOException;
import java.util.Map;

@Component
public class HttpRequestResponseLogFilter extends AbstractRequestLoggingFilter {
    private final ApplicationContext applicationContext;
    private final LoggingService loggingService;

    public HttpRequestResponseLogFilter(LoggingService loggingService, ApplicationContext applicationContext) {
        setIncludeClientInfo(true);
        setIncludeQueryString(true);
        setIncludeHeaders(true);
        setIncludePayload(true);
        setMaxPayloadLength(HttpUtils.DEFAULT_MAX_PAYLOAD_LENGTH);

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

        if (request.getHeader("Content-Type") != null && request.getHeader("Content-Type").contains("multipart/form-data")) {
            filterChain.doFilter(request, response);
            return;
        }

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
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            final Map<String, Object> requestLog = HttpUtils.createRequestLog(requestToUse, getMaxPayloadLength());
            final Map<String, Object> responseLog = HttpUtils.createResponseLog(responseToUse, getMaxPayloadLength());

            // when required, print response after processing
            if (shouldLog && !isAsyncStarted(requestToUse)) {
                logger.info(responseLog);
            }

            if (requestLog.get("payload") == null) {
                requestLog.put("payload", ((HttpServletRequestCopier) requestToUse).getPayload());
            }

            /*loggingService.create(
                    HttpLoggerPayload.builder()
                            .uri(request.getRequestURI())
                            .statusCode(response.getStatus())
                            .httpMethod(request.getMethod())
                            .request(requestLog)
                            .response(responseLog)
                            .build()
            );*/
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
}
