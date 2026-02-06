package org.example.user_web_service.services;

import lombok.RequiredArgsConstructor;
import org.example.user_web_service.config.StorageProperties;
import org.example.user_web_service.entities.User;
import org.example.user_web_service.exception.ApiException;
import org.example.user_web_service.exception.StorageException;
import org.example.user_web_service.repositories.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService implements UserDetailsService {

    private final UserRepository userRepo;
    private final StorageService storageService;
    private final StorageProperties storageProperties;

    @Override
    public UserDetails loadUserByUsername(String username) {

        return userRepo.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException
                        ("User not found")
                );
    }

    public void uploadPhoto(String email, MultipartFile file)
            throws StorageException {

        validateFile(file);

        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException
                        ("User not found")
                );

        storageService.delete(user.getPhotoUrl());

        String path = storageService.storeUserPhoto(user.getId(), file);

        user.setPhotoUrl(path);
    }

    private void validateFile(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Photo file is required");
        }

        if (file.getSize() > storageProperties.getMaxFileSize()) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "Photo size exceeds allowed limit"
            );
        }

        if (!storageProperties.getAllowedContentTypes()
                .contains(file.getContentType())) {

            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "Unsupported file type"
            );
        }
    }
}