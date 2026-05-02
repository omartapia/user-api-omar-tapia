package com.nisum.userapi.infrastructure.output.adapter;

import com.nisum.userapi.domain.Phone;
import com.nisum.userapi.infrastructure.output.adapter.repository.PhoneRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PhonePersistenceAdapterTest {

    @Mock
    private PhoneRepository phoneRepository;

    @InjectMocks
    private PhonePersistenceAdapter adapter;

    @Test
    void saveDelegatesToRepository() {
        Phone phone = new Phone();
        phone.setNumber("1234");

        when(phoneRepository.save(phone)).thenReturn(Mono.just(phone));

        StepVerifier.create(adapter.save(phone))
                .expectNext(phone)
                .verifyComplete();
    }

    @Test
    void findByUserIdDelegatesToRepository() {
        UUID userId = UUID.randomUUID();
        Phone phone = new Phone();
        phone.setNumber("5678");

        when(phoneRepository.findByUserId(userId)).thenReturn(Flux.just(phone));

        StepVerifier.create(adapter.findByUserId(userId))
                .expectNext(phone)
                .verifyComplete();
    }

    @Test
    void deleteByUserIdDelegatesToRepository() {
        UUID userId = UUID.randomUUID();
        when(phoneRepository.deleteByUserId(userId)).thenReturn(Mono.empty());

        StepVerifier.create(adapter.deleteByUserId(userId))
                .verifyComplete();
    }
}
