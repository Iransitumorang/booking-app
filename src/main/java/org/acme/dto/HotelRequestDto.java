package org.acme.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record HotelRequestDto(
        @NotNull(message = "name wajib diisi")
        @Size(min = 1, max = 100)
        String name,
        @NotNull(message = "location wajib diisi")
        @Size(min = 1, max = 200)
        String location
) {
}
