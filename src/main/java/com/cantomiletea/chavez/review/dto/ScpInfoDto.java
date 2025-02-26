package com.cantomiletea.chavez.review.dto;

public record ScpInfoDto(
        String itemNumber,
        String objectClass,
        ScpAcsDto acs
) { }
