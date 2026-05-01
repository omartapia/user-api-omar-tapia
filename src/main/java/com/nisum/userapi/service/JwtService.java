package com.nisum.userapi.service;

public interface JwtService {
    String generate(String subject);
    void validate(String token);
}
