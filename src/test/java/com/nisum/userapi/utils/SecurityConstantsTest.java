package com.nisum.userapi.utils;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityConstantsTest {
    @Test
    void givenSecurityConstantsWhenReadThenReturnExpectedValues() {
        // given
        String expectedAuthorizationHeader = "Authorization";
        String expectedBearerPrefix = "Bearer ";

        // when
        String authorizationHeader = SecurityConstants.AUTHORIZATION_HEADER;
        String bearerPrefix = SecurityConstants.BEARER_PREFIX;

        // then
        assertThat(authorizationHeader).isEqualTo(expectedAuthorizationHeader);
        assertThat(bearerPrefix).isEqualTo(expectedBearerPrefix);
    }
}
