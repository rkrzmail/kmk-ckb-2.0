package com.kmkbe.core.domain.model;

import com.kmkbe.core.utils.DateTimeUtils;
import lombok.*;
import org.bson.Document;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Map;

@Getter
@Builder
public class HttpLoggerPayload {
    private String uri;
    private String httpMethod;
    private Integer statusCode;
    private Map<String, Object> request;
    private Map<String, Object> response;
    private String controller;

    public Document toCollection() {
        Document doc = new Document();
        doc.put("uri", uri);
        doc.put("httpMethod", httpMethod);
        doc.put("statusCode", statusCode);
        doc.put("request", request);
        doc.put("response", response);
        doc.put("controller", controller);
        doc.put("timestamp", DateTimeUtils.nowDate());
        return doc;
    }
}
