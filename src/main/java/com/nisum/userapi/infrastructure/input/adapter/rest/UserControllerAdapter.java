package com.nisum.userapi.infrastructure.input.adapter.rest;

import com.nisum.userapi.api.UsersApi;
import com.nisum.userapi.application.port.in.*;
import com.nisum.userapi.dto.UserRequest;
import com.nisum.userapi.dto.UserResponse;
import com.nisum.userapi.exception.UserApiException;
import com.nisum.userapi.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class UserControllerAdapter implements UsersApi {
    private final UserApplicationPort userApplicationService;
    private final UserMapper mapper;

    @Override
    public Mono<ResponseEntity<UserResponse>> createUser(Mono<UserRequest> userRequest, ServerWebExchange exchange) {
        return userRequest
                .map(mapper::toEntity)
                .flatMap(userApplicationService::create)
                .map(mapper::toResponse)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response));
    }

    @Override
    public Mono<ResponseEntity<Flux<UserResponse>>> listUsers(ServerWebExchange exchange) {
        return Mono.just(
                ResponseEntity.ok(userApplicationService.list().map(mapper::toResponse))
        );
    }

    @Override
    public Mono<ResponseEntity<UserResponse>> patchUser(UUID id, Mono<UserRequest> userRequest, ServerWebExchange exchange) {
        return userRequest
                .map(mapper::toEntity)
                .flatMap(patch -> userApplicationService.patch(id, patch))
                .map(mapper::toResponse)
                .map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<UserResponse>> updateUser(UUID id, Mono<UserRequest> userRequest, ServerWebExchange exchange) {
        return userRequest
                .map(mapper::toEntity).
                flatMap(user -> userApplicationService.update(id, user))
                .map(mapper::toResponse)
                .map(ResponseEntity::ok);
    }


    @Override
    public Mono<ResponseEntity<Void>> deleteUser(UUID id, ServerWebExchange exchange) {
        return userApplicationService.delete(id).thenReturn(ResponseEntity.noContent().build());
    }

    @Override
    public Mono<ResponseEntity<UserResponse>> getUserById(UUID id, ServerWebExchange exchange) {
        return userApplicationService.get(id)
                .map(mapper::toResponse)
                .map(ResponseEntity::ok)
                .switchIfEmpty(Mono.error(new UserApiException("Usuario no encontrado", HttpStatus.NOT_FOUND)));
    }
}
