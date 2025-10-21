package com.sendByOP.expedition.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import lombok.Getter;
import lombok.Setter;

@Configuration
@ConfigurationProperties(prefix = "file.storage")
@Getter
@Setter
public class FileStorageConfig {
    
    private String uploadDir = "uploads";
    private String profilePicturesDir = "profile-pictures";
    private long maxFileSize = 5242880; // 5MB in bytes
    private String[] allowedImageTypes = {"image/jpeg", "image/jpg", "image/png", "image/webp"};
    private String[] allowedExtensions = {".jpg", ".jpeg", ".png", ".webp"};
    
    public String getProfilePicturesPath() {
        return uploadDir + "/" + profilePicturesDir;
    }
}
