package com.nisum.userapi.application.port.in;

import com.nisum.userapi.domain.User;
import reactor.core.publisher.Mono;

public interface CreateUserUseCase {
    Mono<User> create(User user);
}
