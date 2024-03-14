package com.mnq.logsfinder.service;

import com.mnq.logsfinder.service.impl.LocalStorageService;
import com.mnq.logsfinder.service.impl.S3StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StorageServiceFactory {

    private final S3StorageService s3StorageService;
    private final LocalStorageService localStorageService;

    @Autowired
    public StorageServiceFactory(S3StorageService s3StorageService, LocalStorageService localStorageService) {
        this.s3StorageService = s3StorageService;
        this.localStorageService = localStorageService;
    }

    public StorageService getService(String type) {
        return switch (type.toLowerCase()) {
            case "s3" -> s3StorageService;
            case "local" -> localStorageService;
            default -> throw new IllegalArgumentException("Invalid storage type");
        };
    }
}

