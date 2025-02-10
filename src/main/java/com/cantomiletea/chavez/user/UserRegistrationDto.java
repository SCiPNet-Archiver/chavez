package com.cantomiletea.chavez.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;

public record UserRegistrationDto (
        @NotEmpty(message = "Username must not be empty")
        String username,

        @NotEmpty(message = "Email must not be empty") //Neither null nor 0 size
        @Email(message = "Invalid email format")
        String email,

        @NotEmpty(message = "Password must not be empty")
        String password
){ }
