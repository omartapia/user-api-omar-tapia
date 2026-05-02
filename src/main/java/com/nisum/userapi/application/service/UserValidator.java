package com.nisum.userapi.application.service;

import com.nisum.userapi.config.UserValidationProperties;
import com.nisum.userapi.domain.User;
import com.nisum.userapi.exception.UserApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserValidator {
    private final UserValidationProperties properties;

    public void validateForCreate(User user) {
        validateEmail(user.getEmail());
        validatePassword(user.getPassword());
    }

    public void validateForUpdate(User user) {
        validateEmail(user.getEmail());
        validatePassword(user.getPassword());
    }

    public void validateForPatch(User user) {
        if (user.getEmail() != null) {
            validateEmail(user.getEmail());
        }

        if (user.getPassword() != null) {
            validatePassword(user.getPassword());
        }
    }

    private void validateEmail(String email) {
        if (email == null || !email.matches(properties.getEmailRegex())) {
            throw new UserApiException("Formato de correo inválido", HttpStatus.BAD_REQUEST);
        }
    }

    private void validatePassword(String password) {
        if (password == null || !password.matches(properties.getPasswordRegex())) {
            throw new UserApiException("Formato de contraseña inválido", HttpStatus.BAD_REQUEST);
        }
    }
}
