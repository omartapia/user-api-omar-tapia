package com.nisum.userapi.filter;

import com.nisum.userapi.service.JwtService;
import com.nisum.userapi.utils.SecurityConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class JwtFilter implements WebFilter {

    private final JwtService jwtService;

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
        HttpMethod method = exchange.getRequest().getMethod();

        if (isPublicEndpoint(method, path)) {
            return chain.filter(exchange);
        }

        String authorization = exchange.getRequest()
                .getHeaders()
                .getFirst(SecurityConstants.AUTHORIZATION_HEADER);

        if (authorization == null ||
                !authorization.startsWith(SecurityConstants.BEARER_PREFIX)) {

            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        try {
            jwtService.validate(
                    authorization.substring(SecurityConstants.BEARER_PREFIX.length())
            );
        } catch (Exception e) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
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