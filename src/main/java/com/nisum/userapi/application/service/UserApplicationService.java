package com.nisum.userapi.application.service;

import com.nisum.userapi.application.port.in.UserApplicationPort;
import com.nisum.userapi.application.port.out.PhoneRepository;
import com.nisum.userapi.application.port.out.UserCustomRepository;
import com.nisum.userapi.application.port.out.UserRepository;
import com.nisum.userapi.domain.User;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import io.github.resilience4j.reactor.retry.RetryOperator;
import io.github.resilience4j.retry.Retry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserApplicationService implements UserApplicationPort {

    private final UserRepository userRepository;
    private final PhoneRepository phoneRepository;
    private final UserCustomRepository userCustomRepository;

    private final CircuitBreaker circuitBreaker;
    private final Retry retry;

    // CREATE
    @Override
    public Mono<User> create(User user) {

        return Mono.defer(() ->
                        userRepository.save(user)
                                .flatMap(saved ->
                                        Flux.fromIterable(user.getPhones())
                                                .flatMap(phone -> {
                                                    phone.setUserId(saved.getId());
                                                    return phoneRepository.save(phone);
                                                })
                                                .collectList()
                                                .map(phones -> {
                                                    saved.setPhones(phones);
                                                    return saved;
                                                })
                                )
                )
                .transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
                .transformDeferred(RetryOperator.of(retry));
    }

    @Override
    public Mono<User> get(UUID id) {

        return Mono.defer(() ->
                        userCustomRepository.findByIdWithPhones(id)
                )
                .transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
                .transformDeferred(RetryOperator.of(retry));
    }

    @Override
    public Flux<User> list(int page, int size) {

        return Flux.defer(() ->
                        userCustomRepository.findUsersWithPhonesPaged(page, size)
                )
                .transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
                .transformDeferred(RetryOperator.of(retry));
    }

    @Override
    public Mono<Void> delete(UUID id) {

        return Mono.defer(() ->
                        phoneRepository.findByUserId(id)
                                .flatMap(phoneRepository::delete)
                                .then(userRepository.deleteById(id))
                )
                .transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
                .transformDeferred(RetryOperator.of(retry));
    }

    @Override
    public Mono<User> patch(UUID id, User patch) {

        return Mono.defer(() ->
                        userCustomRepository.findByIdWithPhones(id)
                                .switchIfEmpty(Mono.error(new RuntimeException("User not found")))
                                .flatMap(existing -> {

                                    if (patch.getName() != null) existing.setName(patch.getName());
                                    if (patch.getEmail() != null) existing.setEmail(patch.getEmail());
                                    if (patch.getPassword() != null) existing.setPassword(patch.getPassword());

                                    existing.setModified(LocalDateTime.now());

                                    return userRepository.save(existing)
                                            .map(saved -> {
                                                saved.setPhones(existing.getPhones());
                                                return saved;
                                            });
                                })
                )
                .transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
                .transformDeferred(RetryOperator.of(retry));
    }

    @Override
    public Mono<User> update(UUID id, User user) {
        return Mono.defer(() ->
                        userCustomRepository.findByIdWithPhones(id)
                                .switchIfEmpty(Mono.error(new RuntimeException("User not found")))
                                .flatMap(existing -> {

                                    existing.setName(user.getName());
                                    existing.setEmail(user.getEmail());
                                    existing.setPassword(user.getPassword());
                                    existing.setModified(LocalDateTime.now());

                                    return userRepository.save(existing)
                                            .flatMap(saved ->
                                                    Flux.fromIterable(existing.getPhones())
                                                            .flatMap(phoneRepository::delete)
                                                            .thenMany(
                                                                    Flux.fromIterable(user.getPhones())
                                                                            .flatMap(phone -> {
                                                                                phone.setUserId(id);
                                                                                return phoneRepository.save(phone);
                                                                            })
                                                            )
                                                            .collectList()
                                                            .map(phones -> {
                                                                saved.setPhones(phones);
                                                                return saved;
                                                            })
                                            );
                                })
                )
                .transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
                .transformDeferred(RetryOperator.of(retry));
    }
}

