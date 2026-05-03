package com.nisum.userapi.application.service;

import com.nisum.userapi.application.port.in.JwtPort;
import com.nisum.userapi.application.port.out.PhoneRepository;
import com.nisum.userapi.application.port.out.UserCustomRepository;
import com.nisum.userapi.application.port.out.UserRepository;
import com.nisum.userapi.domain.Phone;
import com.nisum.userapi.domain.User;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.retry.Retry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserApplicationServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private PhoneRepository phoneRepository;
    @Mock
    private UserCustomRepository userCustomRepository;
    @Mock
    private JwtPort jwtPort;

    private UserApplicationService service;

    @BeforeEach
    void setUpService() {
        CircuitBreaker circuitBreaker = CircuitBreaker.ofDefaults("testCircuit");
        Retry retry = Retry.ofDefaults("testRetry");
        service = new UserApplicationService(userRepository, phoneRepository, userCustomRepository, jwtPort, circuitBreaker, retry);
    }

    @Test
    void givenUserWhenCreateThenSavesUserAndPhones() {
        // given
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);
        user.setEmail("test@example.com");
        user.setPassword("password123");

        Phone phone = new Phone();
        phone.setNumber("123456789");
        user.setPhones(List.of(phone));

        User savedUser = new User();
        savedUser.setId(userId);
        savedUser.setEmail("test@example.com");

        Phone savedPhone = new Phone();
        savedPhone.setId(UUID.randomUUID());
        savedPhone.setUserId(userId);
        savedPhone.setNumber("123456789");

        when(userRepository.save(user)).thenReturn(Mono.just(savedUser));
        when(phoneRepository.save(any(Phone.class))).thenReturn(Mono.just(savedPhone));
        when(jwtPort.generate("test@example.com")).thenReturn("mock-jwt-token");

        // when
        StepVerifier.create(service.create(user))
                .expectNextMatches(u -> u.getId().equals(userId))
                .verifyComplete();

        // then
        verify(userRepository).save(user);
        verify(phoneRepository).save(any(Phone.class));
    }

    @Test
    void givenPageWhenListThenReturnsUsersPaged() {
        // given
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setPhones(new ArrayList<>());

        when(userCustomRepository.findUsersWithPhonesPaged(0, 10))
                .thenReturn(Flux.just(user));

        // when
        StepVerifier.create(service.list(0, 10))
                .expectNext(user)
                .verifyComplete();

        // then
        verify(userCustomRepository).findUsersWithPhonesPaged(0, 10);
    }

    @Test
    void givenExistingUserIdWhenGetUserThenReturnsUserWithPhones() {
        // given
        UUID id = UUID.randomUUID();
        User user = new User();
        user.setId(id);

        Phone phone = new Phone();
        phone.setUserId(id);
        user.setPhones(List.of(phone));

        when(userCustomRepository.findByIdWithPhones(id)).thenReturn(Mono.just(user));

        // when
        StepVerifier.create(service.get(id))
                .expectNext(user)
                .verifyComplete();

        // then
        verify(userCustomRepository).findByIdWithPhones(id);
    }

    @Test
    void givenMissingUserIdWhenGetUserThenReturnsEmpty() {
        // given
        UUID id = UUID.randomUUID();
        when(userCustomRepository.findByIdWithPhones(id)).thenReturn(Mono.empty());

        // when & then
        StepVerifier.create(service.get(id))
                .expectError(RuntimeException.class)
                .verify();

    }

    @Test
    void givenExistingUserWhenDeleteThenDeletesUserAndPhones() {
        // given
        UUID id = UUID.randomUUID();

        Phone phone = new Phone();
        phone.setId(UUID.randomUUID());

        when(phoneRepository.findByUserId(id))
                .thenReturn(Flux.just(phone));
        when(phoneRepository.delete(phone)).thenReturn(Mono.empty());
        when(userRepository.deleteById(id)).thenReturn(Mono.empty());

        // when
        StepVerifier.create(service.delete(id))
                .expectComplete()
                .verify();

        // then
        verify(phoneRepository).findByUserId(id);
        verify(phoneRepository).delete(phone);
        verify(userRepository).deleteById(id);
    }

    @Test
    void givenExistingUserWhenPatchThenAppliesPartialUpdate() {
        // given
        UUID id = UUID.randomUUID();
        User existing = new User();
        existing.setId(id);
        existing.setName("old");
        existing.setEmail("old@example.com");
        existing.setPassword("oldpassword");
        existing.setModified(LocalDateTime.now());
        existing.setPhones(new ArrayList<>());

        User patch = new User();
        patch.setEmail("new@example.com");

        when(userCustomRepository.findByIdWithPhones(id))
                .thenReturn(Mono.just(existing));
        when(userRepository.save(any(User.class)))
                .thenReturn(Mono.just(existing));

        // when
        StepVerifier.create(service.patch(id, patch))
                .expectNextMatches(u -> u.getId().equals(id))
                .verifyComplete();

        // then
        verify(userCustomRepository).findByIdWithPhones(id);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void givenMissingUserWhenPatchThenReturnsError() {
        // given
        UUID id = UUID.randomUUID();
        User patch = new User();

        when(userCustomRepository.findByIdWithPhones(id))
                .thenReturn(Mono.empty());

        // when / then
        StepVerifier.create(service.patch(id, patch))
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    void givenExistingUserWhenUpdateThenReplacesUserAndPhones() {
        // given
        UUID id = UUID.randomUUID();
        User existing = new User();
        existing.setId(id);
        existing.setName("old");

        Phone oldPhone = new Phone();
        oldPhone.setId(UUID.randomUUID());
        existing.setPhones(List.of(oldPhone));

        User update = new User();
        update.setName("new");
        update.setEmail("new@example.com");
        update.setPassword("newpassword");

        Phone newPhone = new Phone();
        newPhone.setNumber("987654321");
        update.setPhones(List.of(newPhone));

        User savedUser = new User();
        savedUser.setId(id);
        savedUser.setName("new");

        Phone savedPhone = new Phone();
        savedPhone.setId(UUID.randomUUID());
        savedPhone.setUserId(id);

        when(userCustomRepository.findByIdWithPhones(id))
                .thenReturn(Mono.just(existing));
        when(userRepository.save(any(User.class)))
                .thenReturn(Mono.just(savedUser));
        when(phoneRepository.delete(any(Phone.class)))
                .thenReturn(Mono.empty());
        when(phoneRepository.save(any(Phone.class)))
                .thenReturn(Mono.just(savedPhone));

        // when
        StepVerifier.create(service.update(id, update))
                .expectNextMatches(u -> u.getId().equals(id))
                .verifyComplete();

        // then
        verify(userCustomRepository).findByIdWithPhones(id);
        verify(userRepository).save(any(User.class));
        verify(phoneRepository).delete(any(Phone.class));
        verify(phoneRepository).save(any(Phone.class));
    }

    @Test
    void givenMissingUserWhenUpdateThenReturnsError() {
        // given
        UUID id = UUID.randomUUID();
        User update = new User();

        when(userCustomRepository.findByIdWithPhones(id))
                .thenReturn(Mono.empty());

        // when / then
        StepVerifier.create(service.update(id, update))
                .expectError(RuntimeException.class)
                .verify();
    }
}
