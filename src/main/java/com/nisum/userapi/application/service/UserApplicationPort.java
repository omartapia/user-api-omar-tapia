package com.nisum.userapi.application.service;

import com.nisum.userapi.application.port.in.*;
import com.nisum.userapi.application.port.out.PhonePersistencePort;
import com.nisum.userapi.application.port.out.UserPersistencePort;
import com.nisum.userapi.exception.UserApiException;
import com.nisum.userapi.domain.Phone;
import com.nisum.userapi.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DuplicateKeyException;
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
public class UserApplicationPort implements com.nisum.userapi.application.port.in.UserApplicationPort {
    private final UserPersistencePort userPersistencePort;
    private final PhonePersistencePort phonePersistencePort;
    private final JwtPort jwtPort;
    private final UserValidator userValidator;


    private final CircuitBreaker userCircuit;
    private final Retry userRetry;

    private final CircuitBreaker phoneCircuit;
    private final Retry phoneRetry;

    @Autowired
    public UserApplicationPort(UserPersistencePort userPersistencePort,
                               PhonePersistencePort phonePersistencePort,
                               JwtPort jwtPort,
                               UserValidator userValidator,
                               @Qualifier("userCircuitBreaker") CircuitBreaker userCircuit,
                               @Qualifier("userRetry") Retry userRetry,
                               @Qualifier("phoneCircuitBreaker") CircuitBreaker phoneCircuit,
                               @Qualifier("phoneRetry") Retry phoneRetry) {
        this.userPersistencePort = userPersistencePort;
        this.phonePersistencePort = phonePersistencePort;
        this.jwtPort = jwtPort;
        this.userValidator = userValidator;
        this.userCircuit = userCircuit;
        this.userRetry = userRetry;
        this.phoneCircuit = phoneCircuit;
        this.phoneRetry = phoneRetry;
    }

    @Transactional
    public Mono<User> create(User user) {
        return Mono.defer(() -> {
            userValidator.validateForCreate(user);

            LocalDateTime now = LocalDateTime.now();
            user.setCreated(now);
            user.setModified(now);
            user.setLastLogin(now);
            user.setActive(true);
            user.setToken(jwtPort.generate(user.getEmail()));

            return userPersistencePort.save(user)
                    .transformDeferred(RetryOperator.of(userRetry))
                    .transformDeferred(CircuitBreakerOperator.of(userCircuit))
                    .flatMap(saved ->
                            persistPhones(saved.getId(), user.getPhones())
                                    .collectList()
                                    .doOnNext(saved::setPhones)
                                    .thenReturn(saved)

                    )
                    .onErrorMap(this::mapCreateError);
        });
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
                .switchIfEmpty(Mono.error(new UserApiException("Usuario no encontrado", HttpStatus.NOT_FOUND)))
                .flatMap(user -> {
                    user.setActive(false);
                    user.setModified(LocalDateTime.now());
                    return userPersistencePort.save(user)
                            .then();
                });
    }

    @Transactional
    public Mono<User> update(UUID id, User user) {
        return Mono.defer(() -> {
            userValidator.validateForUpdate(user);

            return userPersistencePort.findById(id)
                    .flatMap(existing -> {
                        existing.setName(user.getName());
                        existing.setEmail(user.getEmail());
                        existing.setPassword(user.getPassword());
                        existing.setModified(LocalDateTime.now());
                        existing.setActive(true);
                        return replacePhones(id, user.getPhones())
                                .then(userPersistencePort.save(existing));
                    }).switchIfEmpty(
                            Mono.error(new UserApiException("No se ha podido actualizar el ususario porque no existe", HttpStatus.NOT_FOUND))
                    );
        });

    }

    public Mono<User> patch(UUID id, User patch) {
        return Mono.defer(() -> {
            userValidator.validateForPatch(patch);

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

                        existing.setModified(LocalDateTime.now());
                        existing.setActive(true);

                        return replacePhones(id, patch.getPhones())
                                .then(userPersistencePort.save(existing));
                    })
                    .switchIfEmpty(
                            Mono.error(new UserApiException("No se ha podido actualizar parcialmente el ususario porque no existe", HttpStatus.NOT_FOUND))
                    );
        });
    }

    public Mono<Void> replacePhones(UUID userId, List<Phone> phones) {
        return phonePersistencePort.deleteByUserId(userId)
                .thenMany(persistPhones(userId, phones))
                .then();
    }

    public Flux<Phone> persistPhones(UUID userId, List<Phone> phones) {
        if (phones == null || phones.isEmpty()) {
            return Flux.empty();
        }

        return Flux.fromIterable(phones)
                .flatMap(phone -> {
                    phone.setUserId(userId);
                    return phonePersistencePort.save(phone)
                            .transformDeferred(RetryOperator.of(phoneRetry))
                            .transformDeferred(CircuitBreakerOperator.of(phoneCircuit))
                            .onErrorMap(err -> new UserApiException("Error persisting phone", HttpStatus.SERVICE_UNAVAILABLE));
                });
    }

    private Throwable mapCreateError(Throwable err) {
        if (err instanceof UserApiException || err instanceof DuplicateKeyException) {
            return err;
        }

        return new UserApiException("Error saving user", HttpStatus.SERVICE_UNAVAILABLE);
    }
}
