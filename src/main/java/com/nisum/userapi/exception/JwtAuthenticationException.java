package com.nisum.userapi.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
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

}
