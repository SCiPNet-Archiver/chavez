package com.cantomiletea.chavez.review.dto;


import jakarta.validation.constraints.NotEmpty;

public record ReviewArticleDto(
        @NotEmpty
        String title,

        @NotEmpty
        String author,

        ScpInfoDto scpInfo
) { }
