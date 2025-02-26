package com.cantomiletea.chavez.review.dto;

public record ReviewResponseDto(
        String slug,
        String title,
        String altTitle,
        String author,
        ReviewRating rating,
        String comment
) { }
