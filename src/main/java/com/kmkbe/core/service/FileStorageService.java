package com.kmkbe.core.service;

import com.kmkbe.core.utils.FileUtils;
import io.netty.util.internal.StringUtil;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
@Slf4j
@RequiredArgsConstructor
public class FileStorageService {
    private final ServletContext context;

    private final Path root = Paths.get("uploads");

    public String save(MultipartFile file, String uploadDir, String name) throws Exception {
        return save(file, uploadDir, name, null);
    }

    /**
     * @return full path of uploaded file
     */
    public String save(
            final MultipartFile file,
            String uploadDir,
            String name,
            String extValidation
    ) throws Exception {
        try {
            if (file == null) {
                throw new Exception("File cannot be null");
            }

            if (file.getOriginalFilename() == null) {
                throw new Exception("File cannot be null");
            }

            String ext = FileUtils.getUploadFileExtension(file);
            if (StringUtil.isNullOrEmpty(ext)) {
                throw new Exception("File format or extension is not valid, try to upload valid file. Uploaded File: " + name);
            }

            if (ext.equals(ext.toUpperCase())) {
                ext = ext.toLowerCase();
            }

            /*if (extValidation != null && !ext.equalsIgnoreCase(extValidation)) {
                throw new Exception("File extension is not valid, expected: " + extValidation + " but got: " + ext);
            }*/

            String fileNameExt = FileUtils.getFileNameExtension(name);
            if (StringUtil.isNullOrEmpty(fileNameExt)) {
                throw new Exception("File format or extension is not valid, try to upload valid file. Uploaded File: " + name);
            }
            if (fileNameExt.equals(fileNameExt.toUpperCase())) {
                name = name.substring(0, name.lastIndexOf(".")) + "." + ext;
            }

            Path uploadPath = root.resolve(uploadDir);
            if (!uploadPath.toFile().exists()) {
                Files.createDirectories(uploadPath);
            }

            Path fileDestination = uploadPath.resolve(Paths.get(name))
                    .normalize()
                    .toAbsolutePath();

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, fileDestination, StandardCopyOption.REPLACE_EXISTING);

                return "/uploads/" + uploadDir + "/" + name;
            } catch (Exception e) {
                throw new RuntimeException("Could not store the file. Error: " + e.getMessage());
            }

        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public Resource load(String filename) {
        try {
            Path file = root.resolve(filename);
            Resource resource = new UrlResource(file.toUri());

            if (resource.exists() || resource.isReadable()) {
                return resource;
            }

            throw new RuntimeException("Could not read the file!");
        } catch (MalformedURLException e) {
            throw new RuntimeException("Error: " + e.getMessage());
        }
    }

    public boolean delete(String filename, String code) {
        try {
            if (filename.startsWith("/uploads/")) {
                filename = filename.replace("/uploads/", "");
            }

            Path file = root.resolve(filename);
            boolean res = Files.deleteIfExists(file);
            return res;
        } catch (IOException e) {
            throw new RuntimeException("Error: " + e.getMessage());
        }
    }

    public ResponseEntity<Resource> downloadUploadFile(
            HttpServletRequest httpServletRequest,
            String filePath,
            String fileName
    ) {
        String filePathStr = filePath + "/" + fileName;
        if (filePathStr.contains("uploads")) {
            filePathStr = filePathStr.replace("/uploads/", "");
        }

        try {
            Path paths = Paths.get("uploads", filePathStr);
            Resource resource = new UrlResource(paths.toUri());
            String contentType = httpServletRequest
                    .getServletContext()
                    .getMimeType(resource.getFile().getAbsolutePath());

            if (resource.exists()) {
                HttpHeaders headers = new HttpHeaders();
                headers.add("content-disposition", "inline;filename=" + fileName);

                return ResponseEntity.ok()
                        .contentLength(paths.toFile().length())
                        .contentType(MediaType.parseMediaType(contentType))
                        .headers(headers)
                        .body(resource);
            } else {
                throw new RuntimeException("File not found ");
            }
        } catch (MalformedURLException ex) {
            throw new RuntimeException("File not found ");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
