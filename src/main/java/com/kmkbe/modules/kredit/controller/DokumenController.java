package com.kmkbe.modules.kredit.controller;

import com.kmkbe.core.model.CommonResult;
import com.kmkbe.modules.kredit.service.DokumenService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/documents")
@Tag(
        name = "Documents",
        description = "Berisi endpoints yg dibutuhkan untuk proses pengajuan Kredit dari debitur, seperti legal dokumen, financing dokumen dan lain-lain"
)
@RequiredArgsConstructor
public class DokumenController {
    private final DokumenService dokumenService;

    @GetMapping("/")
    public CommonResult<Object> getAll() throws Exception {
        return new CommonResult<>().success(dokumenService.fetchAll());
    }
}
