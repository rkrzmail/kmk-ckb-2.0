package com.kmkbe.modules.branch_admin.request;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class FileUploadRequest {
    private String agreementCode;       // agreement no
    private String fileName;           // nama dokumen
    private String fileTypeCode;       // nomor dokumen
    private String isStamp;          // butuh materai
    private MultipartFile file;        // file content

}
