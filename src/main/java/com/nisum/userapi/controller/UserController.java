package com.nisum.userapi.controller;

import com.nisum.userapi.api.UsersApi;
import com.nisum.userapi.dto.UserRequest;
import com.nisum.userapi.dto.UserResponse;
import com.nisum.userapi.mapper.UserMapper;

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
    private final com.nisum.userapi.application.usecase.CreateUserUseCase createUserUseCase;
    private final com.nisum.userapi.application.usecase.ListUsersUseCase listUsersUseCase;
    private final com.nisum.userapi.application.usecase.GetUserUseCase getUserUseCase;
    private final com.nisum.userapi.application.usecase.DeleteUserUseCase deleteUserUseCase;
    private final com.nisum.userapi.application.usecase.UpdateUserUseCase updateUserUseCase;
    private final com.nisum.userapi.application.usecase.PatchUserUseCase patchUserUseCase;
    private final UserMapper mapper;

    @Override
    public Mono<ResponseEntity<UserResponse>> createUser(Mono<UserRequest> userRequest, ServerWebExchange exchange) {
        return userRequest
                .map(mapper::toEntity)
                .flatMap(createUserUseCase::create)
                .map(mapper::toResponse)
                .map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<Flux<UserResponse>>> listUsers(ServerWebExchange exchange) {
        return Mono.just(
                ResponseEntity.ok(listUsersUseCase.list().map(mapper::toResponse))
        );
    }

    @Override
    public Mono<ResponseEntity<UserResponse>> patchUser(UUID id, Mono<UserRequest> userRequest, ServerWebExchange exchange) {
        return userRequest
                .map(mapper::toEntity)
                .flatMap(patch -> patchUserUseCase.patch(id, patch))
                .map(mapper::toResponse)
                .map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<UserResponse>> updateUser(UUID id, Mono<UserRequest> userRequest, ServerWebExchange exchange) {
        return userRequest
                .map(mapper::toEntity)
                .flatMap(user -> updateUserUseCase.update(id, user))
                .map(mapper::toResponse)
                .map(ResponseEntity::ok);
    }


    @Override
    public Mono<ResponseEntity<Void>> deleteUser(UUID id, ServerWebExchange exchange) {
        return deleteUserUseCase.delete(id).thenReturn(ResponseEntity.ok().build());
    }

    @Override
    public Mono<ResponseEntity<UserResponse>> getUserById(UUID id, ServerWebExchange exchange) {
        return getUserUseCase.get(id)
                .map(mapper::toResponse)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
}
