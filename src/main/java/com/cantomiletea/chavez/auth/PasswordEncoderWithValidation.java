package com.cantomiletea.chavez.auth;

import jakarta.validation.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class PasswordEncoderWithValidation implements PasswordEncoder {

    private final BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();


    @Override
    public String encode(CharSequence rawPassword) {
        validatePassword(rawPassword.toString());
        return bCryptPasswordEncoder.encode(rawPassword);
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        return bCryptPasswordEncoder.matches(rawPassword, encodedPassword);
    }

    private void validatePassword(String password) {
        PasswordValidator passwordValidator = new PasswordValidator(password);
        var violations = validator.validate(passwordValidator);

        if (!violations.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (var v : violations) {
                sb.append(v.getMessage()).append(" ");
            }
            throw new ConstraintViolationException(sb.toString().trim(), violations);
        }
    }

    @AllArgsConstructor
    public static class PasswordValidator {
        @NotNull
        @Size(min = 8, message = "Password must be at least 8 characters long")
        private final String password;
    }
}
