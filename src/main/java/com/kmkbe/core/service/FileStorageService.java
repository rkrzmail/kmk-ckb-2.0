package com.kmkbe.core.service;

import com.kmkbe.core.utils.FileUtils;
import com.kmkbe.modules.user.utils.Utils;
import io.netty.util.internal.StringUtil;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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
public class FileStorageService {
    private final ServletContext context;
    private final Path root;

    public FileStorageService(
            ServletContext context,
            @Value("${kmk.file-storage.root:uploads}") String root
    ) {
        this.context = context;
        this.root = Paths.get(root);
    }

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

            // Ambil ekstensi file dengan benar dari titik terakhir
            String ext = getFileExtension(file.getOriginalFilename());
            log.info("Detected file extension: " + ext);

            if (StringUtil.isNullOrEmpty(ext)) {
                throw new Exception("File format or extension is not valid, try to upload valid file. Uploaded File: " + name);
            }

            // Validasi ekstensi
            if (!(ext.equalsIgnoreCase("doc") ||
                    ext.equalsIgnoreCase("pdf") ||
                    ext.equalsIgnoreCase("jpg") ||
                    ext.equalsIgnoreCase("jpeg") ||
                    ext.equalsIgnoreCase("png"))) {
                throw new Exception("File format or extension is not valid. Detected extension: " + ext + ". Allowed extensions: doc, pdf, jpg, jpeg, png.");
            }

            // Ganti spasi dalam nama file dengan garis bawah
            name = name.replaceAll("\\s", "_");
            log.info("Processed file name: " + name);

            // Pastikan nama file memiliki ekstensi yang benar
            if (!name.endsWith("." + ext)) {
                name = name.substring(0, name.lastIndexOf(".")) + "." + ext;
            }

            Path uploadPath = root.resolve(uploadDir);
            if (!uploadPath.toFile().exists()) {
                Files.createDirectories(uploadPath);
            }
            name = Utils.UUID() + "." + ext;//Paths.get(name)

            Path fileDestination = uploadPath.resolve(name)
                    .normalize()
                    .toAbsolutePath();

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, fileDestination, StandardCopyOption.REPLACE_EXISTING);

                return  uploadDir + "/" + name;
            } catch (Exception e) {
                throw new RuntimeException("Could not store the file. Error: " + e.getMessage());
            }

        } catch (Exception e) {
            log.error("Error while saving file: " + e.getMessage(), e);
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
            String fileName,
            String cd
    ) {
        try {
            // Tentukan path lengkap file
            //Path paths = root.resolve(filePath).resolve(fileName).normalize().toAbsolutePath();


            Path paths = root.resolve(filePath).normalize()  .toAbsolutePath();


            // Validasi apakah file ada dan dapat dibaca
            Resource resource = new UrlResource(paths.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new RuntimeException("File not found or not readable: " + paths.toUri());
            }

            // Tentukan content type
            String contentType = httpServletRequest
                    .getServletContext()
                    .getMimeType(fileName);

            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            // Header untuk file response
            HttpHeaders headers = new HttpHeaders();
            if (String.valueOf(httpServletRequest.getParameter("cd")).equalsIgnoreCase("attachment")){
                headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"");
            }else{
                headers.set("Content-Disposition","inline");
            }


            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .headers(headers)
                    .body(resource);
        } catch (Exception e) {
            throw new RuntimeException("Error occurred while downloading file: " + e.getMessage(), e);
        }
    }

    /**
     * Utility method to get the file extension from a filename.
     */
    private String getFileExtension(String filename) {
        if (filename != null && filename.contains(".")) {
            return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
        }
        return null;
    }
}
