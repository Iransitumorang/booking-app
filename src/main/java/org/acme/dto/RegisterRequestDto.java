package org.acme.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequestDto(
        @NotBlank(message = "username wajib diisi")
        @Size(min = 3, max = 50)
        String username,
        @NotBlank(message = "password wajib diisi")
        @Size(min = 6, message = "password minimal 6 karakter")
        String password,
        @NotBlank(message = "name wajib diisi")
        @Size(min = 1, max = 100)
        String name
) {
}
