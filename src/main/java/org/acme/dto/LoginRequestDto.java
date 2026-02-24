package org.acme.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequestDto(
        @NotBlank(message = "username wajib diisi")
        String username,
        @NotBlank(message = "password wajib diisi")
        String password
) {
}
