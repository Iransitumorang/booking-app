package org.acme.service;

import at.favre.lib.crypto.bcrypt.BCrypt;
import io.smallrye.jwt.build.Jwt;
import org.acme.entity.User;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import java.util.Arrays;
import java.util.HashSet;

@ApplicationScoped
public class AuthService {

    @ConfigProperty(name = "mp.jwt.verify.issuer", defaultValue = "hotel-booking")
    String issuer;

    @Transactional
    public User register(String username, String password, String name) {
        if (User.findByUsername(username) != null) {
            throw new WebApplicationException("Username sudah dipakai", Response.Status.BAD_REQUEST);
        }
        User user = new User();
        user.username = username;
        user.passwordHash = BCrypt.withDefaults().hashToString(12, password.toCharArray());
        user.name = name;
        user.role = "user";
        user.persist();
        return user;
    }

    public String login(String username, String password) {
        User user = User.findByUsername(username);
        if (user == null) {
            throw new WebApplicationException("Username atau password salah", Response.Status.UNAUTHORIZED);
        }
        if (!BCrypt.verifyer().verify(password.toCharArray(), user.passwordHash).verified) {
            throw new WebApplicationException("Username atau password salah", Response.Status.UNAUTHORIZED);
        }
        return generateToken(user);
    }

    private String toJwtGroup(String role) {
        if (role == null) return "User";
        return "admin".equalsIgnoreCase(role) ? "Admin" : "User";
    }

    public String generateToken(User user) {
        return Jwt.issuer(issuer)
                .upn(user.username)
                .groups(new HashSet<>(Arrays.asList(toJwtGroup(user.role != null ? user.role : "user"))))
                .claim("name", user.name)
                .claim("userId", user.id)
                .sign();
    }
}
