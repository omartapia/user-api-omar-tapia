package com.nisum.userapi.service;

import com.nisum.userapi.exception.UserApiException;
import com.nisum.userapi.model.Phone;
import com.nisum.userapi.model.User;
import com.nisum.userapi.repository.PhoneRepository;
import com.nisum.userapi.repository.UserRepository;
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
public class UserService implements IUserService {
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PhoneRepository phoneRepository;

    @Transactional
    public Mono<User> create(User user) {
        LocalDateTime now = LocalDateTime.now();
        user.setCreated(now);
        user.setModified(now);
        user.setLastLogin(now);
        user.setActive(true);
        user.setToken(jwtService.generate(user.getEmail()));

        return userRepository.save(user)
                .flatMap(saved ->
                        persistPhones(saved.getId(), user.getPhones())
                                .collectList()
                                .doOnNext(saved::setPhones)
                                .thenReturn(saved)

                );
    }

    public Flux<User> list() {
        return userRepository.findAll()
                .flatMap(user ->
                        phoneRepository.findByUserId(user.getId())
                                .collectList()
                                .map(phones -> {
                                    user.setPhones(phones);
                                    return user;
                                })
                );
    }

    public Mono<User> get(java.util.UUID uuid) {
        return userRepository.findById(uuid)
                .flatMap(user ->
                        phoneRepository.findByUserId(user.getId())
                                .collectList()
                                .map(phones -> {
                                    user.setPhones(phones);
                                    return user;
                                })
                );
    }

    @Transactional
    public Mono<Void> delete(UUID id) {
        return userRepository.deleteById(id)
                .then(phoneRepository.deleteByUserId(id));
    }

    @Transactional
    public Mono<User> update(UUID id, User user) {
        return userRepository.findById(id)
                .flatMap(existing -> {
                    existing.setName(user.getName());
                    existing.setEmail(user.getEmail());
                    existing.setPassword(user.getPassword());
                    existing.setModified(LocalDateTime.now());
                    replacePhones(id, user.getPhones());
                    return userRepository.save(existing);
                }).switchIfEmpty(
                        Mono.error(new UserApiException("No se ha podido actualizar el ususario porque no existe", HttpStatus.NOT_FOUND))
                );

    }

    public Mono<User> patch(UUID id, User patch) {
        return userRepository.findById(id)
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
                    return userRepository.save(existing);
                })
                .switchIfEmpty(
                        Mono.error(new UserApiException("No se ha podido actualizar parcialmente el ususario porque no existe", HttpStatus.NOT_FOUND))
                );
    }

    public Mono<Void> replacePhones(UUID userId, List<Phone> phones) {
        return phoneRepository.deleteByUserId(userId)
                .thenMany(persistPhones(userId, phones))
                .then();
    }

    public Flux<Phone> persistPhones(UUID userId, List<Phone> phones) {
        return Flux.fromIterable(phones)
                .flatMap(phone -> {
                    phone.setUserId(userId);
                    return phoneRepository.save(phone);
                });
    }

}
