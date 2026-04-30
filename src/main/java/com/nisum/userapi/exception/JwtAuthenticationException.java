package com.nisum.userapi.exception;

import org.springframework.http.HttpStatus;

public class JwtAuthenticationException extends RuntimeException {

        private final HttpStatus status;

    public JwtAuthenticationException(
                String message,
                HttpStatus status,
                Throwable cause
        ) {
            super(message, cause);
            this.status = status;
        }

        public HttpStatus getStatus() {
            return status;
        }
    }
