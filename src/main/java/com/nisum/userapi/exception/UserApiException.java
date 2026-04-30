package com.nisum.userapi.exception;

import org.springframework.http.HttpStatus;

public class UserApiException extends RuntimeException {

        private final HttpStatus status;

    public UserApiException(
                String message,
                HttpStatus status
        ) {
            super(message);
            this.status = status;
        }

        public HttpStatus getStatus() {
            return status;
        }
    }
