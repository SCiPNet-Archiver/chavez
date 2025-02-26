package com.cantomiletea.chavez.review.dto;

import jakarta.validation.constraints.NotEmpty;

public record ArticleDto(
        @NotEmpty
        String slug,

        @NotEmpty
        String title,

        @NotEmpty
        String author,

        ScpInfoDto scpInfo
) { }
