package com.kmkbe.modules.loan_submission.controller;

import com.kmkbe.core.service.JwtService;
import com.kmkbe.modules.loan_submission.service.DocumentService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetailsService;
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
    private final JwtService jwtService;

    @Autowired
    @Qualifier("internalUserDetailService")
    private UserDetailsService internalUserDetails;

    @Autowired
    @Qualifier("userDetailsService")
    private UserDetailsService customerUserDetails;

    private final DocumentService documentService;

    @GetMapping("/download/loan/{id}")
    public ResponseEntity<Resource> download(
            @PathVariable String id,
            HttpServletRequest request
    ) {
        return documentService.documentByLegalFileId(
                request,
                Long.valueOf(id)
        );
    }

    @GetMapping("/download/agreement/{id}")
    public ResponseEntity<Resource> downloadAgreement(
            @PathVariable String id,
            HttpServletRequest request
    ) {
        return documentService.agreementDocByAgreementId(
                request,
                Long.valueOf(id)
        );
    }
}
