package com.kmkbe.modules.loan_submission.service;


import com.kmkbe.modules.customer.entity.Customer;
import com.kmkbe.modules.loan_submission.entity.LegalFile;
import com.kmkbe.modules.loan_submission.entity.MstFileType;
import com.kmkbe.modules.loan_submission.repository.LegalFileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class LegalFileService {
    private final LegalFileRepository legalFileRepository;

    public LegalFile fetchByCust(Customer customer) {
        return legalFileRepository.findByCustCode(customer).orElse(null);
    }

    public LegalFile fetchByCust(Customer customer, MstFileType mstFileType) {
        return legalFileRepository.findByCustCodeAndFileTypeCode(customer, mstFileType).orElse(null);
    }

    public void create(
            Customer customer,
            MstFileType fileType,
            MultipartFile file,
            String path
    ) {
        try {
            LegalFile checkExisting = fetchByCust(customer, fileType);
            if (checkExisting != null) {
                legalFileRepository.delete(checkExisting);
            }

            LegalFile legalFile = new LegalFile();
            {
                legalFile.setCustCode(customer);
                legalFile.setFileName(file.getOriginalFilename());
                legalFile.setFilePath(path);
                legalFile.setContentType(file.getContentType());
                legalFile.setFileTypeCode(fileType);
                legalFile.setUsrCrt(customer.getCustName());
                legalFile.setDtmCrt(Instant.now());
                legalFile.setUsrUpd(customer.getCustName());
                legalFile.setDtmUpd(Instant.now());
            }

            legalFileRepository.save(legalFile);
        } catch (Exception e) {
            log.error("create, error {}", e.getMessage());
            throw e;
        }
    }
}
