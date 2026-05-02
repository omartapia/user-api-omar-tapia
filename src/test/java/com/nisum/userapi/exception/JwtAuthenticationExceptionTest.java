package com.nisum.userapi.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

class JwtAuthenticationExceptionTest {

    @Test
    void gettersReturnValues() {
        Throwable cause = new IllegalArgumentException("cause");
        JwtAuthenticationException ex = new JwtAuthenticationException("msg", HttpStatus.UNAUTHORIZED, cause);

        assertThat(ex.getMessage()).isEqualTo("msg");
        assertThat(ex.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(ex.getCause()).isSameAs(cause);
    }
}
