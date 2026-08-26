package com.kmkbe.feign.exception;

public class VendorNotFoundException extends RuntimeException {
  public VendorNotFoundException(String message) {
    super(message);
  }
}
