package com.nisum.userapi.model;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PhoneTest {

    @Test
    void givenPhonePropertiesWhenSettersAreCalledThenGettersReturnSameValues() {
        // given
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Phone phone = new Phone();

        // when
        phone.setId(id);
        phone.setNumber("123456789");
        phone.setCitycode("01");
        phone.setContrycode("57");
        phone.setUserId(userId);

        // then
        assertThat(phone.getId()).isEqualTo(id);
        assertThat(phone.getNumber()).isEqualTo("123456789");
        assertThat(phone.getCitycode()).isEqualTo("01");
        assertThat(phone.getContrycode()).isEqualTo("57");
        assertThat(phone.getUserId()).isEqualTo(userId);
    }
}