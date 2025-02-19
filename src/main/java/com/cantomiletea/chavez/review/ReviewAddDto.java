package com.cantomiletea.chavez.review;

import jakarta.validation.constraints.NotEmpty;

public record ReviewAddDto(
        @NotEmpty(message = "Username must not be empty")
        String username,

        @NotEmpty(message = "Article slug must not be empty")
        String slug,

        ReviewRating rating,

        String comment
) { }
