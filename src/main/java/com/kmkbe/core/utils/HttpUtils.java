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
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public class HttpUtils {

  public static final int DEFAULT_MAX_PAYLOAD_LENGTH = 50000;
  public static final String MASKED_VALUE = "[REDACTED]";

  // Header yang nilainya tidak boleh muncul di log (dibandingkan tanpa '-'/'_')
  private static final Set<String> SENSITIVE_HEADERS = Set.of(
    "authorization", "proxyauthorization", "apikey", "xapikey",
    "cookie", "setcookie", "rtoken", "xauthtoken"
  );

  // Field body (request/response) yang nilainya tidak boleh muncul di log
  private static final Set<String> SENSITIVE_FIELDS = Set.of(
    "password", "currentpassword", "newpassword", "confirmpassword", "passwordconfirmation",
    "pin", "otp", "token", "accesstoken", "refreshtoken", "idtoken", "jwt",
    "secret", "secretkey", "apikey", "authorization", "credential", "credentials",
    "privatekey", "cardnumber", "cvv"
  );

  // Fallback untuk payload non-JSON (mis. form-urlencoded)
  private static final Pattern SENSITIVE_URL_PARAM = Pattern.compile(
    "(?i)(password|pin|otp|token|secret|api[_-]?key|authorization)=([^&\\s\"]*)"
  );

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

    result.put("queryString", sanitizeQueryString(queryString));
    result.put("headers", sanitizeHeaders(headers));
    result.put("payload", ObjectUtils.strToJson(sanitizePayload(payload)));
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
    result.put("payload", ObjectUtils.strToJson(sanitizePayload(payload)));

    return result;
  }

  /**
   * Header dengan nilai kredensial (Authorization, API-Key, Cookie, dll)
   * tidak boleh ditulis apa adanya ke log.
   */
  public static HttpHeaders sanitizeHeaders(HttpHeaders headers) {
    HttpHeaders sanitized = new HttpHeaders();
    if (headers == null) {
      return sanitized;
    }
    headers.forEach((name, values) -> {
      if (isSensitiveHeader(name)) {
        sanitized.put(name, List.of(MASKED_VALUE));
      } else {
        sanitized.put(name, values);
      }
    });
    return sanitized;
  }

  /**
   * Payload JSON yang berisi field sensitif (password, otp, token, dll)
   * di-mask sebelum masuk log. Payload non-JSON disanitasi sebagai query param.
   */
  public static String sanitizePayload(String payload) {
    if (payload == null || payload.isEmpty()) {
      return payload;
    }
    Map<String, Object> json = ObjectUtils.strToJson(payload);
    if (json != null) {
      try {
        maskMap(json);
        return ObjectUtils.jsonToStr(json);
      } catch (Exception ignored) {
        // jika gagal serialisasi ulang, jangan tampilkan payload mentah
        return MASKED_VALUE;
      }
    }
    return SENSITIVE_URL_PARAM.matcher(payload).replaceAll("$1=" + MASKED_VALUE);
  }

  public static String sanitizeQueryString(String queryString) {
    if (queryString == null || queryString.isEmpty()) {
      return queryString;
    }
    return SENSITIVE_URL_PARAM.matcher(queryString).replaceAll("$1=" + MASKED_VALUE);
  }

  private static boolean isSensitiveHeader(String name) {
    return name != null
      && SENSITIVE_HEADERS.contains(name.toLowerCase(Locale.ROOT).replace("-", "").replace("_", ""));
  }

  private static boolean isSensitiveField(String name) {
    return name != null
      && SENSITIVE_FIELDS.contains(name.toLowerCase(Locale.ROOT).replace("-", "").replace("_", ""));
  }

  @SuppressWarnings("unchecked")
  private static void maskMap(Map<String, Object> json) {
    for (Map.Entry<String, Object> entry : json.entrySet()) {
      Object value = entry.getValue();
      if (isSensitiveField(entry.getKey())) {
        if (value != null) {
          entry.setValue(MASKED_VALUE);
        }
      } else if (value instanceof Map<?, ?> nested && !nested.isEmpty()) {
        maskMap((Map<String, Object>) nested);
      } else if (value instanceof List<?> list) {
        list.forEach(item -> {
          if (item instanceof Map<?, ?> nested && !nested.isEmpty()) {
            maskMap((Map<String, Object>) nested);
          }
        });
      }
    }
  }

  public static String getHeaderBearerToken(HttpServletRequest request) {
    final String authHeader = getHeaderAuthorization(request);

    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      return null;
    }

    return authHeader.substring("Bearer ".length());
  }

  public static String getHeaderAuthorization(HttpServletRequest request) {
    return request.getHeader("Authorization");
  }

  public static String getHeaderApiKey(HttpServletRequest request) {
    return request.getHeader("API-Key");
  }
}
