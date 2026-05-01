package com.nisum.userapi.exception;

import com.nisum.userapi.dto.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(UserApiException.class)
    public ResponseEntity<ErrorResponse> handleUserApiException(
            UserApiException ex) {
        log.error("Error handleUserApiException: ", ex);
        ErrorResponse error = new ErrorResponse();
        error.setMensaje(ex.getMessage());

        return ResponseEntity
                .status(ex.getStatus())
                .body(error);
    }

    @ExceptionHandler(JwtAuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleJwtApiException(
            JwtAuthenticationException ex) {
        log.error("Error handleJwtApiException: ", ex);
        ErrorResponse error = new ErrorResponse();
        error.setMensaje(ex.getMessage());

        return ResponseEntity
                .status(ex.getStatus())
                .body(error);
    }


    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateKey(
            DuplicateKeyException ex) {
        log.error("Error handleDuplicateKey: ", ex);
        ErrorResponse error = new ErrorResponse();
        error.setMensaje("El correo ya está registrado");

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(error);
    }

    @ExceptionHandler(Throwable.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(
            Throwable ex) {
        log.error("Error handleUnexpected: ", ex);
        ErrorResponse error = new ErrorResponse();
        error.setMensaje("Error interno del servidor");

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(error);
    }
}