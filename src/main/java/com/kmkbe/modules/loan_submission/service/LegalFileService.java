package com.kmkbe.modules.loan_submission.service;


import com.kmkbe.core.service.FileStorageService;
import com.kmkbe.core.utils.UriUtils;
import com.kmkbe.modules.customer.entity.Customer;
import com.kmkbe.modules.loan_submission.dto.LegalFileDto;
import com.kmkbe.modules.loan_submission.entity.LegalFile;
import com.kmkbe.modules.loan_submission.entity.MstFileType;
import com.kmkbe.modules.loan_submission.mapper.FileTypeMapper;
import com.kmkbe.modules.loan_submission.repository.LegalFileRepository;
import io.netty.util.internal.StringUtil;
import jakarta.servlet.http.HttpServletRequest;
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
    private final FileStorageService fileStorageService;

    public LegalFile fetchByCust(Customer customer) {
        return legalFileRepository.findByCustCode(customer).orElse(null);
    }

    public LegalFile fetchByCust(Customer customer, MstFileType mstFileType) {
        return legalFileRepository.findByCustCodeAndFileTypeCode(customer, mstFileType).orElse(null);
    }

    public LegalFileDto create(
            HttpServletRequest httpServletRequest,
            Customer customer,
            MstFileType fileType,
            MultipartFile file,
            String path,
            String fileName
    ) throws Exception {
        try {
            if (StringUtil.isNullOrEmpty(path)) {
                throw new Exception("File path cannot be null. Expected upload dir, provided: " + path);
            }

            LegalFile checkExisting = fetchByCust(customer, fileType);
            if (checkExisting != null) {
                legalFileRepository.delete(checkExisting);
            }

            LegalFile legalFile = new LegalFile();
            {
                legalFile.setCustCode(customer);
                //legalFile.setFileName(file.getOriginalFilename());
                legalFile.setFileName(fileName);
                legalFile.setFilePath(path);
                legalFile.setContentType(file.getContentType());
                legalFile.setFileTypeCode(fileType);
                legalFile.setUsrCrt(customer.getCustName());
                legalFile.setDtmCrt(Instant.now());
                legalFile.setUsrUpd(customer.getCustName());
                legalFile.setDtmUpd(Instant.now());
            }

            legalFileRepository.save(legalFile);

            LegalFileDto dto = FileTypeMapper.INSTANCE.legalFileToDto(legalFile);
            dto.setUploadedDate(legalFile.getDtmUpd());
            dto.setFileUrl(
                    UriUtils.getBaseUrl(httpServletRequest)
                            + legalFile.getFilePath()
                            + "/"
                            + legalFile.getFileName()
            );

            return dto;
        } catch (Exception e) {
            log.error("create, error {}", e.getMessage());
            throw e;
        }
    }

    public void delete(Long id) {
        try {
            LegalFile legalFile = legalFileRepository.findById(id).orElse(null);
            if (legalFile == null) {
                return;
            }

            legalFileRepository.deleteById(id);
            fileStorageService.delete(legalFile.getFilePath(), legalFile.getFileName());
        } catch (Exception e) {
            log.error("delete, error {}", e.getMessage());
            throw e;
        }
    }
}
