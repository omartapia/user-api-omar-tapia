package com.nisum.userapi.controller;

import com.nisum.userapi.api.UsersApi;
import com.nisum.userapi.dto.UserPatchRequest;
import com.nisum.userapi.dto.UserRequest;
import com.nisum.userapi.dto.UserResponse;
import com.nisum.userapi.mapper.UserMapper;
import com.nisum.userapi.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class UserController implements UsersApi {
    private final UserService userService;
    private final UserMapper mapper;

    @Override
    public Mono<ResponseEntity<UserResponse>> createUser(Mono<UserRequest> userRequest, ServerWebExchange exchange) {
        return userRequest
                .map(mapper::toEntity)
                .flatMap(userService::create)
                .map(mapper::toResponse)
                .map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<Flux<UserResponse>>> listUsers(ServerWebExchange exchange) {
        return Mono.just(ResponseEntity.ok(userService.list().map(mapper::toResponse)));
    }

    @Override
    public Mono<ResponseEntity<UserResponse>> patchUser(UUID id, Mono<UserPatchRequest> userPatchRequest, ServerWebExchange exchange) {
        return userPatchRequest
                .flatMap(patch -> userService.patch(id, patch))
                .map(mapper::toResponse)
                .map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<UserResponse>> updateUser(UUID id, Mono<UserRequest> userRequest, ServerWebExchange exchange) {
        return userRequest
                .map(mapper::toEntity).
                flatMap(user -> userService.update(id, user))
                .map(mapper::toResponse)
                .map(ResponseEntity::ok);
    }


    @Override
    public Mono<ResponseEntity<Void>> deleteUser(UUID id, ServerWebExchange exchange) {
        return userService.delete(id).thenReturn(ResponseEntity.ok().build());
    }

    @Override
    public Mono<ResponseEntity<UserResponse>> getUserById(UUID id, ServerWebExchange exchange) {
        return userService.get(id)
                .map(mapper::toResponse)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
}
