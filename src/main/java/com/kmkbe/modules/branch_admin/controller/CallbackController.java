package com.kmkbe.modules.branch_admin.controller;

import com.kmkbe.core.domain.model.CommonResult;
import com.kmkbe.modules.branch_admin.request.CallbackRequest;
import com.kmkbe.modules.branch_admin.service.CallbackService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/digital-sign")
@RequiredArgsConstructor
public class CallbackController {
    private final CallbackService callbackService;

    @Value("${app.api.key}")
    private String expectedApiKey;

    @PostMapping("/callback")
    public ResponseEntity<CommonResult<String>> handleCallback(
            @RequestHeader("x-api-key") String apiKey,
            @RequestBody CallbackRequest request) {

        if (!expectedApiKey.equals(apiKey)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new CommonResult<String>().fail(401, "Invalid API Key"));
        }

        try {
            callbackService.processCallback(request);
            return ResponseEntity.ok(new CommonResult<String>().success("Callback processed successfully"));
        } catch (Exception e) {
            log.error("Error processing callback: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new CommonResult<String>().fail(400, e.getMessage()));
        }
    }

}