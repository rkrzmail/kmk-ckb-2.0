package com.kmkbe.modules.loan_submission.service;

import com.kmkbe.modules.loan_submission.entity.MstFileType;
import com.kmkbe.modules.loan_submission.repository.LegalFileRepository;
import com.kmkbe.modules.loan_submission.repository.MstFileTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentService {
    private final MstFileTypeRepository mstFileTypeRepository;
    private final LegalFileRepository legalFileRepository;

    public List<MstFileType> fetchAll() throws Exception {
        throw new Exception("Api not implemented yet");
    }

    public void uploadLoanDocument() throws Exception {
        try {
            throw new Exception("Api not implemented yet");
        } catch (Exception e) {
            log.error("uploadLoanDocument, error {}", e.getMessage());
            throw e;
        }
    }
}
