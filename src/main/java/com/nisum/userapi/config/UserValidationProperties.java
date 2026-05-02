package com.nisum.userapi.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "nisum.validation")
@Data
public class UserValidationProperties {
    private String emailRegex = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
    private String passwordRegex = "^(?=.*[A-Za-z])(?=.*\\d).{6,}$";
}
