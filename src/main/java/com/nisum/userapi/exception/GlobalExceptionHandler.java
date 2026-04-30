package com.nisum.userapi.exception;

import com.nisum.userapi.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 400 - Validaciones
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException ex
    ) {
        String message = ex.getBindingResult()
                .getFieldErrors()
                .get(0)
                .getDefaultMessage();

        ErrorResponse error = new ErrorResponse();
        error.setMensaje(message);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(error);
    }

    // 403 - Acceso denegado
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleForbidden(
            AccessDeniedException ex
    ) {
        ErrorResponse error = new ErrorResponse();
        error.setMensaje("Acceso denegado");

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(error);
    }

    // JWT - 401 / 500
    @ExceptionHandler(JwtAuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleJwt(
            JwtAuthenticationException ex
    ) {
        ErrorResponse error = new ErrorResponse();
        error.setMensaje(ex.getMessage());

        return ResponseEntity
                .status(ex.getStatus())
                .body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(
            Exception ex
    ) {
        ErrorResponse error = new ErrorResponse();
        error.setMensaje("Error interno del servidor");

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUserApiGeneric(
            UserApiException ex
    ) {
        ErrorResponse error = new ErrorResponse();
        error.setMensaje(ex.getMessage());

        return ResponseEntity
                .status(ex.getStatus())
                .body(error);
    }
}