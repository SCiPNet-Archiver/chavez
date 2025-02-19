package com.cantomiletea.chavez.user;

import jakarta.validation.constraints.NotEmpty;

public record UserEditDto(
        @NotEmpty(message = "Username must not be empty")
        String username,

        @NotEmpty(message = "Email must not be empty")
        String email,

        String bio,

        String pfpUrl
){ }
