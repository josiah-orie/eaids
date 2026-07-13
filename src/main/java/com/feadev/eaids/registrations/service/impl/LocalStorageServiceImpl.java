package com.feadev.eaids.registrations.service.impl;

import com.feadev.eaids.registrations.service.StorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@Slf4j
public class LocalStorageServiceImpl implements StorageService {

    private final Path basePath;

    public LocalStorageServiceImpl(@Value("${storage.local.base-path:./storage}") String basePath) {
        this.basePath = Paths.get(basePath).toAbsolutePath().normalize();
    }

    @Override
    public String store(MultipartFile file, String folder) {
        try {
            Path folderPath = basePath.resolve(folder).normalize();
            Files.createDirectories(folderPath);

            String originalFilename = file.getOriginalFilename();
            String extension = extractExtension(originalFilename);
            String uniqueFilename = UUID.randomUUID() + extension;

            Path targetPath = folderPath.resolve(uniqueFilename);

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
            }

            String relativePath = basePath.relativize(targetPath).toString();
            log.debug("File stored at: {}", relativePath);
            return relativePath;
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(String filePath) {
        try {
            Path path = basePath.resolve(filePath).normalize();
            Files.deleteIfExists(path);
            log.debug("File deleted: {}", filePath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete file: " + filePath, e);
        }
    }

    @Override
    public boolean exists(String filePath) {
        Path path = basePath.resolve(filePath).normalize();
        return Files.exists(path);
    }

    private String extractExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf("."));
    }
}
