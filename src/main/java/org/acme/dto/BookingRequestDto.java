package org.acme.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record BookingRequestDto(
        @NotNull(message = "roomId wajib diisi")
        Long roomId,
        @NotNull(message = "checkInDate wajib diisi")
        @FutureOrPresent(message = "checkInDate tidak boleh tanggal lalu")
        LocalDate checkInDate,
        @NotNull(message = "checkOutDate wajib diisi")
        @FutureOrPresent(message = "checkOutDate tidak boleh tanggal lalu")
        LocalDate checkOutDate
) {
}
