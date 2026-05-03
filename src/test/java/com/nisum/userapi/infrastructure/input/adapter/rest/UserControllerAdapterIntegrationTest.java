package com.nisum.userapi.infrastructure.input.adapter.rest;

import com.nisum.userapi.application.port.in.UserApplicationPort;
import com.nisum.userapi.application.port.in.JwtPort;
import com.nisum.userapi.config.SecurityConfig;
import com.nisum.userapi.domain.Phone;
import com.nisum.userapi.domain.User;
import com.nisum.userapi.dto.PhoneRequest;
import com.nisum.userapi.dto.UserRequest;
import com.nisum.userapi.exception.UserApiException;
import com.nisum.userapi.mapper.UserMapperImpl;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebFluxTest(UserControllerAdapter.class)
@Import({SecurityConfig.class, UserMapperImpl.class})
class UserControllerAdapterIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private UserApplicationPort userApplicationService;

    @MockBean
    private JwtPort jwtPort;

    @Test
    void givenValidRequestWhenPostUsersThenReturnsCreatedUser() {
        UUID id = UUID.randomUUID();
        User saved = user(id, "Juan Rodriguez", "juan@rodriguez.org");
        when(userApplicationService.create(any(User.class))).thenReturn(Mono.just(saved));

        webTestClient.post()
                .uri("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(validRequest())
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.id").isEqualTo(id.toString())
                .jsonPath("$.name").isEqualTo("Juan Rodriguez")
                .jsonPath("$.email").isEqualTo("juan@rodriguez.org")
                .jsonPath("$.token").isEqualTo("jwt-token")
                .jsonPath("$.isactive").isEqualTo(true)
                .jsonPath("$.phones[0].number").isEqualTo("1234567")
                .jsonPath("$.phones[0].citycode").isEqualTo("1")
                .jsonPath("$.phones[0].contrycode").isEqualTo("57");

        verify(userApplicationService).create(argThat(user ->
                "Juan Rodriguez".equals(user.getName())
                        && "juan@rodriguez.org".equals(user.getEmail())
                        && "hunter2".equals(user.getPassword())
                        && user.getPhones().size() == 1
        ));
    }

    @Test
    void givenUsersWhenGetUsersThenReturnsUserList() {
        UUID firstId = UUID.randomUUID();
        UUID secondId = UUID.randomUUID();
        when(userApplicationService.list(0, 10)).thenReturn(Flux.just(
                user(firstId, "Juan Rodriguez", "juan@rodriguez.org"),
                user(secondId, "Maria Perez", "maria@perez.org")
        ));

        webTestClient.get()
                .uri("/users?page=1&size=10")
                .header("Authorization", "Bearer jwt-token")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$[0].id").isEqualTo(firstId.toString())
                .jsonPath("$[0].name").isEqualTo("Juan Rodriguez")
                .jsonPath("$[1].id").isEqualTo(secondId.toString())
                .jsonPath("$[1].email").isEqualTo("maria@perez.org");
    }

    @Test
    void givenExistingUserWhenGetUsersByIdThenReturnsUser() {
        UUID id = UUID.randomUUID();
        when(userApplicationService.get(id)).thenReturn(Mono.just(user(id, "Juan Rodriguez", "juan@rodriguez.org")));

        webTestClient.get()
                .uri("/users/{id}", id)
                .header("Authorization", "Bearer jwt-token")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.id").isEqualTo(id.toString())
                .jsonPath("$.email").isEqualTo("juan@rodriguez.org");
    }

    @Test
    void givenMissingUserWhenGetUsersByIdThenReturnsNotFound() {
        UUID id = UUID.randomUUID();
        when(userApplicationService.get(id)).thenReturn(Mono.empty());

        webTestClient.get()
                .uri("/users/{id}", id)
                .header("Authorization", "Bearer jwt-token")
                .exchange()
                .expectStatus().isNotFound()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.mensaje").isEqualTo("Usuario no encontrado");
    }

    @Test
    void givenValidRequestWhenPutUsersByIdThenReturnsUpdatedUser() {
        UUID id = UUID.randomUUID();
        when(userApplicationService.update(any(UUID.class), any(User.class)))
                .thenReturn(Mono.just(user(id, "Juan Actualizado", "juan@rodriguez.org")));

        webTestClient.put()
                .uri("/users/{id}", id)
                .header("Authorization", "Bearer jwt-token")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(validRequest().name("Juan Actualizado"))
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.id").isEqualTo(id.toString())
                .jsonPath("$.name").isEqualTo("Juan Actualizado");

        verify(userApplicationService).update(argThat(id::equals), argThat(user ->
                "Juan Actualizado".equals(user.getName())
                        && "juan@rodriguez.org".equals(user.getEmail())
        ));
    }

    @Test
    void givenValidRequestWhenPatchUsersByIdThenReturnsPatchedUser() {
        UUID id = UUID.randomUUID();
        when(userApplicationService.patch(any(UUID.class), any(User.class)))
                .thenReturn(Mono.just(user(id, "Juan Rodriguez", "nuevo@rodriguez.org")));

        webTestClient.patch()
                .uri("/users/{id}", id)
                .header("Authorization", "Bearer jwt-token")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(validRequest().email("nuevo@rodriguez.org"))
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.id").isEqualTo(id.toString())
                .jsonPath("$.email").isEqualTo("nuevo@rodriguez.org");

        verify(userApplicationService).patch(argThat(id::equals), argThat(user ->
                "nuevo@rodriguez.org".equals(user.getEmail())
        ));
    }

    @Test
    void givenExistingUserWhenDeleteUsersByIdThenReturnsOk() {
        UUID id = UUID.randomUUID();
        when(userApplicationService.delete(id)).thenReturn(Mono.empty());

        webTestClient.delete()
                .uri("/users/{id}", id)
                .header("Authorization", "Bearer jwt-token")
                .exchange()
                .expectStatus().isNoContent()
                .expectBody().isEmpty();

        verify(userApplicationService).delete(id);
    }

    @Test
    void givenMalformedRequestWhenPostUsersThenReturnsBadRequest() {
        webTestClient.post()
                .uri("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"Juan Rodriguez\",")
                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.mensaje").isEqualTo("Solicitud inválida");
    }

    @Test
    void givenInvalidEmailWhenPostUsersThenReturnsBadRequestErrorResponse() {
        webTestClient.post()
                .uri("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(validRequest().email("invalid-email"))
                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.mensaje").isEqualTo("Solicitud inválida");
    }

    @Test
    void givenInvalidPasswordWhenPostUsersThenReturnsBadRequestErrorResponse() {
        when(userApplicationService.create(any(User.class)))
                .thenReturn(Mono.error(new UserApiException("Formato de contraseña inválido", HttpStatus.BAD_REQUEST)));

        webTestClient.post()
                .uri("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(validRequest().password("short"))
                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.mensaje").isEqualTo("Formato de contraseña inválido");
    }

    @Test
    void givenMissingTokenWhenGetUsersThenReturnsUnauthorized() {
        webTestClient.get()
                .uri("/users")
                .exchange()
                .expectStatus().isUnauthorized()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.mensaje").isEqualTo("Token requerido");
    }

    @Test
    void givenInvalidTokenWhenGetUsersThenReturnsUnauthorizedErrorResponse() {
        doThrow(new IllegalArgumentException("Token expirado"))
                .when(jwtPort).validate("bad-token");

        webTestClient.get()
                .uri("/users")
                .header("Authorization", "Bearer bad-token")
                .exchange()
                .expectStatus().isUnauthorized()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.mensaje").isEqualTo("Token expirado");
    }

    @Test
    void givenServiceNotFoundWhenGetUsersByIdThenReturnsNotFoundErrorResponse() {
        UUID id = UUID.randomUUID();
        when(userApplicationService.get(id))
                .thenReturn(Mono.error(new UserApiException("Usuario no encontrado", HttpStatus.NOT_FOUND)));

        webTestClient.get()
                .uri("/users/{id}", id)
                .header("Authorization", "Bearer jwt-token")
                .exchange()
                .expectStatus().isNotFound()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.mensaje").isEqualTo("Usuario no encontrado");
    }

    @Test
    void givenDuplicateEmailWhenPostUsersThenReturnsConflictErrorResponse() {
        when(userApplicationService.create(any(User.class)))
                .thenReturn(Mono.error(new DuplicateKeyException("duplicate email")));

        webTestClient.post()
                .uri("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(validRequest())
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.CONFLICT)
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.mensaje").isEqualTo("El correo ya está registrado");
    }

    @Test
    void givenUnexpectedServiceErrorWhenGetUsersThenReturnsInternalServerErrorResponse() {
        when(userApplicationService.list(0, 20)).thenReturn(Flux.error(new RuntimeException("boom")));

        webTestClient.get()
                .uri("/users")
                .header("Authorization", "Bearer jwt-token")
                .exchange()
                .expectStatus().is5xxServerError()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.mensaje").isEqualTo("Error interno del servidor");
    }

    private UserRequest validRequest() {
        return new UserRequest()
                .name("Juan Rodriguez")
                .email("juan@rodriguez.org")
                .password("hunter2")
                .phones(List.of(new PhoneRequest()
                        .number("1234567")
                        .citycode("1")
                        .contrycode("57")));
    }

    private User user(UUID id, String name, String email) {
        User user = new User();
        user.setId(id);
        user.setName(name);
        user.setEmail(email);
        user.setPassword("hunter2");
        user.setCreated(LocalDateTime.of(2026, 5, 1, 10, 0));
        user.setModified(LocalDateTime.of(2026, 5, 1, 10, 0));
        user.setLastLogin(LocalDateTime.of(2026, 5, 1, 10, 0));
        user.setToken("jwt-token");
        user.setActive(true);
        user.setPhones(List.of(phone(id)));
        return user;
    }

    private Phone phone(UUID userId) {
        Phone phone = new Phone();
        phone.setNumber("1234567");
        phone.setCitycode("1");
        phone.setContrycode("57");
        phone.setUserId(userId);
        return phone;
    }
}
