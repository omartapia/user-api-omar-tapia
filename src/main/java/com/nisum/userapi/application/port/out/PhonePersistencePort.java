package com.nisum.userapi.application.port.out;

import com.nisum.userapi.domain.Phone;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface PhonePersistencePort {
    Mono<Phone> save(Phone phone);
    Flux<Phone> findByUserId(UUID userId);
    Flux<Phone> findAllByUserId(UUID userId);
    Mono<Void> deleteByUserId(UUID userId);
}
