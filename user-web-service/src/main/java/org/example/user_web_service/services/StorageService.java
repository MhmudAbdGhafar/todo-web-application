package org.example.user_web_service.services;

import lombok.RequiredArgsConstructor;
import org.example.user_web_service.config.StorageProperties;
import org.example.user_web_service.exception.StorageException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class StorageService {

    private final StorageProperties properties;

    public String storeUserPhoto(Long userId, MultipartFile file) throws StorageException {

        try {
            Path root = Paths.get(properties.getRootDir());
            Path userDir = root.resolve(String.valueOf(userId));

            Files.createDirectories(userDir);

            String extension = getExtension(Objects.requireNonNull(file.getOriginalFilename()));
            Path target = userDir.resolve("profile." + extension);

            Files.copy(
                    file.getInputStream(),
                    target,
                    StandardCopyOption.REPLACE_EXISTING
            );

            return target.toString();
        } catch (IOException e) {
            throw new StorageException("Failed to store user photo", e);
        }
    }

    public void delete(String path) {
        if (path == null || path.isBlank()) return;
        try {
            Files.deleteIfExists(Paths.get(path));
        } catch (IOException ignored) {
        }
    }

    private String getExtension(String filename) {
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }
}