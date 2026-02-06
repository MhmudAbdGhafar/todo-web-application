package org.example.user_web_service.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Set;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "storage")
public class StorageProperties {

    /**
     * Root directory for all uploads
     */
    private String rootDir;

    /**
     * Max allowed file size (bytes)
     */
    private long maxFileSize;

    /**
     * Allowed MIME types
     */
    private Set<String> allowedContentTypes;
}