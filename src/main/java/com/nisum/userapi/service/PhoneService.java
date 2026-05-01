package com.nisum.userapi.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import com.nisum.userapi.repository.PhoneRepository;
import java.util.List;
import java.util.UUID;
import com.nisum.userapi.model.Phone;

@Service
@RequiredArgsConstructor
public class PhoneService {
    private final PhoneRepository repository;

    public Flux<Phone> getByUserId(UUID userId) {
        return repository.getByUserId(userId)
                .switchIfEmpty(Flux.fromIterable(List.of()));
    }
}
