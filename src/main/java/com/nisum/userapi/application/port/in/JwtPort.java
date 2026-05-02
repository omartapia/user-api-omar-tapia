package com.nisum.userapi.application.port.in;

public interface JwtPort {
    String generate(String subject);
    void validate(String token);
}
