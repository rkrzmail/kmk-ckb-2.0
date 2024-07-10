package com.kmkbe.modules.kredit.controller;

import com.kmkbe.modules.kredit.service.BouwheerService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/bouwheers")
@Tag(
        name = "Bouwheer",
        description = "Berisi endpoints Pemberi Kerja atau Instansi yg memperkerjakan Vendor"
)
@RequiredArgsConstructor
public class BouwheerController {
    private final BouwheerService bouwheerService;
}
