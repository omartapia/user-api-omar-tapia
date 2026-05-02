package com.nisum.userapi.domain;

import com.nisum.userapi.domain.User;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UserTest {

    @Test
    void givenUserPropertiesWhenSettersAreCalledThenGettersReturnSameValues() {
        // given
        UUID id = UUID.randomUUID();
        User user = new User();

        // when
        user.setId(id);
        user.setName("Omar Tapia");
        user.setEmail("omar@example.com");
        user.setPassword("Password1");
        user.setToken("token");
        user.setActive(true);

        // then
        assertThat(user.getId()).isEqualTo(id);
        assertThat(user.getName()).isEqualTo("Omar Tapia");
        assertThat(user.getEmail()).isEqualTo("omar@example.com");
        assertThat(user.getPassword()).isEqualTo("Password1");
        assertThat(user.getToken()).isEqualTo("token");
        assertThat(user.isActive()).isTrue();
    }
}
