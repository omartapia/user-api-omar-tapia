package com.nisum.userapi.filter;

import com.nisum.userapi.service.impl.JwtServiceImpl;
import com.nisum.userapi.utils.SecurityConstants;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import com.nisum.userapi.exception.JwtAuthenticationException;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtFilterTest {
    @Mock
    private JwtServiceImpl jwtServiceImpl;
    @Mock
    private WebFilterChain chain;

    @InjectMocks
    private JwtFilter filter;

    @Test
    void givenRequestWithoutAuthorizationHeaderWhenFilterThenContinuesChain() {
        // given
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.post("/users"));
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        // when
        Mono<Void> result = filter.filter(exchange, chain);

        // then
        StepVerifier.create(result).verifyComplete();
        verify(chain).filter(exchange);
        verifyNoInteractions(jwtServiceImpl);
    }

    @Test
    void givenRequestWithoutAuthorizationHeaderWhenFilterThenUnauthorized() {
        // given
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/users"));

        // when
        Mono<Void> result = filter.filter(exchange, chain);

        // then
        StepVerifier.create(result).verifyComplete();
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        verifyNoInteractions(jwtServiceImpl);
    }

    @Test
    void givenRequestWithValidBearerTokenWhenFilterThenContinuesChain() {
        // given
        String token = "valid-token";
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/users")
                        .header(SecurityConstants.AUTHORIZATION_HEADER, SecurityConstants.BEARER_PREFIX + token)
        );
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        // when
        Mono<Void> result = filter.filter(exchange, chain);

        // then
        StepVerifier.create(result).verifyComplete();
        verify(jwtServiceImpl).validate(token);
        verify(chain).filter(exchange);
    }

    @Test
    void givenRequestWithInvalidBearerTokenWhenFilterThenReturnsUnauthorized() {
        // given
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/users")
                        .header(SecurityConstants.AUTHORIZATION_HEADER, SecurityConstants.BEARER_PREFIX + "invalid-token")
        );
        org.mockito.Mockito.doThrow(new IllegalArgumentException("invalid token"))
                .when(jwtServiceImpl).validate("invalid-token");
        Mono<Void> result = filter.filter(exchange, chain);
        // when
        StepVerifier.create(result)
                .expectError(JwtAuthenticationException.class)
                .verify();
    }
}
