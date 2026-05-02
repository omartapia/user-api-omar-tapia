package com.nisum.userapi.application.port.in;

import com.nisum.userapi.domain.User;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface GetUserUseCase {
    Mono<User> get(UUID uuid);
}
