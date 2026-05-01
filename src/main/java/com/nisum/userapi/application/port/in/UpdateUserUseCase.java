package com.nisum.userapi.application.port.in;

import com.nisum.userapi.model.User;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface UpdateUserUseCase {
    Mono<User> update(UUID id, User user);
}
