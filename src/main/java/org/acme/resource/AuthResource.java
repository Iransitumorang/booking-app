package org.acme.resource;

import org.acme.dto.LoginRequestDto;
import org.acme.dto.LoginResponseDto;
import org.acme.dto.MeResponseDto;
import org.acme.dto.RegisterRequestDto;
import org.acme.entity.User;
import org.acme.service.AuthService;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.util.Set;

@Path("/auth")
@Tag(name = "Auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {

    private final AuthService authService;

    @Inject
    JsonWebToken jwt;

    public AuthResource(AuthService authService) {
        this.authService = authService;
    }

    @GET
    @Path("/me")
    @RolesAllowed({"User", "Admin"})
    public MeResponseDto me() {
        User user = User.findByUsername(jwt.getName());
        Set<String> groups = jwt.getGroups() != null ? jwt.getGroups() : Set.of();
        if (user == null) {
            return new MeResponseDto(jwt.getName(), (String) jwt.getClaim("name"), null, groups);
        }
        return new MeResponseDto(user.username, user.name, user.role, groups);
    }

    @POST
    @Path("/login")
    @PermitAll
    public LoginResponseDto login(@Valid LoginRequestDto request) {
        String token = authService.login(request.username(), request.password());
        User user = User.findByUsername(request.username());
        return new LoginResponseDto(token, user.username, user.name, user.role);
    }

    @POST
    @Path("/register")
    @PermitAll
    public LoginResponseDto register(@Valid RegisterRequestDto request) {
        User user = authService.register(request.username(), request.password(), request.name());
        String token = authService.generateToken(user);
        return new LoginResponseDto(token, user.username, user.name, user.role);
    }
}
