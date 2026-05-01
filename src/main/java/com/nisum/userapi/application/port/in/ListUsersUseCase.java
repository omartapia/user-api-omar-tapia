package com.nisum.userapi.application.port.in;

import com.nisum.userapi.model.User;
import reactor.core.publisher.Flux;

public interface ListUsersUseCase {
    Flux<User> list();
}
