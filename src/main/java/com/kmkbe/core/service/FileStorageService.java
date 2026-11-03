package com.kmkbe.core.service;

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
import java.nio.charset.StandardCharsets;
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

            // Validasi isi file (magic bytes) - extension saja bisa dipalsukan
            validateFileContent(file, ext);

            // Ganti spasi dalam nama file dengan garis bawah
            name = name.replaceAll("\\s", "_");
            log.info("Processed file name: " + name);

            // Pastikan nama file memiliki ekstensi yang benar
            if (!name.endsWith("." + ext)) {
                name = name.substring(0, name.lastIndexOf(".")) + "." + ext;
            }

            Path uploadPath = resolveSafely(uploadDir);
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
          if (filename.startsWith("/uploads/")) {
            filename = filename.replace("/uploads/", "");
          }

          Path file = resolveSafely(filename);
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

            Path file = resolveSafely(filename);
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
            Path paths = resolveSafely(filePath);
            Resource resource = new UrlResource(paths.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                // Pesan sengaja tidak memuat path absolut agar tidak bocor via response
                throw new RuntimeException("File not found or not readable");
            }
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
     * Resolve path relatif di dalam storage root dan pastikan hasilnya
     * tetap berada di bawah root (security issue #11: cegah path traversal).
     * Absolute path atau path dengan "../" yang meloloskan diri keluar root
     * ditolak dengan pesan generik (tanpa membocorkan path).
     */
    private Path resolveSafely(String relativePath) {
        if (relativePath == null || relativePath.isBlank()) {
            throw new RuntimeException("Invalid file path");
        }

        Path rootAbs = root.toAbsolutePath().normalize();
        Path resolved = rootAbs.resolve(relativePath).normalize();

        if (!resolved.startsWith(rootAbs)) {
            log.warn("Rejected file path outside storage root");
            throw new RuntimeException("Invalid file path");
        }

        return resolved;
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

    /**
     * Validasi magic bytes dari isi file agar cocok dengan ekstensi yang diterima.
     * DOC lama (OLE2) dan PDF punya signature pasti; JPG/PNG juga.
     * DOCX (zip) tidak di-whitelist sehingga tidak perlu dicek di sini.
     */
    private void validateFileContent(MultipartFile file, String ext) throws Exception {
        byte[] header = new byte[8];
        try (InputStream is = file.getInputStream()) {
            int read = is.read(header);
            if (read < 4) {
                throw new Exception("File content is too small or empty to be validated. Uploaded File: " + file.getOriginalFilename());
            }
        }

        boolean valid;
        switch (ext) {
            case "pdf":
                valid = startsWith(header, "%PDF");
                break;
            case "jpg":
            case "jpeg":
                valid = startsWith(header, new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF});
                break;
            case "png":
                valid = startsWith(header, new byte[]{(byte) 0x89, 'P', 'N', 'G'});
                break;
            case "doc":
                // DOC lama (OLE2 Compound File) dimulai dengan signature D0 CF 11 E0
                valid = startsWith(header, new byte[]{(byte) 0xD0, (byte) 0xCF, 0x11, (byte) 0xE0});
                break;
            default:
                valid = false;
        }

        if (!valid) {
            throw new Exception("File content does not match the declared extension: " + ext + ". Uploaded File: " + file.getOriginalFilename());
        }
    }

    private boolean startsWith(byte[] data, String prefix) {
        return startsWith(data, prefix.getBytes(StandardCharsets.US_ASCII));
    }

    private boolean startsWith(byte[] data, byte[] prefix) {
        if (data.length < prefix.length) {
            return false;
        }
        for (int i = 0; i < prefix.length; i++) {
            if (data[i] != prefix[i]) {
                return false;
            }
        }
        return true;
    }
}
