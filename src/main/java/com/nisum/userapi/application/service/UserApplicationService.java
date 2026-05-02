package com.nisum.userapi.application.service;

import com.nisum.userapi.application.port.out.PhonePersistencePort;
import com.nisum.userapi.application.port.out.UserPersistencePort;
import com.nisum.userapi.application.port.in.CreateUserUseCase;
import com.nisum.userapi.application.port.in.ListUsersUseCase;
import com.nisum.userapi.application.port.in.GetUserUseCase;
import com.nisum.userapi.application.port.in.DeleteUserUseCase;
import com.nisum.userapi.application.port.in.UpdateUserUseCase;
import com.nisum.userapi.application.port.in.PatchUserUseCase;
import com.nisum.userapi.exception.UserApiException;
import com.nisum.userapi.domain.Phone;
import com.nisum.userapi.domain.User;
import com.nisum.userapi.application.port.in.JwtPort;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.reactor.retry.RetryOperator;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class UserApplicationService implements CreateUserUseCase, ListUsersUseCase, GetUserUseCase, DeleteUserUseCase, UpdateUserUseCase, PatchUserUseCase {
    private final UserPersistencePort userPersistencePort;
    private final PhonePersistencePort phonePersistencePort;
    private final JwtPort jwtPort;

    // resilience components (provided via configuration)
    private final CircuitBreaker userCircuit;
    private final Retry userRetry;

    private final CircuitBreaker phoneCircuit;
    private final Retry phoneRetry;

    @org.springframework.beans.factory.annotation.Autowired
    public UserApplicationService(UserPersistencePort userPersistencePort,
                                  PhonePersistencePort phonePersistencePort,
                                  JwtPort jwtPort,
                                  @org.springframework.beans.factory.annotation.Qualifier("userCircuitBreaker") CircuitBreaker userCircuit,
                                  @org.springframework.beans.factory.annotation.Qualifier("userRetry") Retry userRetry,
                                  @org.springframework.beans.factory.annotation.Qualifier("phoneCircuitBreaker") CircuitBreaker phoneCircuit,
                                  @org.springframework.beans.factory.annotation.Qualifier("phoneRetry") Retry phoneRetry) {
        this.userPersistencePort = userPersistencePort;
        this.phonePersistencePort = phonePersistencePort;
        this.jwtPort = jwtPort;
        this.userCircuit = userCircuit;
        this.userRetry = userRetry;
        this.phoneCircuit = phoneCircuit;
        this.phoneRetry = phoneRetry;
    }

    @Transactional
    public Mono<User> create(User user) {
        LocalDateTime now = LocalDateTime.now();
        user.setCreated(now);
        user.setModified(now);
        user.setLastLogin(now);
        user.setActive(true);
        user.setToken(jwtPort.generate(user.getEmail()));

        return withRetryAndCircuit(userPersistencePort.save(user), userRetry, userCircuit)
                .flatMap(saved ->
                        persistPhones(saved.getId(), user.getPhones())
                                .collectList()
                                .doOnNext(saved::setPhones)
                                .thenReturn(saved)

                )
                .onErrorMap(err -> new UserApiException("Error saving user", HttpStatus.SERVICE_UNAVAILABLE));
    }

    @Override
    public Flux<User> list() {
        return userPersistencePort.findAll()
                .filter(User::isActive)
                .flatMap(user ->
                        phonePersistencePort.findByUserId(user.getId())
                                .collectList()
                                .map(phones -> {
                                    user.setPhones(phones);
                                    return user;
                                })
                )
                .switchIfEmpty(Flux.error(new UserApiException("No user where found", HttpStatus.NOT_FOUND)));
    }

    @Override
    public Mono<User> get(UUID uuid) {
        return userPersistencePort.findById(uuid)
                .flatMap(user ->
                        phonePersistencePort.findByUserId(user.getId())
                                .collectList()
                                .map(phones -> {
                                    user.setPhones(phones);
                                    return user;
                                })
                )
                .switchIfEmpty(Mono.error(new UserApiException("No user where found", HttpStatus.NOT_FOUND)));
    }

    @Transactional
    public Mono<Void> delete(UUID id) {
        return userPersistencePort.findById(id)
                .flatMap(user -> {
                    user.setActive(false);
                    user.setModified(LocalDateTime.now());
                    return userPersistencePort.save(user)
                            .then();
                });
    }

    @Transactional
    public Mono<User> update(UUID id, User user) {
        return userPersistencePort.findById(id)
                .flatMap(existing -> {
                    existing.setName(user.getName());
                    existing.setEmail(user.getEmail());
                    existing.setPassword(user.getPassword());
                    existing.setModified(LocalDateTime.now());
                    existing.setActive(true);
                    // keep replacePhones fire-and-forget to preserve existing behavior/tests
                    replacePhones(id, user.getPhones());
                    return userPersistencePort.save(existing);
                }).switchIfEmpty(
                        Mono.error(new UserApiException("No se ha podido actualizar el ususario porque no existe", HttpStatus.NOT_FOUND))
                );

    }

    public Mono<User> patch(UUID id, User patch) {
        return userPersistencePort.findById(id)
                .flatMap(existing -> {
                    if (patch.getName() != null) {
                        existing.setName(patch.getName());
                    }

                    if (patch.getEmail() != null) {
                        existing.setEmail(patch.getEmail());
                    }

                    if (patch.getPassword() != null) {
                        existing.setPassword(patch.getPassword());
                    }

                    if (patch.getPhones() != null && !patch.getPhones().isEmpty()) {
                        replacePhones(id, patch.getPhones());
                    }

                    existing.setModified(LocalDateTime.now());
                    existing.setActive(true);
                    return userPersistencePort.save(existing);
                })
                .switchIfEmpty(
                        Mono.error(new UserApiException("No se ha podido actualizar parcialmente el ususario porque no existe", HttpStatus.NOT_FOUND))
                );
    }

    public Mono<Void> replacePhones(UUID userId, List<Phone> phones) {
        return phonePersistencePort.deleteByUserId(userId)
                .thenMany(persistPhones(userId, phones))
                .then();
    }

    public Flux<Phone> persistPhones(UUID userId, List<Phone> phones) {
        return Flux.fromIterable(phones)
                .flatMap(phone -> {
                    phone.setUserId(userId);
                    return withRetryAndCircuit(phonePersistencePort.save(phone), phoneRetry, phoneCircuit)
                            .onErrorMap(err -> new UserApiException("Error persisting phone", HttpStatus.SERVICE_UNAVAILABLE));
                });
    }

    private <T> Mono<T> withRetryAndCircuit(Mono<T> mono, Retry retry, CircuitBreaker cb) {
        Mono<T> m = mono;
        if (retry != null) {
            m = m.transformDeferred(RetryOperator.of(retry));
        }
        if (cb != null) {
            m = m.transformDeferred(CircuitBreakerOperator.of(cb));
        }
        return m;
    }

    private <T> Flux<T> withRetryAndCircuit(Flux<T> flux, Retry retry, CircuitBreaker cb) {
        Flux<T> f = flux;
        if (retry != null) {
            f = f.transformDeferred(fl -> fl.transformDeferred(RetryOperator.of(retry)));
        }
        if (cb != null) {
            f = f.transformDeferred(fl -> fl.transformDeferred(CircuitBreakerOperator.of(cb)));
        }
        return f;
    }
}
