package com.kmkbe.modules.loan_submission.controller;

import com.kmkbe.modules.loan_submission.service.DocumentService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/documents")
@Tag(
        name = "Documents",
        description = "Berisi endpoints data dokumen"
)
@RequiredArgsConstructor
public class DocumentController {
    private final DocumentService documentService;

    /**
     * Download dokumen loan. Diamankan untuk issue C10:
     * - Tidak lagi menerima token via query param (bocor ke log).
     * - Autentikasi wajib via SecurityContext (filter tidak lagi whitelist path ini).
     * - Otorisasi kepemilikan diverifikasi di DocumentService terhadap
     *   customer login atau user internal berwenang.
     */
    @GetMapping("/download/loan/{id}")
    public ResponseEntity<Resource> download(
            @PathVariable String id,
            HttpServletRequest request
    ) throws Exception {
        return documentService.documentByLegalFileId(
                request,
                Long.valueOf(id)
        );
    }

    @GetMapping("/download/agreement/{id}")
    public ResponseEntity<Resource> downloadAgreement(
            @PathVariable String id,
            HttpServletRequest request
    ) throws Exception {
        return documentService.agreementDocByAgreementId(
                request,
                Long.valueOf(id)
        );
    }
}
