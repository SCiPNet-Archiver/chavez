package com.cantomiletea.chavez.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;

public record UserRegistrationDto (
        @NotEmpty(message = "User Name must not be empty")
        String username,

        @NotEmpty(message = "User email must not be empty") //Neither null nor 0 size
        @Email(message = "Invalid email format")
        String email,

        @NotEmpty(message = "User password must not be empty")
        String password
){ }
