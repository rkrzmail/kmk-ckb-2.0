package com.kmkbe.core.domain.model;


import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response data setelah JWT berhasil divalidasi
 */
public class ValidationResponse {

    @JsonProperty("bouwheer")
    private String bouwheer;        // custom claim dari payload

    @JsonProperty("exp")
    private Long exp;               // expired time (unix timestamp)

    @JsonProperty("raw_header")
    private String rawHeader;       // decoded header JSON

    @JsonProperty("raw_payload")
    private String rawPayload;      // decoded payload JSON

    @JsonProperty("api_key")
    private String apiKey;          // app_key yang dipakai

    // --- Getters & Setters ---
    public String getBouwheer() { return bouwheer; }
    public void setBouwheer(String bouwheer) { this.bouwheer = bouwheer; }

    public Long getExp() { return exp; }
    public void setExp(Long exp) { this.exp = exp; }

    public String getRawHeader() { return rawHeader; }
    public void setRawHeader(String rawHeader) { this.rawHeader = rawHeader; }

    public String getRawPayload() { return rawPayload; }
    public void setRawPayload(String rawPayload) { this.rawPayload = rawPayload; }

    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }
}

