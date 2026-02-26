package org.acme.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record RoomRequestDto(
        @NotNull(message = "roomNumber wajib diisi")
        @Size(min = 1, max = 20)
        String roomNumber,
        @NotNull(message = "type wajib diisi")
        @Size(min = 1, max = 50)
        String type,
        @NotNull(message = "price wajib diisi")
        @Min(value = 0, message = "price minimal 0")
        Double price,
        @NotNull(message = "hotelId wajib diisi")
        UUID hotelId
) {
}
