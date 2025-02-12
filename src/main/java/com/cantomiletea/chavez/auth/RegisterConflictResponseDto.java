package com.cantomiletea.chavez.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterConflictResponseDto {
    private final String EMAIL = "email";
    private final String PASSWORD = "password";

    @JsonProperty("conflicting_field")
    private String conflictingField;
}
