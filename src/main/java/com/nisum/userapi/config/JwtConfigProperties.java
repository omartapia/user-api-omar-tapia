package com.nisum.userapi.config;


import lombok.Data;
import lombok.NonNull;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "nisum.jwt")
@Data
public class JwtConfigProperties {
    @NonNull
    private String secret;
    @NonNull
    private Integer ttl;
}
