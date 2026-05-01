package com.nisum.userapi.application.usecase;

import reactor.core.publisher.Mono;

import java.util.UUID;

public interface DeleteUserUseCase {
    Mono<Void> delete(UUID id);
}
