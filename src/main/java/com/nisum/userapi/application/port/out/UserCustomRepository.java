package com.nisum.userapi.application.port.out;

import com.nisum.userapi.domain.User;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface UserCustomRepository {
    Flux<User> findUsersWithPhonesPaged(int page, int size);
    Mono<User> findByIdWithPhones(UUID id);
}
