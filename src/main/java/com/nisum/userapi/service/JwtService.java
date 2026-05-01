package com.nisum.userapi.service;

import com.nisum.userapi.config.JwtConfigProperties;
import com.nisum.userapi.exception.JwtAuthenticationException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class JwtService implements IJwtService {
    private final JwtConfigProperties jwtConfigProperties;

    public String generate(String subject) {
        return Jwts.builder().setSubject(subject)
                .setIssuedAt(new Date()).setExpiration(new Date(System.currentTimeMillis() + jwtConfigProperties.getTtl()))
                .signWith(Keys.hmacShaKeyFor(jwtConfigProperties.getSecret().getBytes(StandardCharsets.UTF_8)), SignatureAlgorithm.HS256)
                .compact();
    }

    public void validate(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(
                            Keys.hmacShaKeyFor(
                                    jwtConfigProperties.getSecret()
                                            .getBytes(StandardCharsets.UTF_8)
                            )
                    )
                    .build()
                    .parseClaimsJws(token);

        } catch (ExpiredJwtException ex) {
            // Token expirado
            throw new JwtAuthenticationException(
                    "Token expirado", HttpStatus.UNAUTHORIZED, ex
            );

        } catch (UnsupportedJwtException ex) {
            // Tipo de JWT no soportado
            throw new JwtAuthenticationException(
                    "Token no soportado", HttpStatus.UNAUTHORIZED, ex
            );

        } catch (MalformedJwtException ex) {
            // Token mal formado
            throw new JwtAuthenticationException(
                    "Token mal formado", HttpStatus.UNAUTHORIZED, ex
            );

        } catch (SignatureException ex) {
            // Firma inválida
            throw new JwtAuthenticationException(
                    "Firma del token inválida", HttpStatus.UNAUTHORIZED, ex
            );

        } catch (IllegalArgumentException ex) {
            // Token vacío o null
            throw new JwtAuthenticationException(
                    "Token inválido", HttpStatus.UNAUTHORIZED, ex
            );

        } catch (JwtException ex) {
            // Cualquier otro error JWT
            throw new JwtAuthenticationException(
                    "Error de autenticación", HttpStatus.UNAUTHORIZED, ex
            );

        } catch (Exception ex) {
            throw new JwtAuthenticationException(
                    "Error interno al validar el token",
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    ex
            );
        }
    }
}
