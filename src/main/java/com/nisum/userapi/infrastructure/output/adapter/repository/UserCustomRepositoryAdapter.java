package com.nisum.userapi.infrastructure.output.adapter.repository;

import com.nisum.userapi.application.port.out.UserCustomRepository;
import com.nisum.userapi.domain.Phone;
import com.nisum.userapi.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
                // Agrupamos por user_id
                .collectMultimap(r -> r.get("user_id", UUID.class))
                .flatMapMany(map -> Flux.fromIterable(map.entrySet()))
                .map(entry -> {
                    UUID userId = entry.getKey();
                    List<Map<String, Object>> rows = entry.getValue().stream()
                            .map(r -> (Map<String, Object>) r)
                            .toList();

                    var first = rows.get(0);

                    User user = new User();
                    user.setId(userId);
                    user.setName((String) first.get("name"));
                    user.setEmail((String) first.get("email"));
                    user.setPassword((String) first.get("password"));
                    user.setCreated((LocalDateTime) first.get("created"));
                    user.setModified((LocalDateTime) first.get("modified"));
                    user.setLastLogin((LocalDateTime) first.get("last_login"));
                    user.setToken((String) first.get("token"));
                    user.setActive(Boolean.TRUE.equals(first.get("active")));

                    List<Phone> phones = new ArrayList<>();
                    for (var r : rows) {
                        UUID phoneId = (UUID) r.get("phone_id");
                        if (phoneId != null) {
                            Phone p = new Phone();
                            p.setId(phoneId);
                            p.setNumber((String) r.get("number"));
                            p.setCitycode((String) r.get("city_code"));
                            p.setContrycode((String) r.get("contry_code"));
                            p.setUserId(userId);
                            phones.add(p);
                        }
                    }
                    user.setPhones(phones);
                    return user;
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
                .map((row, meta) -> row)
                .all()
                .collectList()
                .flatMap(rows -> {
                    if (rows.isEmpty()) {
                        return Mono.empty();
                    }

                    Map<String, Object> first = (Map<String, Object>) rows.get(0);
                    UUID userId = (UUID) first.get("user_id");

                    User user = new User();
                    user.setId(userId);
                    user.setName((String) first.get("name"));
                    user.setEmail((String) first.get("email"));
                    user.setPassword((String) first.get("password"));
                    user.setCreated((LocalDateTime) first.get("created"));
                    user.setModified((LocalDateTime) first.get("modified"));
                    user.setLastLogin((LocalDateTime) first.get("last_login"));
                    user.setToken((String) first.get("token"));
                    user.setActive(Boolean.TRUE.equals(first.get("active")));

                    List<Phone> phones = new ArrayList<>();
                    for (var r : rows) {
                        UUID phoneId = (UUID) r.get("phone_id");
                        if (phoneId != null) {
                            Phone p = new Phone();
                            p.setId(phoneId);
                            p.setNumber((String) r.get("number"));
                            p.setCitycode((String) r.get("city_code"));
                            p.setContrycode((String) r.get("contry_code"));
                            p.setUserId(userId);
                            phones.add(p);
                        }
                    }
                    user.setPhones(phones);
                    return Mono.just(user);
                });
    }
}
