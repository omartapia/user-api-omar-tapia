package com.nisum.userapi.infrastructure.output.adapter.repository;

import com.nisum.userapi.domain.Phone;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface PhoneRepository extends ReactiveCrudRepository<Phone, UUID> {

    Flux<Phone> findAllByUserId(UUID userId);
    Mono<Void> deleteByUserId(UUID userId);
    Flux<Phone> findByUserId(UUID userId);
}
