package com.nisum.userapi.mapper;

import com.nisum.userapi.dto.PhoneRequest;
import com.nisum.userapi.domain.Phone;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PhoneMapperTest {
    private final UserMapper mapper = Mappers.getMapper(UserMapper.class);

    @Test
    void givenPhoneRequestWhenToPhoneEntityThenMapsFields() {
        PhoneRequest req = new PhoneRequest();
        req.setNumber("1234567");
        req.setCitycode("1");
        req.setContrycode("57");

        Phone phone = mapper.toPhoneEntity(req);

        assertThat(phone.getNumber()).isEqualTo("1234567");
        assertThat(phone.getCitycode()).isEqualTo("1");
        assertThat(phone.getContrycode()).isEqualTo("57");
        assertThat(phone.getId()).isNull();
        assertThat(phone.getUserId()).isNull();
    }

    @Test
    void givenPhoneRequestListWhenToListPhoneEntityThenMapsAll() {
        PhoneRequest req1 = new PhoneRequest();
        req1.setNumber("1");
        req1.setCitycode("10");
        req1.setContrycode("57");

        PhoneRequest req2 = new PhoneRequest();
        req2.setNumber("2");
        req2.setCitycode("20");
        req2.setContrycode("58");

        List<Phone> phones = mapper.toListPhoneEntity(List.of(req1, req2));

        assertThat(phones).hasSize(2);
        assertThat(phones.get(0).getNumber()).isEqualTo("1");
        assertThat(phones.get(1).getCitycode()).isEqualTo("20");
    }
}
