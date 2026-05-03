package com.nisum.userapi.infrastructure.output.adapter.repository;

import com.nisum.userapi.application.port.out.UserCustomRepository;
import com.nisum.userapi.domain.Phone;
import com.nisum.userapi.domain.User;
import io.r2dbc.spi.Row;
import lombok.RequiredArgsConstructor;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class UserCustomRepositoryAdapter implements UserCustomRepository {
    private final DatabaseClient databaseClient;

    @Override
    public Flux<User> findUsersWithPhonesPaged(int page, int limit) {
        int offset = page * limit;

        return databaseClient.sql("""
                            SELECT 
                                u.id        AS user_id,
                                u.name      AS name,
                                u.email     AS email,
                                u.password  AS password,
                                u.created   AS created,
                                u.modified  AS modified,
                                u.last_login AS last_login,
                                u.token     AS token,
                                u.active    AS active,
                                p.id        AS phone_id,
                                p.number    AS number,
                                p.city_code AS city_code,
                                p.contry_code AS contry_code
                            FROM users u
                            LEFT JOIN phones p ON u.id = p.user_id
                            ORDER BY u.id
                            LIMIT :limit OFFSET :offset
                        """)
                .bind("limit", limit)
                .bind("offset", offset)
                .map((row, meta) -> row)
                .all()
                .collectMultimap(r -> r.get("user_id", UUID.class))
                .flatMapMany(map -> Flux.fromIterable(map.entrySet()))
                .map(entry -> {
                    UUID userId = entry.getKey();
                    List<Row> rows = entry.getValue().stream().toList();
                    return mapToUser(rows);
                });
    }

    @Override
    public Mono<User> findByIdWithPhones(UUID id) {
        return databaseClient.sql("""
                            SELECT 
                                u.id        AS user_id,
                                u.name      AS name,
                                u.email     AS email,
                                u.password  AS password,
                                u.created   AS created,
                                u.modified  AS modified,
                                u.last_login AS last_login,
                                u.token     AS token,
                                u.active    AS active,
                                p.id        AS phone_id,
                                p.number    AS number,
                                p.city_code AS city_code,
                                p.contry_code AS contry_code
                            FROM users u
                            LEFT JOIN phones p ON u.id = p.user_id
                            WHERE u.id = :id
                        """)
                .bind("id", id)
                .map((row, meta) -> row) // devolvemos directamente Row
                .all()
                .collectList()
                .flatMap(rows -> {
                    if (rows.isEmpty()) {
                        return Mono.empty();
                    }
                    return Mono.just(mapToUser(rows));
                });
    }

    private User mapToUser(List<Row> rows) {
        if (rows.isEmpty()) return null;

        Row first = rows.get(0);
        UUID userId = first.get("user_id", UUID.class);

        User user = new User();
        user.setId(userId);
        user.setName(first.get("name", String.class));
        user.setEmail(first.get("email", String.class));
        user.setPassword(first.get("password", String.class));
        user.setCreated(first.get("created", LocalDateTime.class));
        user.setModified(first.get("modified", LocalDateTime.class));
        user.setLastLogin(first.get("last_login", LocalDateTime.class));
        user.setToken(first.get("token", String.class));
        user.setActive(Boolean.TRUE.equals(first.get("active", Boolean.class)));

        List<Phone> phones = new ArrayList<>();
        for (Row r : rows) {
            UUID phoneId = r.get("phone_id", UUID.class);
            if (phoneId != null) {
                Phone p = new Phone();
                p.setId(phoneId);
                p.setNumber(r.get("number", String.class));
                p.setCitycode(r.get("city_code", String.class));
                p.setContrycode(r.get("contry_code", String.class));
                p.setUserId(userId);
                phones.add(p);
            }
        }
        user.setPhones(phones);
        return user;
    }

}
