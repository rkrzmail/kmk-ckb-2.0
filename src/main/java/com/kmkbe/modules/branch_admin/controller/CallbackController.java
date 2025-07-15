package com.kmkbe.modules.branch_admin.controller;

import com.kmkbe.core.domain.model.CommonResult;
import com.kmkbe.modules.branch_admin.request.CallbackRequest;
import com.kmkbe.modules.branch_admin.service.CallbackService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/digital-sign")
@RequiredArgsConstructor
public class CallbackController {
    private final CallbackService callbackService;

    @PostMapping("/callback")
    public ResponseEntity<CommonResult<String>> handleCallback(@RequestBody CallbackRequest request) {
        log.info("Received callback from eSignHub: {}", request);

        try {
            callbackService.processCallback(request);
            return ResponseEntity.ok(new CommonResult<String>().success("Callback processed successfully"));
        } catch (Exception e) {
            log.error("Error processing callback: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new CommonResult<String>().fail(400, e.getMessage()));
        }
    }
}