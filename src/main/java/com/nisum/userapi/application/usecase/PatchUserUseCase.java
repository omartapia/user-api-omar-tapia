package com.nisum.userapi.application.usecase;

import com.nisum.userapi.model.User;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface PatchUserUseCase {
    Mono<User> patch(UUID id, User patch);
}
