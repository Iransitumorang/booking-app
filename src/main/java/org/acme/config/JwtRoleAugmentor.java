package org.acme.config;

import io.quarkus.security.identity.AuthenticationRequestContext;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.identity.SecurityIdentityAugmentor;
import io.quarkus.security.runtime.QuarkusSecurityIdentity;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.HashSet;
import java.util.Set;

@ApplicationScoped
public class JwtRoleAugmentor implements SecurityIdentityAugmentor {

    @Override
    public Uni<SecurityIdentity> augment(SecurityIdentity identity, AuthenticationRequestContext context) {
        Set<String> roles = new HashSet<>(identity.getRoles());
        for (String role : Set.copyOf(roles)) {
            if (role != null && !role.isEmpty()) {
                roles.add(role.toLowerCase());
                if (role.length() > 1) {
                    roles.add(role.substring(0, 1).toUpperCase() + role.substring(1).toLowerCase());
                }
            }
        }
        return Uni.createFrom().item(QuarkusSecurityIdentity.builder(identity).addRoles(roles).build());
    }
}
