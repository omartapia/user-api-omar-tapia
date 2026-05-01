package com.nisum.userapi.application.usecase;

import com.nisum.userapi.model.User;
import reactor.core.publisher.Flux;

public interface ListUsersUseCase {
    Flux<User> list();
}
