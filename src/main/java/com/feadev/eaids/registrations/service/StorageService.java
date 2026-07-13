package com.feadev.eaids.registrations.service;

import org.springframework.web.multipart.MultipartFile;

public interface StorageService {

    String store(MultipartFile file, String folder);

    void delete(String filePath);

    boolean exists(String filePath);
}
