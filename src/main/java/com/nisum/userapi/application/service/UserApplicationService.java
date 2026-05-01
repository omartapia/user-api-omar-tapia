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
import com.nisum.userapi.model.Phone;
import com.nisum.userapi.model.User;
import com.nisum.userapi.application.port.out.JwtPort;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserApplicationService implements CreateUserUseCase, ListUsersUseCase, GetUserUseCase, DeleteUserUseCase, UpdateUserUseCase, PatchUserUseCase {
    private final UserPersistencePort userPersistencePort;
    private final PhonePersistencePort phonePersistencePort;
    private final JwtPort jwtPort;

    @Transactional
    public Mono<User> create(User user) {
        LocalDateTime now = LocalDateTime.now();
        user.setCreated(now);
        user.setModified(now);
        user.setLastLogin(now);
        user.setActive(true);
        user.setToken(jwtPort.generate(user.getEmail()));

        return userPersistencePort.save(user)
                .flatMap(saved ->
                        persistPhones(saved.getId(), user.getPhones())
                                .collectList()
                                .doOnNext(saved::setPhones)
                                .thenReturn(saved)

                );
    }

    @Override
    public Flux<User> list() {
        return userPersistencePort.findAll()
                .flatMap(user ->
                        phonePersistencePort.findByUserId(user.getId())
                                .collectList()
                                .map(phones -> {
                                    user.setPhones(phones);
                                    return user;
                                })
                );
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
                );
    }

    @Transactional
    public Mono<Void> delete(UUID id) {
        return userPersistencePort.deleteById(id)
                .then(phonePersistencePort.deleteByUserId(id));
    }

    @Transactional
    public Mono<User> update(UUID id, User user) {
        return userPersistencePort.findById(id)
                .flatMap(existing -> {
                    existing.setName(user.getName());
                    existing.setEmail(user.getEmail());
                    existing.setPassword(user.getPassword());
                    existing.setModified(LocalDateTime.now());
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
                    return phonePersistencePort.save(phone);
                });
    }
}
