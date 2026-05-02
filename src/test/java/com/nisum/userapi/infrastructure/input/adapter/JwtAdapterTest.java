package com.nisum.userapi.infrastructure.input.adapter;

import com.nisum.userapi.config.JwtConfigProperties;
import com.nisum.userapi.exception.JwtAuthenticationException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class JwtAdapterTest {

    @Test
    void generateAndValidate() {
        JwtConfigProperties props = new JwtConfigProperties("01234567890123456789012345678901", 3600000);

        JwtAdapter adapter = new JwtAdapter(props);
        String token = adapter.generate("user@example.com");
        assertNotNull(token);

        // validate should not throw
        adapter.validate(token);
    }

    @Test
    void validateMalformedTokenThrows() {
        JwtConfigProperties props = new JwtConfigProperties("01234567890123456789012345678901", 3600000);

        JwtAdapter adapter = new JwtAdapter(props);

        JwtAuthenticationException ex = assertThrows(JwtAuthenticationException.class, () -> adapter.validate("not-a-jwt"));
        assertEquals(org.springframework.http.HttpStatus.UNAUTHORIZED, ex.getStatus());
    }

    @Test
    void validateExpiredTokenThrows() {
        JwtConfigProperties props = new JwtConfigProperties("01234567890123456789012345678901", 3600000);

        String secret = props.getSecret();
        String expiredToken = Jwts.builder()
                .setSubject("user@example.com")
                .setIssuedAt(new Date(System.currentTimeMillis() - 2000))
                .setExpiration(new Date(System.currentTimeMillis() - 1000))
                .signWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)))
                .compact();

        JwtAdapter adapter = new JwtAdapter(props);

        JwtAuthenticationException ex = assertThrows(JwtAuthenticationException.class, () -> adapter.validate(expiredToken));
        assertEquals(org.springframework.http.HttpStatus.UNAUTHORIZED, ex.getStatus());
    }
}
