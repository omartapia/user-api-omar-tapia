package com.nisum.userapi.application.port.in;

import reactor.core.publisher.Mono;

import java.util.UUID;

public interface DeleteUserUseCase {
    Mono<Void> delete(UUID id);
}
