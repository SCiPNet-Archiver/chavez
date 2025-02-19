package com.cantomiletea.chavez.review.dto;

import jakarta.validation.constraints.NotEmpty;

public record ReviewDeleteDto (
        @NotEmpty(message = "Username must not be empty")
        String username,

        @NotEmpty(message = "Article slug must not be empty")
        String slug
) { }
