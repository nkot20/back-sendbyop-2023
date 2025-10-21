package com.sendByOP.expedition.services;

import com.sendByOP.expedition.config.FileStorageConfig;
import com.sendByOP.expedition.exception.SendByOpException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileStorageService {

    private final FileStorageConfig fileStorageConfig;

    /**
     * Stores a profile picture with security validations
     */
    public String storeProfilePicture(MultipartFile file, Integer customerId) throws SendByOpException {
        validateFile(file);
        
        try {
            // Create upload directory if it doesn't exist
            Path uploadPath = createUploadDirectory();
            
            // Generate unique filename
            String filename = generateUniqueFilename(file, customerId);
            
            // Store the file
            Path targetLocation = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            
            log.info("Profile picture stored successfully for customer {} at: {}", customerId, filename);
            return filename;
            
        } catch (IOException ex) {
            log.error("Failed to store profile picture for customer {}: {}", customerId, ex.getMessage());
            throw new SendByOpException("Could not store file. Please try again!", org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Deletes a profile picture file
     */
    public void deleteProfilePicture(String filename) {
        if (filename == null || filename.trim().isEmpty()) {
            return;
        }
        
        try {
            Path filePath = Paths.get(fileStorageConfig.getProfilePicturesPath()).resolve(filename);
            Files.deleteIfExists(filePath);
            log.info("Profile picture deleted: {}", filename);
        } catch (IOException ex) {
            log.error("Failed to delete profile picture {}: {}", filename, ex.getMessage());
        }
    }

    /**
     * Validates uploaded file for security
     */
    private void validateFile(MultipartFile file) throws SendByOpException {
        if (file.isEmpty()) {
            throw new SendByOpException("Please select a file to upload", org.springframework.http.HttpStatus.BAD_REQUEST);
        }

        // Check file size
        if (file.getSize() > fileStorageConfig.getMaxFileSize()) {
            throw new SendByOpException("File size exceeds maximum allowed size of " + 
                (fileStorageConfig.getMaxFileSize() / 1024 / 1024) + "MB", org.springframework.http.HttpStatus.BAD_REQUEST);
        }

        // Check content type
        String contentType = file.getContentType();
        if (contentType == null || !Arrays.asList(fileStorageConfig.getAllowedImageTypes()).contains(contentType)) {
            throw new SendByOpException("Invalid file type. Only JPEG, PNG and WebP images are allowed", org.springframework.http.HttpStatus.BAD_REQUEST);
        }

        // Check file extension
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        if (originalFilename.contains("..")) {
            throw new SendByOpException("Invalid file path", org.springframework.http.HttpStatus.BAD_REQUEST);
        }

        String extension = getFileExtension(originalFilename).toLowerCase();
        if (!Arrays.asList(fileStorageConfig.getAllowedExtensions()).contains(extension)) {
            throw new SendByOpException("Invalid file extension. Only .jpg, .jpeg, .png and .webp are allowed", org.springframework.http.HttpStatus.BAD_REQUEST);
        }

        // Validate actual image content (prevents malicious files with fake extensions)
        try {
            BufferedImage image = ImageIO.read(file.getInputStream());
            if (image == null) {
                throw new SendByOpException("Invalid image file", org.springframework.http.HttpStatus.BAD_REQUEST);
            }
            
            // Additional security: check image dimensions (prevent extremely large images)
            if (image.getWidth() > 4000 || image.getHeight() > 4000) {
                throw new SendByOpException("Image dimensions too large. Maximum 4000x4000 pixels allowed", org.springframework.http.HttpStatus.BAD_REQUEST);
            }
            
        } catch (IOException ex) {
            throw new SendByOpException("Failed to validate image file", org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Creates upload directory if it doesn't exist
     */
    private Path createUploadDirectory() throws IOException {
        Path uploadPath = Paths.get(fileStorageConfig.getProfilePicturesPath()).toAbsolutePath().normalize();
        Files.createDirectories(uploadPath);
        return uploadPath;
    }

    /**
     * Generates unique filename with customer ID and timestamp
     */
    private String generateUniqueFilename(MultipartFile file, Integer customerId) {
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        String extension = getFileExtension(originalFilename);
        
        return String.format("customer_%d_%s%s", 
            customerId, 
            UUID.randomUUID().toString().replace("-", ""), 
            extension);
    }

    /**
     * Extracts file extension from filename
     */
    private String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf(".") == -1) {
            return "";
        }
        return filename.substring(filename.lastIndexOf("."));
    }

    /**
     * Gets the full path to a profile picture
     */
    public Path getProfilePicturePath(String filename) {
        return Paths.get(fileStorageConfig.getProfilePicturesPath()).resolve(filename);
    }
}
