package com.nisum.userapi.service;

public interface IJwtService {
    String generate(String subject);
    void validate(String token);
}
