package com.nisum.userapi.service;

import com.nisum.userapi.model.User;
import com.nisum.userapi.model.Phone;
import com.nisum.userapi.repository.UserRepository;
import com.nisum.userapi.repository.PhoneRepository;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.util.TestPropertyValues;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    private UserRepository repository;
    @Mock
    private PhoneRepository phoneRepository;
    @Mock
    private JwtService jwtService;
    @InjectMocks
    private UserService service;

    @Test
    void givenUserWithoutTokenWhenCreateUserThenSavesUserWithToken() {
        // given
        User user = new User();
        user.setPhones(new ArrayList<>());

        user.setEmail("omar@example.com");
        when(jwtService.generate("omar@example.com")).thenReturn("jwt-token");
        when(repository.save(user)).thenReturn(Mono.just(user));
        when(phoneRepository.saveAll(any(Iterable.class))).thenReturn(Flux.fromIterable(user.getPhones()));

        // when
        StepVerifier.FirstStep<User> result = StepVerifier.create(service.create(user));

        // then
        result.assertNext(saved -> {
                    assertThat(saved.getToken()).isEqualTo("jwt-token");
                    assertThat(saved).isSameAs(user);
                })
                .verifyComplete();
        verify(jwtService).generate("omar@example.com");
        verify(repository).save(user);
    }

    @Test
    void givenRepositoryUsersWhenListUsersThenReturnsAllUsers() {
        // given
        User firstUser = new User();
        User secondUser = new User();
        when(repository.findAll()).thenReturn(Flux.fromIterable(List.of(firstUser, secondUser)));

        // when
        StepVerifier.FirstStep<User> result = StepVerifier.create(service.list());

        // then
        result.expectNext(firstUser, secondUser).verifyComplete();
    }

    @Test
    void givenExistingUserIdWhenGetUserThenReturnsUser() {
        // given
        UUID id = UUID.randomUUID();
        User user = new User();
        when(repository.findById(id)).thenReturn(Mono.just(user));

        // when
        StepVerifier.FirstStep<User> result = StepVerifier.create(service.get(id));

        // then
        result.expectNext(user).verifyComplete();
    }

    @Test
    void givenMissingUserIdWhenGetUserThenReturnsEmptyMono() {
        // given
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Mono.empty());

        // when
        StepVerifier.FirstStep<User> result = StepVerifier.create(service.get(id));

        // then
        result.verifyComplete();
    }

    @Test
    void givenExistingUserIdWhenDeleteUserThenDeletesById() {
        // given
        UUID id = UUID.randomUUID();
        when(repository.deleteById(id)).thenReturn(Mono.empty());
        // when
       var result = StepVerifier.create(service.delete(id));

        // then
        result.verifyComplete();
        verify(repository).deleteById(id);
    }
}
