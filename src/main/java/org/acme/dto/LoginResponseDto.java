package org.acme.dto;

public record LoginResponseDto(
        String token,
        String username,
        String name,
        String role
) {
}
