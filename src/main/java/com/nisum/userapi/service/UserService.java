package com.nisum.userapi.service;

import com.nisum.userapi.model.Phone;
import com.nisum.userapi.model.User;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

public interface UserService {
    Mono<User> create(User user);
    Flux<User> list();
    Mono<User> get(UUID uuid);
    Mono<Void> delete(UUID id);
    Mono<User> update(UUID id, User user);
    Mono<User> patch(UUID id, User patch);
    Mono<Void> replacePhones(UUID userId, List<Phone> phones);
    Flux<Phone> persistPhones(UUID userId, List<Phone> phones);
}
