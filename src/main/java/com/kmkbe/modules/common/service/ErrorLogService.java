package com.kmkbe.modules.common.service;

import com.kmkbe.core.utils.HttpUtils;
import com.kmkbe.core.utils.JsonUtils;
import com.kmkbe.modules.common.entity.ErrorLog;
import com.kmkbe.modules.common.repository.ErrorLogRepository;
import io.netty.util.internal.StringUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@Slf4j
@RequiredArgsConstructor
public class ErrorLogService {
    private final ErrorLogRepository errorLogRepository;

    public void create(
            HttpServletRequest servletRequest,
            Throwable throwable
    ) throws Exception {
        try {
            ErrorLog errorLog = new ErrorLog();

            String type = throwable.getClass().getCanonicalName();
            if (StringUtil.isNullOrEmpty(type)) {
                type = "Unknown";
            }

            final StackTraceElement thrownElement = throwable.getStackTrace()[0];

            errorLog.setErrorType(type);
            errorLog.setErrorLine(String.valueOf(thrownElement.getLineNumber()));
            errorLog.setErrorMsg(throwable.getMessage());
            errorLog.setPageUrl(servletRequest.getRequestURI());
            errorLog.setMethodName(thrownElement.getMethodName());
            errorLog.setRequestParam(JsonUtils.jsonToStr(HttpUtils.createRequestLog(servletRequest, HttpUtils.DEFAULT_MAX_PAYLOAD_LENGTH)));
            errorLog.setUsrCrt("SYSTEM");
            errorLog.setDtmCrt(Instant.now());
            errorLogRepository.save(errorLog);
        } catch (Exception e) {
            log.error("create, error {}", e.getMessage());
            throw e;
        }
    }

    public void getAll() {
    }

    public void getById(long id) {
    }
}
