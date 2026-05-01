package com.nisum.userapi.application.service;

import com.nisum.userapi.application.usecase.CreateUserUseCase;
import com.nisum.userapi.application.usecase.ListUsersUseCase;
import com.nisum.userapi.application.usecase.GetUserUseCase;
import com.nisum.userapi.application.usecase.DeleteUserUseCase;
import com.nisum.userapi.application.usecase.UpdateUserUseCase;
import com.nisum.userapi.application.usecase.PatchUserUseCase;
import com.nisum.userapi.model.User;
import com.nisum.userapi.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserApplicationService implements CreateUserUseCase, ListUsersUseCase, GetUserUseCase, DeleteUserUseCase, UpdateUserUseCase, PatchUserUseCase {
    private final UserService userService;

    @Override
    public Mono<User> create(User user) {
        return userService.create(user);
    }

    @Override
    public Flux<User> list() {
        return userService.list();
    }

    @Override
    public Mono<User> get(UUID uuid) {
        return userService.get(uuid);
    }

    @Override
    public Mono<Void> delete(UUID id) {
        return userService.delete(id);
    }

    @Override
    public Mono<User> update(UUID id, User user) {
        return userService.update(id, user);
    }

    @Override
    public Mono<User> patch(UUID id, User patch) {
        return userService.patch(id, patch);
    }
}
