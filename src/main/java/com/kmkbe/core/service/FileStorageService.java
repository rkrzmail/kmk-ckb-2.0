package com.kmkbe.core.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
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
    private final Path root = Paths.get("uploads");

    public FileStorageService() {
        init();
    }

    private void init() {
        try {
            Files.createDirectories(root);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize folder for upload!");
        }
    }

    public String save(MultipartFile file, String code) throws Exception {
        try {
            if (file == null) {
                throw new Exception("File cannot be null");
            }

            if (file.getOriginalFilename() == null) {
                throw new Exception("File cannot be null");
            }

            Path uniquePath = Paths.get(code, file.getOriginalFilename());
            Path fileDestination = root.resolve(
                            Paths.get(file.getOriginalFilename())
                    )
                    .normalize()
                    .toAbsolutePath();

            if (!fileDestination.getParent().equals(root.toAbsolutePath())) {
                throw new RuntimeException("Cannot store file outside current directory.");
            }

           /* File transferFile = new File(code + "/" + file.getOriginalFilename());
            if (!transferFile.exists()) {
                boolean make = transferFile.mkdirs();
                if (make) {
                    file.transferTo(transferFile);
                }
            }*/

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, fileDestination, StandardCopyOption.REPLACE_EXISTING);
                return fileDestination.toFile().getAbsolutePath();
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
            Path file = root.resolve(code).resolve(filename);
            return Files.deleteIfExists(file);
        } catch (IOException e) {
            throw new RuntimeException("Error: " + e.getMessage());
        }
    }
}
