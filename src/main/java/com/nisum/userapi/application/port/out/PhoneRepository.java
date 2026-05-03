package com.nisum.userapi.application.port.out;

import com.nisum.userapi.domain.Phone;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.util.UUID;

@Repository
public interface PhoneRepository extends ReactiveCrudRepository<Phone, UUID> {

    Flux<Phone> findByUserId(UUID userId);
}
