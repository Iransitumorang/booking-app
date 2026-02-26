package org.acme.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

public record BookingRequestDto(
        @NotNull(message = "roomId wajib diisi")
        UUID roomId,
        @NotNull(message = "checkInDate wajib diisi")
        @FutureOrPresent(message = "checkInDate tidak boleh tanggal lalu")
        LocalDate checkInDate,
        @NotNull(message = "checkOutDate wajib diisi")
        @FutureOrPresent(message = "checkOutDate tidak boleh tanggal lalu")
        LocalDate checkOutDate,
        String customerName
) {
}
