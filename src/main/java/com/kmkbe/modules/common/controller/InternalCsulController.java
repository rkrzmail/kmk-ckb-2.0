package com.kmkbe.modules.common.controller;

import com.kmkbe.modules.common.response.BaseCsulResponse;
import com.kmkbe.modules.common.response.CsulMailResponse;
import com.kmkbe.modules.common.service.InternalCsulService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/internal")
@RequiredArgsConstructor
public class InternalCsulController {

    private final InternalCsulService internalCsulService;

    @PostMapping("/jwt")
    public BaseCsulResponse<String> requestSisca() throws Exception {
        return internalCsulService.requestAuthJwt();
    }

    @GetMapping("/mail")
    public CsulMailResponse getEmailInfo() throws Exception {
        return internalCsulService.fetchEmailInfo();
    }

}
