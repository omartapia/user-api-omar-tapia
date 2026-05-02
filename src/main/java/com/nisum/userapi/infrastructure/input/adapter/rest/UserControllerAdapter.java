package com.nisum.userapi.infrastructure.input.adapter.rest;

import com.nisum.userapi.api.UsersApi;
import com.nisum.userapi.dto.UserRequest;
import com.nisum.userapi.dto.UserResponse;
import com.nisum.userapi.mapper.UserMapper;
import com.nisum.userapi.application.port.in.CreateUserUseCase;
import com.nisum.userapi.application.port.in.ListUsersUseCase;
import com.nisum.userapi.application.port.in.GetUserUseCase;
import com.nisum.userapi.application.port.in.DeleteUserUseCase;
import com.nisum.userapi.application.port.in.UpdateUserUseCase;
import com.nisum.userapi.application.port.in.PatchUserUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class UserControllerAdapter implements UsersApi {
    private final CreateUserUseCase createUserUseCase;
    private final ListUsersUseCase listUsersUseCase;
    private final GetUserUseCase getUserUseCase;
    private final DeleteUserUseCase deleteUserUseCase;
    private final UpdateUserUseCase updateUserUseCase;
    private final PatchUserUseCase patchUserUseCase;
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
                .map(mapper::toEntity).
                flatMap(user -> updateUserUseCase.update(id, user))
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
