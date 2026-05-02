package com.nisum.userapi.exception;

import com.nisum.userapi.dto.ErrorResponse;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleUserApiExceptionReturnsStatusAndMessage() {
        UserApiException ex = new UserApiException("user not found", HttpStatus.NOT_FOUND);
        ResponseEntity<ErrorResponse> resp = handler.handleUserApiException(ex);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(resp.getBody()).isNotNull();
        assertThat(resp.getBody().getMensaje()).isEqualTo("user not found");
    }

    @Test
    void handleJwtApiExceptionReturnsStatusAndMessage() {
        JwtAuthenticationException ex = new JwtAuthenticationException("jwt error", HttpStatus.UNAUTHORIZED, new IllegalArgumentException("cause"));
        ResponseEntity<ErrorResponse> resp = handler.handleJwtApiException(ex);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(resp.getBody()).isNotNull();
        assertThat(resp.getBody().getMensaje()).isEqualTo("jwt error");
    }

    @Test
    void handleDuplicateKeyReturnsConflictAndMessage() {
        DuplicateKeyException ex = new DuplicateKeyException("dup");
        ResponseEntity<ErrorResponse> resp = handler.handleDuplicateKey(ex);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(resp.getBody()).isNotNull();
        assertThat(resp.getBody().getMensaje()).isEqualTo("El correo ya está registrado");
    }

    @Test
    void handleUnexpectedReturnsInternalServerError() {
        RuntimeException ex = new RuntimeException("boom");
        ResponseEntity<ErrorResponse> resp = handler.handleUnexpected(ex);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(resp.getBody()).isNotNull();
        assertThat(resp.getBody().getMensaje()).isEqualTo("Error interno del servidor");
    }
}
