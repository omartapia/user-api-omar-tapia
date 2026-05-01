package com.nisum.userapi.mapper.infrastructure.adapter;

import com.nisum.userapi.application.port.out.PhonePersistencePort;
import com.nisum.userapi.application.port.out.UserPersistencePort;
import com.nisum.userapi.mapper.infrastructure.repository.PhoneRepository;
import com.nisum.userapi.mapper.infrastructure.repository.UserRepository;
import com.nisum.userapi.model.Phone;
import com.nisum.userapi.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UserPersistenceAdapterIT {

    @Autowired
    private UserPersistencePort userPersistencePort;

    @Autowired
    private PhonePersistencePort phonePersistencePort;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PhoneRepository phoneRepository;

    @BeforeEach
    void setUp() {
        // clean database before each test
        phoneRepository.deleteAll().then(userRepository.deleteAll()).block();
    }

    @Test
    void saveUserAndPhones() {
        User user = new User();
        user.setName("Integration Test");
        user.setEmail("it@example.com");
        user.setPassword("pass");
        user.setCreated(LocalDateTime.now());
        user.setModified(LocalDateTime.now());
        user.setLastLogin(LocalDateTime.now());
        user.setActive(true);

        User saved = userPersistencePort.save(user).block();
        assertNotNull(saved);
        assertNotNull(saved.getId());

        User found = userPersistencePort.findByEmail("it@example.com").block();
        assertNotNull(found);
        assertEquals(saved.getId(), found.getId());

        Phone phone = new Phone();
        phone.setNumber("1234567");
        phone.setCitycode("1");
        phone.setContrycode("57");
        phone.setUserId(saved.getId());

        Phone savedPhone = phonePersistencePort.save(phone).block();
        assertNotNull(savedPhone);
        assertNotNull(savedPhone.getId());

        List<Phone> phones = phonePersistencePort.findByUserId(saved.getId()).collectList().block();
        assertNotNull(phones);
        assertEquals(1, phones.size());
        assertEquals(savedPhone.getId(), phones.get(0).getId());
    }
}
