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

        String[] splitter = file.getOriginalFilename().split("\\.");
        if (splitter.length >= 1) {
            return splitter[1];
        }
        return null;
    }

    public static String getFileNameExtension(String fileName) throws Exception {
        if (StringUtil.isNullOrEmpty(fileName.trim())) {
            throw new Exception("File name is null");
        }

        String[] splitter = fileName.split("\\.");
        if (splitter.length >= 1) {
            return splitter[1];
        }

        return null;
    }

    public static String getFilePathFromFullPath(String path) {
        if (StringUtil.isNullOrEmpty(path)) {
            return null;
        }

        return path.substring(0, path.lastIndexOf("/"));
    }
}
