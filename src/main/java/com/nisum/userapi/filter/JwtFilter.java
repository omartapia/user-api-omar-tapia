package com.nisum.userapi.filter;

import com.nisum.userapi.application.port.out.JwtPort;
import static  com.nisum.userapi.utils.SecurityConstants.BEARER_PREFIX;
import static  com.nisum.userapi.utils.SecurityConstants.AUTHORIZATION_HEADER;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import com.nisum.userapi.exception.JwtAuthenticationException;

import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtFilter implements WebFilter {

    private final JwtPort jwtPort;

    private static final Map<HttpMethod, Set<String>> PUBLIC_ENDPOINTS =
            Map.of(
                    HttpMethod.POST, Set.of("/users"),
                    HttpMethod.GET, Set.of(
                            "/swagger-ui",
                            "/v3/api-docs"
                    )
            );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {

        String path = exchange.getRequest().getURI().getPath();

        log.info("Ingresando al filter path: {}", path);
        HttpMethod method = exchange.getRequest().getMethod();

        if (isPublicEndpoint(method, path)) {
            return chain.filter(exchange);
        }

        String authorization = exchange.getRequest()
                .getHeaders()
                .getFirst(AUTHORIZATION_HEADER);

        if (authorization == null ||
                !authorization.startsWith(BEARER_PREFIX)) {
            log.error("Error, filter sin token");
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        try {
            jwtPort.validate(
                    authorization.substring(BEARER_PREFIX.length())
            );
        } catch (Exception e) {
            return Mono.error(new JwtAuthenticationException(e.getMessage(), HttpStatus.UNAUTHORIZED, e));
        }

        return chain.filter(exchange);
    }

    private boolean isPublicEndpoint(HttpMethod method, String path) {
        if (method == null) {
            return false;
        }

        return PUBLIC_ENDPOINTS.getOrDefault(method, Set.of())
                .stream()
                .anyMatch(path::startsWith);
    }
}