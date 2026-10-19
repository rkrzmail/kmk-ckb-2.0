package com.kmkbe.core.utils;

import io.netty.util.internal.StringUtil;
import org.springframework.web.multipart.MultipartFile;

public class FileUtils {
    public static String getUploadFileExtension(MultipartFile file) throws Exception {
        if (file == null) {
            throw new Exception("File cannot be null");
        }

        if (file.getOriginalFilename() == null) {
            throw new Exception("File cannot be null");
        }

        return extractLastExtension(file.getOriginalFilename());
    }

    public static String getFileNameExtension(String fileName) throws Exception {
        if (StringUtil.isNullOrEmpty(fileName)) {
            return null;
        }

        return extractLastExtension(fileName.trim());
    }

    private static String extractLastExtension(String filename) {
        if (filename == null) {
            return null;
        }
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex != -1 && lastDotIndex < filename.length() - 1) {
            return filename.substring(lastDotIndex + 1).toLowerCase();
        }
        return null;
    }

    public static String getFilePathFromFullPath(String path) {
        if (StringUtil.isNullOrEmpty(path)) {
            return null;
        }

        int lastSlashIndex = path.lastIndexOf("/");
        if (lastSlashIndex == -1) {
            return path;
        }
        return path.substring(0, lastSlashIndex);
    }
}
