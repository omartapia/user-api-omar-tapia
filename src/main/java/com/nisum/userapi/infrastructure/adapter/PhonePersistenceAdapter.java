package com.nisum.userapi.infrastructure.adapter;

import com.nisum.userapi.application.port.out.PhonePersistencePort;
import com.nisum.userapi.model.Phone;
import com.nisum.userapi.infrastructure.repository.PhoneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PhonePersistenceAdapter implements PhonePersistencePort {
    private final PhoneRepository phoneRepository;

    @Override
    public Mono<Phone> save(Phone phone) {
        return phoneRepository.save(phone);
    }

    @Override
    public Flux<Phone> findByUserId(UUID userId) {
        return phoneRepository.findByUserId(userId);
    }

    @Override
    public Flux<Phone> findAllByUserId(UUID userId) {
        return phoneRepository.findAllByUserId(userId);
    }

    @Override
    public Mono<Void> deleteByUserId(UUID userId) {
        return phoneRepository.deleteByUserId(userId);
    }
}
