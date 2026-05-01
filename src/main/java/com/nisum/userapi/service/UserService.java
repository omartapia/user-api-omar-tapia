package com.nisum.userapi.service;

import com.nisum.userapi.dto.UserPatchRequest;
import com.nisum.userapi.exception.UserApiException;
import com.nisum.userapi.mapper.UserMapper;
import com.nisum.userapi.model.Phone;
import com.nisum.userapi.model.User;
import com.nisum.userapi.repository.PhoneRepository;
import com.nisum.userapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final UserMapper userMapper;
    private final PhoneRepository phoneRepository;

    public Mono<User> create(User user) {
        List<Phone> phones = user.getPhones();
        LocalDateTime now = LocalDateTime.now();
        user.setCreated(now);
        user.setModified(now);
        user.setLastLogin(now);
        user.setActive(true);
        user.setToken(jwtService.generate(user.getEmail()));

        phoneRepository.saveAll(phones.stream()
                .filter(existPhone -> existPhone.getNumber() != null)
                .map(phone -> {
                            phone.setUserId(user.getId());
                            return phone;
                            })
                .collect(Collectors.toList()));

        return userRepository.save(user);
    }

    public Flux<User> list() {
        return userRepository.findAll();
    }

    public Mono<User> get(java.util.UUID uuid) {
        return userRepository.findById(uuid);
    }

    public Mono<Void> delete(java.util.UUID uuid) {
        return userRepository.deleteById(uuid);
    }

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

    public Mono<User> patch(UUID id, UserPatchRequest patch) {
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
                        replacePhones(id,userMapper.toListPhoneEntity(patch.getPhones()));
                    }

                    existing.setModified(LocalDateTime.now());
                    return userRepository.save(existing);
                })
                .switchIfEmpty(
                        Mono.error(new UserApiException("No se ha podido actualizar parcialmente el ususario porque no existe", HttpStatus.NOT_FOUND))
                );
    }

    public Mono<Void> replacePhones(UUID userId, List<Phone> phones) {

        return phoneRepository.deleteAllByUserId(userId)
                .thenMany(
                        Flux.fromIterable(phones)
                                .map(phone -> {
                                    phone.setId(UUID.randomUUID());
                                    phone.setUserId(userId);
                                    return phone;
                                })
                                .flatMap(phoneRepository::save)
                )
                .then();
    }

}
