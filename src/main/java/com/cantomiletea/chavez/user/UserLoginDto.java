package com.cantomiletea.chavez.user;

import jakarta.validation.constraints.NotEmpty;

public record UserLoginDto (
        @NotEmpty(message = "Username must not be empty")
        String username,

        @NotEmpty(message = "Password must not be empty")
        String password
){ }
