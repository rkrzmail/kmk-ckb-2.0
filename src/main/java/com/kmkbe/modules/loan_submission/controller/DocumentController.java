package com.kmkbe.modules.loan_submission.controller;

import ch.qos.logback.core.util.StringUtil;
import com.kmkbe.core.service.JwtService;
import com.kmkbe.modules.loan_submission.service.DocumentService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
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
    private final UserDetailsService userDetailsService;
    private final DocumentService documentService;

    @GetMapping("/download/loan/{id}")
    public ResponseEntity<Resource> download(
            @PathVariable String id,
            HttpServletRequest request,
            String token
    ) {
        if (StringUtil.isNullOrEmpty(token)) {
            throw new IllegalStateException("Please provide credential for accessing this source");
        }

        IllegalStateException ex = new IllegalStateException("You dont have access to view this document, try to login first");
        final String username = jwtService.extractUsername(token);
        if (StringUtil.isNullOrEmpty(username)) {
            throw ex;
        }

        final UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        if (!jwtService.isTokenValid(token, userDetails)) {
            throw ex;
        }

        return documentService.documentByLegalFileId(
                request,
                Long.valueOf(id)
        );
    }
}
