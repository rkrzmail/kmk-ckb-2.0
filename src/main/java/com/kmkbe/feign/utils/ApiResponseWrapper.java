package com.kmkbe.feign.utils;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ApiResponseWrapper<T> {
  private String status;

  @JsonProperty("status_code")
  private int statusCode;

  private T data;

  // Getters and Setters
  public String getStatus() { return status; }
  public void setStatus(String status) { this.status = status; }
  public int getStatusCode() { return statusCode; }
  public void setStatusCode(int statusCode) { this.statusCode = statusCode; }
  public T getData() { return data; }
  public void setData(T data) { this.data = data; }
}

