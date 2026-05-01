package com.nisum.userapi.controller;

import com.nisum.userapi.dto.UserRequest;
import com.nisum.userapi.dto.UserResponse;
import com.nisum.userapi.mapper.UserMapper;
import com.nisum.userapi.model.User;
import com.nisum.userapi.service.impl.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {
    @Mock
    private UserServiceImpl service;

    @Mock
    private UserMapper mapper;
    @InjectMocks
    private UserController controller;

    @Test
    void givenValidUserRequestWhenCreateUserThenReturnsUserResponse() {
        // given
        UserRequest request = new UserRequest();
        request.setPhones(new ArrayList<>());
        User entity = new User();
        User saved = new User();
        UserResponse response = new UserResponse();
        when(mapper.toEntity(request)).thenReturn(entity);
        when(service.create(entity)).thenReturn(Mono.just(saved));
        when(mapper.toResponse(saved)).thenReturn(response);

        // when
        Mono<ResponseEntity<UserResponse>> result = controller.createUser(Mono.just(request), null);

        // then
        StepVerifier.create(result)
                .assertNext(entityResponse -> {
                    assertThat(entityResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
                    assertThat(entityResponse.getBody()).isSameAs(response);
                })
                .verifyComplete();
    }

    @Test
    void givenExistingUsersWhenListUsersThenReturnsFluxOfUserResponses() {
        // given
        User firstUser = new User();
        User secondUser = new User();
        UserResponse firstResponse = new UserResponse();
        UserResponse secondResponse = new UserResponse();
        when(service.list()).thenReturn(Flux.just(firstUser, secondUser));
        when(mapper.toResponse(firstUser)).thenReturn(firstResponse);
        when(mapper.toResponse(secondUser)).thenReturn(secondResponse);

        // when
        Mono<ResponseEntity<Flux<UserResponse>>> result = controller.listUsers(null);

        // then
        StepVerifier.create(result)
                .assertNext(entityResponse -> {
                    assertThat(entityResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
                    StepVerifier.create(entityResponse.getBody())
                            .expectNext(firstResponse, secondResponse)
                            .verifyComplete();
                })
                .verifyComplete();
    }

    @Test
    void givenExistingUserIdWhenGetUserThenReturnsUserResponse() {
        // given
        UUID id = UUID.randomUUID();
        User user = new User();
        UserResponse response = new UserResponse();
        when(service.get(id)).thenReturn(Mono.just(user));
        when(mapper.toResponse(user)).thenReturn(response);

        // when
        Mono<ResponseEntity<UserResponse>> result = controller.getUserById(id, null);

        // then
        StepVerifier.create(result)
                .assertNext(entityResponse -> {
                    assertThat(entityResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
                    assertThat(entityResponse.getBody()).isSameAs(response);
                })
                .verifyComplete();
    }

    @Test
    void givenMissingUserIdWhenGetUserThenReturnsNotFound() {
        // given
        UUID id = UUID.randomUUID();
        when(service.get(id)).thenReturn(Mono.empty());

        // when
        Mono<ResponseEntity<UserResponse>> result = controller.getUserById(id, null);

        // then
        StepVerifier.create(result)
                .assertNext(entityResponse -> assertThat(entityResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND))
                .verifyComplete();
    }

    @Test
    void givenExistingUserIdWhenDeleteUserThenReturnsOk() {
        // given
        UUID id = UUID.randomUUID();
        when(service.delete(id)).thenReturn(Mono.empty());

        // when
        Mono<ResponseEntity<Void>> result = controller.deleteUser(id, null);

        // then
        StepVerifier.create(result)
                .assertNext(entityResponse -> assertThat(entityResponse.getStatusCode()).isEqualTo(HttpStatus.OK))
                .verifyComplete();
        verify(service).delete(id);
    }
}
