package com.nisum.userapi.service;

import com.nisum.userapi.exception.UserApiException;
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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
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
        // phoneRepository.save is used by persistPhones; for empty phones list it won't be called

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
        firstUser.setId(UUID.randomUUID());
        User secondUser = new User();
        secondUser.setId(UUID.randomUUID());
        when(repository.findAll()).thenReturn(Flux.fromIterable(List.of(firstUser, secondUser)));
        when(phoneRepository.findByUserId(any())).thenReturn(Flux.empty());
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
        when(phoneRepository.findByUserId(any())).thenReturn(Flux.empty());

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

    @Test
    void givenExistingUserWhenUpdateThenSavesAndReplacesPhones() {
        // given
        UUID id = UUID.randomUUID();
        User existing = new User();
        existing.setId(id);
        existing.setName("old");
        when(repository.findById(id)).thenReturn(Mono.just(existing));

        User update = new User();
        update.setName("new");
        Phone p = new Phone();
        p.setNumber("123");
        update.setPhones(List.of(p));

        when(repository.save(existing)).thenReturn(Mono.just(existing));
        when(phoneRepository.deleteByUserId(id)).thenReturn(Mono.empty());
        when(phoneRepository.save(any(Phone.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        // when
        StepVerifier.create(service.update(id, update))
                .expectNextMatches(u -> u.getName().equals("new"))
                .verifyComplete();

        // then
        verify(repository).save(existing);
        verify(phoneRepository).deleteByUserId(id);
        verify(phoneRepository, times(1)).save(any(Phone.class));
    }

    @Test
    void givenMissingUserWhenUpdateThenReturnsNotFound() {
        // given
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Mono.empty());

        User update = new User();

        // when / then
        StepVerifier.create(service.update(id, update))
                .expectError(UserApiException.class)
                .verify();
    }

    @Test
    void givenExistingUserWhenPatchThenAppliesPartialUpdate() {
        // given
        UUID id = UUID.randomUUID();
        User existing = new User();
        existing.setId(id);
        existing.setName("old");
        existing.setEmail("old@example.com");
        when(repository.findById(id)).thenReturn(Mono.just(existing));

        User patch = new User();
        patch.setEmail("new@example.com");

        when(repository.save(existing)).thenReturn(Mono.just(existing));

        // when
        StepVerifier.create(service.patch(id, patch))
                .expectNextMatches(u -> u.getEmail().equals("new@example.com"))
                .verifyComplete();

        verify(repository).save(existing);
    }

    @Test
    void givenMissingUserWhenPatchThenReturnsNotFound() {
        // given
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Mono.empty());

        User patch = new User();

        // when / then
        StepVerifier.create(service.patch(id, patch))
                .expectError(UserApiException.class)
                .verify();
    }

    @Test
    void givenPhonesWhenReplacePhonesThenDeletesAndPersists() {
        // given
        UUID id = UUID.randomUUID();
        Phone p1 = new Phone();
        Phone p2 = new Phone();
        List<Phone> phones = List.of(p1, p2);

        when(phoneRepository.deleteByUserId(id)).thenReturn(Mono.empty());
        when(phoneRepository.save(any(Phone.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        // when
        StepVerifier.create(service.replacePhones(id, phones)).verifyComplete();

        // then
        verify(phoneRepository).deleteByUserId(id);
        verify(phoneRepository, times(2)).save(any(Phone.class));
    }

    @Test
    void givenPhonesWhenPersistPhonesThenSavesEachPhone() {
        // given
        UUID id = UUID.randomUUID();
        Phone p1 = new Phone();
        p1.setNumber("1");
        Phone p2 = new Phone();
        p2.setNumber("2");
        List<Phone> phones = List.of(p1, p2);

        when(phoneRepository.save(any(Phone.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        // when / then
        StepVerifier.create(service.persistPhones(id, phones))
                .expectNextMatches(p -> p.getNumber().equals("1"))
                .expectNextMatches(p -> p.getNumber().equals("2"))
                .verifyComplete();

        verify(phoneRepository, times(2)).save(any(Phone.class));
    }
}
