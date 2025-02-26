package com.cantomiletea.chavez.review.dto;

public record ScpAcsDto(
        String containmentClass,
        String riskClass,
        String disruptionClass,
        String secondaryClass
) { }
