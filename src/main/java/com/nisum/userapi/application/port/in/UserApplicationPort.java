package com.nisum.userapi.application.port.in;

import com.nisum.userapi.domain.User;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface UserApplicationPort {

    Mono<User> create(User user);
    Mono<Void> delete(UUID id);
    Mono<User> get(UUID uuid);
    Flux<User> list(int page, int size);
    Mono<User> patch(UUID id, User patch);
    Mono<User> update(UUID id, User user);
}
