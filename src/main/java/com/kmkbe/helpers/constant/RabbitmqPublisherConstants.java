package com.kmkbe.helpers.constant;

public final class RabbitmqPublisherConstants {

  private RabbitmqPublisherConstants() {

  }

  public static final String EXCEPTION_NULL_PAYLOAD_MESSAGE = "Payload is null";
  public static final String HEADER_MESSAGE_ID = "X-Idempotency-Key";
  public static final String HEADER_RETURNED_MESSAGE_CORRELATION = "spring_returned_message_correlation";
  public static final String PUBLISHED_SUCCESS = "PUBLISHED";
  public static final String PUBLISHED_FAILED = "FAILED";
  public static final String PUBLISHED_PENDING = "PENDING";
  public static final String RETURNED_REPLY_TEXT_NO_ROUTE = "NO_ROUTE";
  public static final String NOTE_NO_ROUTE = "No route (invalid exchange / routing key)";

}
