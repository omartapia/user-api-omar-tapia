package com.nisum.userapi.repository;

import com.nisum.userapi.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserRepositoryTest {

    @Mock
    private UserRepository repository;

    @Test
    void givenSavedUserWhenFindByEmailThenReturnsUser() {
        User user = new User();
        user.setName("Omar Tapia");
        user.setEmail("omar@example.com");

        when(repository.findByEmail("omar@example.com"))
                .thenReturn(Mono.just(user));

        StepVerifier.create(repository.findByEmail("omar@example.com"))
                .assertNext(found ->
                        assertThat(found.getName()).isEqualTo("Omar Tapia")
                )
                .verifyComplete();
    }
}