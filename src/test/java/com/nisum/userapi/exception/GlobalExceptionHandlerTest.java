package com.nisum.userapi.exception;

import com.nisum.userapi.dto.ErrorResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void givenGenericExceptionWhenHandleGenericThenReturn500AndErrorResponse() {
        // GIVEN
        Exception exception = new Exception("error de prueba");

        // WHEN
        ResponseEntity<ErrorResponse> response =
                handler.handleGeneric(exception);

        // THEN
        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);

        assertThat(response.getBody())
                .isNotNull();

        assertThat(response.getBody().getMensaje())
                .isEqualTo("Error interno del servidor");
    }

    @Test
    void givenValidationExceptionWhenHandleValidationThenReturn400AndErrorResponse() {
        // GIVEN
        BeanPropertyBindingResult bindingResult =
                new BeanPropertyBindingResult(new Object(), "userRequest");

        bindingResult.addError(
                new FieldError(
                        "userRequest",
                        "email",
                        "Formato de correo inválido"
                )
        );

        MethodArgumentNotValidException exception =
                new MethodArgumentNotValidException(null, bindingResult);

        // WHEN
        ResponseEntity<ErrorResponse> response =
                handler.handleValidation(exception);

        // THEN
        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);

        assertThat(response.getBody())
                .isNotNull();

        assertThat(response.getBody().getMensaje())
                .isEqualTo("Formato de correo inválido");
    }

    @Test
    void givenAccessDeniedExceptionWhenHandleForbiddenThenReturn403AndErrorResponse() {
        // GIVEN
        AccessDeniedException exception =
                new AccessDeniedException("Acceso denegado");

        // WHEN
        ResponseEntity<ErrorResponse> response =
                handler.handleForbidden(exception);

        // THEN
        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.FORBIDDEN);

        assertThat(response.getBody())
                .isNotNull();

        assertThat(response.getBody().getMensaje())
                .isEqualTo("Acceso denegado");
    }

    @Test
    void givenJwtAuthenticationExceptionWhenHandleJwtThenReturnConfiguredStatusAndErrorResponse() {
        // GIVEN
        JwtAuthenticationException exception =
                new JwtAuthenticationException(
                        "Token expirado",
                        HttpStatus.UNAUTHORIZED,
                        null
                );

        // WHEN
        ResponseEntity<ErrorResponse> response =
                handler.handleJwt(exception);

        // THEN
        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.UNAUTHORIZED);

        assertThat(response.getBody())
                .isNotNull();

        assertThat(response.getBody().getMensaje())
                .isEqualTo("Token expirado");
    }
}