package com.sendByOP.expedition.utils;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Pattern;

public class SecurityUtils {

    private static final Pattern SAFE_FILENAME_PATTERN = Pattern.compile("[a-zA-Z0-9_\\-\\.]+");
    
    /**
     * Validates if a filename is safe (no path traversal)
     * @param filename the filename to validate
     * @return true if the filename is safe, false otherwise
     */
    public static boolean isValidFilename(String filename) {
        return filename != null && SAFE_FILENAME_PATTERN.matcher(filename).matches();
    }
    
    /**
     * Checks if a path is within a base directory (prevents path traversal)
     * @param basePath the base directory
     * @param filePath the file path to check
     * @return true if the path is safe, false otherwise
     */
    public static boolean isPathWithinBaseDir(String basePath, String filePath) {
        try {
            Path base = Paths.get(basePath).toAbsolutePath().normalize();
            Path file = Paths.get(basePath, filePath).toAbsolutePath().normalize();
            return file.startsWith(base);
        } catch (Exception e) {
            return false;
        }
    }
}