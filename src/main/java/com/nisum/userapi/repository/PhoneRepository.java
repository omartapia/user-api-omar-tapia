package com.nisum.userapi.repository;


import com.nisum.userapi.model.Phone;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface PhoneRepository
        extends ReactiveCrudRepository<Phone, UUID> {

    Flux<Phone> findAllByUserId(UUID userId);
    Mono<Void> deleteByUserId(UUID userId);
    Flux<Phone> findByUserId(UUID userId);
}
