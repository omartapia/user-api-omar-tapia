package com.nisum.userapi.application.port.out;

import com.nisum.userapi.domain.User;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface UserPersistencePort {
    Mono<User> save(User user);
    Flux<User> findAll();
    Mono<User> findById(UUID id);
    Mono<User> findByEmail(String email);
}
