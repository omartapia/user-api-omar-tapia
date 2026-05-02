package com.nisum.userapi.mapper;

import com.nisum.userapi.dto.UserRequest;
import com.nisum.userapi.dto.UserResponse;
import com.nisum.userapi.domain.User;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UserMapperTest {
    private final UserMapper mapper = Mappers.getMapper(UserMapper.class);

    @Test
    void givenUserRequestWhenToEntityThenMapsInputFieldsAndIgnoresGeneratedFields() {
        // given
        UserRequest request = new UserRequest();
        request.setName("Omar Tapia");
        request.setEmail("omar@example.com");
        request.setPassword("Password1");

        // when
        User result = mapper.toEntity(request);

        // then
        assertThat(result.getName()).isEqualTo("Omar Tapia");
        assertThat(result.getEmail()).isEqualTo("omar@example.com");
        assertThat(result.getPassword()).isEqualTo("Password1");
        assertThat(result.getId()).isNull();
        assertThat(result.getToken()).isNull();
        assertThat(result.isActive()).isFalse();
    }

    @Test
    void givenUserEntityWhenToResponseThenMapsOutputFields() {
        // given
        UUID id = UUID.randomUUID();
        LocalDateTime created = LocalDateTime.now().minusDays(1);
        LocalDateTime modified = LocalDateTime.now();
        LocalDateTime lastLogin = LocalDateTime.now().minusHours(2);
        User user = new User();
        user.setId(id);
        user.setName("Omar Tapia");
        user.setEmail("omar@example.com");
        user.setCreated(created);
        user.setModified(modified);
        user.setLastLogin(lastLogin);
        user.setToken("token");
        user.setActive(true);

        // when
        UserResponse result = mapper.toResponse(user);

        // then
        assertThat(result.getId()).isEqualTo(id);
        assertThat(result.getName()).isEqualTo("Omar Tapia");
        assertThat(result.getEmail()).isEqualTo("omar@example.com");
        assertThat(result.getCreated()).isNotNull();
        assertThat(result.getModified()).isNotNull();
        assertThat(result.getLastLogin()).isNotNull();
        assertThat(result.getToken()).isEqualTo("token");
        assertThat(result.getIsactive()).isTrue();
    }

    @Test
    void givenNullLocalDateTimeWhenMapThenReturnsNull() {
        // given
        LocalDateTime value = null;

        // when
        Object result = mapper.map(value);

        // then
        assertThat(result).isNull();
    }
}
