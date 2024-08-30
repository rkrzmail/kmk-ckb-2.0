package com.kmkbe.modules.branch_admin.service;

import com.kmkbe.core.domain.entity.Agreement;
import com.kmkbe.core.domain.entity.AgreementFile;
import com.kmkbe.core.domain.entity.Customer;
import com.kmkbe.core.domain.entity.MstFileType;
import com.kmkbe.core.domain.repository.AgreementFileRepository;
import com.kmkbe.core.domain.repository.AgreementRepository;
import com.kmkbe.core.domain.repository.CustomerRepository;
import com.kmkbe.core.domain.repository.MstFileTypeRepository;
import com.kmkbe.core.service.FileStorageService;
import com.kmkbe.core.utils.FileUtils;
import com.kmkbe.modules.user.entity.MstUser;
import com.kmkbe.modules.user.utils.UserInternalUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AgreementService {
    private final MstFileTypeRepository mstFileTypeRepository;
    private final AgreementFileRepository agreementFileRepository;
    private final CustomerRepository customerRepository;
    private final FileStorageService fileStorageService;
    private final AgreementRepository agreementRepository;

    public void upload(
            Authentication authentication,
            MultipartFile multipartFile,
            String agreementCode
    ) throws Exception {
        try {
            final MstFileType mstFileType = mstFileTypeRepository.findByFileTypeCode("AGGREMENT01").orElseThrow(
                    () -> new IllegalArgumentException("File type not found")
            );

            final Agreement agreement = agreementRepository.findTopByAgreementCodeOrderByAgreementId(
                    agreementCode
            ).orElseThrow(() -> new IllegalArgumentException("Agreement not found"));

            AgreementFile agreementFile = agreementFileRepository.findTopByAgreementOrderByAgreementFileId(agreement)
                    .orElse(null);

            final MstUser user = UserInternalUtils.authenticateUser(authentication);

            if (agreement.getFinancingHdr() == null) {
                throw new IllegalArgumentException("Agreement Financing not found");
            }

            final String uploadDir = agreement.getFinancingHdr().getCustomer().getCustCode() + "/loan_submission";
            final String uploadName = mstFileType.getFileTypeCode() + "_" + multipartFile.getOriginalFilename();
            final String uploadedPath = fileStorageService.save(
                    multipartFile,
                    uploadDir,
                    uploadName,//,
                    null
            );

            if (agreementFile == null) {
                agreementFile = AgreementFile.builder()
                        .agreement(agreement)
                        .mstFileType(mstFileType)
                        .fileName(uploadName)
                        .filePath(FileUtils.getFilePathFromFullPath(uploadedPath))
                        .contentType(multipartFile.getContentType())
                        .usrCrt(user.getUsername())
                        .dtmCrt(Instant.now())
                        .build();

                agreementFileRepository.save(agreementFile);
            } else {
                fileStorageService.delete(agreementFile.getFilePath() + "/" + agreementFile.getFileName(), "");

                agreementFile.setFileName(uploadName);
                agreementFile.setFilePath(FileUtils.getFilePathFromFullPath(uploadedPath));
                agreementFile.setDtmUpd(Instant.now());
                agreementFile.setUsrUpd(user.getUsername());
                agreementFileRepository.save(agreementFile);
            }


        } catch (Exception e) {
            /*if (code != null && multipartFile != null && multipartFile.getOriginalFilename() != null) {
                fileStorageService.delete(multipartFile.getOriginalFilename(), code);
            }*/

            log.error("upload, error {}", e.getMessage());
            throw e;
        }
    }
}
