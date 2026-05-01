package com.nisum.userapi.infrastructure.adapter;

import com.nisum.userapi.application.port.out.JwtPort;
import com.nisum.userapi.config.JwtConfigProperties;
import com.nisum.userapi.exception.JwtAuthenticationException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtAdapter implements JwtPort {
    private final JwtConfigProperties jwtConfigProperties;

    @Override
    public String generate(String subject) {
        return Jwts.builder().setSubject(subject)
                .setIssuedAt(new Date()).setExpiration(new Date(System.currentTimeMillis() + jwtConfigProperties.getTtl()))
                .signWith(Keys.hmacShaKeyFor(jwtConfigProperties.getSecret().getBytes(StandardCharsets.UTF_8)), SignatureAlgorithm.HS256)
                .compact();
    }

    @Override
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
            throw new JwtAuthenticationException(
                    "Token expirado", HttpStatus.UNAUTHORIZED, ex
            );

        } catch (UnsupportedJwtException ex) {
            throw new JwtAuthenticationException(
                    "Token no soportado", HttpStatus.UNAUTHORIZED, ex
            );

        } catch (MalformedJwtException ex) {
            throw new JwtAuthenticationException(
                    "Token mal formado", HttpStatus.UNAUTHORIZED, ex
            );

        } catch (SignatureException ex) {
            throw new JwtAuthenticationException(
                    "Firma del token inválida", HttpStatus.UNAUTHORIZED, ex
            );

        } catch (IllegalArgumentException ex) {
            throw new JwtAuthenticationException(
                    "Token inválido", HttpStatus.UNAUTHORIZED, ex
            );

        } catch (JwtException ex) {
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
