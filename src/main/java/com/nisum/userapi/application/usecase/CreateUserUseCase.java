package com.nisum.userapi.application.usecase;

import com.nisum.userapi.model.User;
import reactor.core.publisher.Mono;

public interface CreateUserUseCase {
    Mono<User> create(User user);
}
