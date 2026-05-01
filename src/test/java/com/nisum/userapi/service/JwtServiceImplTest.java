package com.nisum.userapi.service;

import com.nisum.userapi.config.JwtConfigProperties;
import com.nisum.userapi.service.impl.JwtServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtServiceImplTest {
    @InjectMocks
    private JwtServiceImpl jwtServiceImpl;
    @Mock
    private JwtConfigProperties jwtConfigProperties;

    @BeforeEach
    void setup() {
        when(jwtConfigProperties.getSecret()).thenReturn("bebf8157-2c41-43a5-9fc3-b2d7bb6dac7f");
    }

    @Test
    void givenSubjectWhenGenerateTokenThenReturnsValidToken() {
        // given
        String subject = "omar@example.com";
        when(jwtConfigProperties.getTtl()).thenReturn(3600000);
        // when
        String token = jwtServiceImpl.generate(subject);

        // then
        assertThat(token).isNotBlank();
        assertThatCode(() -> jwtServiceImpl.validate(token)).doesNotThrowAnyException();
    }

    @Test
    void givenInvalidTokenWhenValidateTokenThenThrowsException() {
        // given
        String token = "invalid-token";

        // when
        Throwable thrown = org.assertj.core.api.ThrowableAssert.catchThrowable(() -> jwtServiceImpl.validate(token));

        // then
        assertThatThrownBy(() -> {
            throw thrown;
        }).isInstanceOf(Exception.class);
    }
}
