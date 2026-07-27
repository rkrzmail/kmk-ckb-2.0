package com.kmkbe.helpers.constant;

public abstract class LoggingConstants {
  private LoggingConstants(){}
  public static final int CORRELATION_ID_SCOPE = 99;
  public static final String CORRELATION_ID_LOG_VAR_NAME = "correlationId";
  public static final String CORRELATION_ID_HEADER_NAME = "X-Correlation-Id";
}
