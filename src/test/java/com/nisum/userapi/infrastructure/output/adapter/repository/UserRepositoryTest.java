package com.nisum.userapi.infrastructure.output.adapter.repository;

import com.nisum.userapi.domain.User;
import com.nisum.userapi.application.port.out.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserRepositoryTest {

    @Mock
    private UserRepository repository;

    @Test
    void givenSavedUserWhenFindByIdThenReturnsUser() {
        UUID id = UUID.randomUUID();
        User user = new User();
        user.setId(id);
        user.setName("Omar Tapia");
        user.setEmail("omar@example.com");

        when(repository.findById(id))
                .thenReturn(Mono.just(user));

        StepVerifier.create(repository.findById(id))
                .assertNext(found ->
                        assertThat(found.getName()).isEqualTo("Omar Tapia")
                )
                .verifyComplete();
    }
}