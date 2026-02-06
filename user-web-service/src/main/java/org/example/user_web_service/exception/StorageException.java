package org.example.user_web_service.exception;

import java.io.IOException;

public class StorageException extends Throwable {
    public StorageException(String failedToStoreUserPhoto, IOException e) {
    }
}