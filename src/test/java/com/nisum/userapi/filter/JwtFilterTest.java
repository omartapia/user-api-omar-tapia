package com.nisum.userapi.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nisum.userapi.application.port.in.JwtPort;
import com.nisum.userapi.utils.SecurityConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtFilterTest {
    @Mock
    private JwtPort jwtPort;
    @Mock
    private WebFilterChain chain;

    private JwtFilter filter;

    @BeforeEach
    void setUp() {
        filter = new JwtFilter(jwtPort, new ObjectMapper());
    }

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
        verifyNoInteractions(jwtPort);
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
        assertThat(exchange.getResponse().getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
        assertThat(exchange.getResponse().getBodyAsString().block()).isEqualTo("{\"mensaje\":\"Token requerido\"}");
        verifyNoInteractions(jwtPort);
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
        verify(jwtPort).validate(token);
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
                .when(jwtPort).validate("invalid-token");
        Mono<Void> result = filter.filter(exchange, chain);

        // when
        StepVerifier.create(result).verifyComplete();
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(exchange.getResponse().getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
        assertThat(exchange.getResponse().getBodyAsString().block()).isEqualTo("{\"mensaje\":\"invalid token\"}");
        verify(jwtPort).validate("invalid-token");
        verifyNoInteractions(chain);
    }
}
