package org.acme.dto;

import java.util.Set;

public record MeResponseDto(
        String username,
        String name,
        String role,
        Set<String> groups
) {
}
