package org.acme.resource;

import org.acme.dto.LoginRequestDto;
import org.acme.dto.LoginResponseDto;
import org.acme.dto.RegisterRequestDto;
import org.acme.entity.User;
import org.acme.service.AuthService;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

@Path("/auth")
@Tag(name = "Auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@PermitAll
public class AuthResource {

    private final AuthService authService;

    public AuthResource(AuthService authService) {
        this.authService = authService;
    }

    @POST
    @Path("/login")
    public LoginResponseDto login(@Valid LoginRequestDto request) {
        String token = authService.login(request.username(), request.password());
        User user = User.findByUsername(request.username());
        return new LoginResponseDto(token, user.username, user.name, user.role);
    }

    @POST
    @Path("/register")
    public LoginResponseDto register(@Valid RegisterRequestDto request) {
        User user = authService.register(request.username(), request.password(), request.name());
        String token = authService.generateToken(user);
        return new LoginResponseDto(token, user.username, user.name, user.role);
    }
}
