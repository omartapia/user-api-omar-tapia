package com.nisum.userapi.infrastructure.output.adapter.repository;

import com.nisum.userapi.application.port.out.PhoneRepository;
import com.nisum.userapi.domain.Phone;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PhoneRepositoryTest {

    @Mock
    private PhoneRepository repository;

    @Test
    void givenSavedPhoneWhenFindByUserIdThenReturnsPhone() {
        Phone phone = new Phone();
        phone.setNumber("1234567");
        UUID userId = UUID.randomUUID();

        when(repository.findByUserId(userId)).thenReturn(Flux.just(phone));

        StepVerifier.create(repository.findByUserId(userId))
                .assertNext(found -> assertThat(found.getNumber()).isEqualTo("1234567"))
                .verifyComplete();
    }
}
