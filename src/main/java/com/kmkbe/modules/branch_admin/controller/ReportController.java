package com.kmkbe.modules.branch_admin.controller;

import com.kmkbe.core.domain.dto.DuedateDto;
import com.kmkbe.core.domain.dto.ProyeksiDto;
import com.kmkbe.core.domain.dto.VisitorDto;
import com.kmkbe.core.domain.model.CommonResult;
import com.kmkbe.modules.branch_admin.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/report")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @GetMapping("/visitor")
    public CommonResult<List<VisitorDto>> getVisitorReport() {
        List<VisitorDto> visitorData = reportService.getVisitorReport();
        return new CommonResult<List<VisitorDto>>().success(visitorData);
    }
}