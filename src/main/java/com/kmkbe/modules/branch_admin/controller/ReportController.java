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
        Integer count = visitorData != null ? (int) visitorData.size() : 0;
        return new CommonResult<List<VisitorDto>>().successWithCount(visitorData,count);
    }

    @GetMapping("/proyeksi")
    public CommonResult<List<ProyeksiDto>> getProyeksiReport() {
        List<ProyeksiDto> proyeksiData = reportService.getProyeksiReport();
        Integer count = proyeksiData != null ? (int) proyeksiData.size() : 0;
        return new CommonResult<List<ProyeksiDto>>().successWithCount(proyeksiData,count);
    }

    @GetMapping("/duedate")
    public CommonResult<List<DuedateDto>> getDuedateReport() {
        List<DuedateDto> duedateData = reportService.getDuedateReport();
        Integer count = duedateData != null ? (int) duedateData.size() : 0;
        return new CommonResult<List<DuedateDto>>().successWithCount(duedateData,count);
    }
}