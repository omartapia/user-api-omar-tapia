package com.nisum.userapi.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

class UserApiExceptionTest {

    @Test
    void gettersReturnValues() {
        UserApiException ex = new UserApiException("msg", HttpStatus.BAD_REQUEST);

        assertThat(ex.getMessage()).isEqualTo("msg");
        assertThat(ex.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
