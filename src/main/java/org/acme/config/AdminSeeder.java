package org.acme.config;

import at.favre.lib.crypto.bcrypt.BCrypt;
import io.quarkus.runtime.StartupEvent;
import org.acme.entity.User;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class AdminSeeder {

    @Transactional
    void onStart(@Observes StartupEvent event) {
        if (User.findByUsername("admin") == null) {
            User admin = new User();
            admin.username = "admin";
            admin.passwordHash = BCrypt.withDefaults().hashToString(12, "admin123".toCharArray());
            admin.name = "Administrator";
            admin.role = "admin";
            admin.persist();
        }
        if (User.findByUsername("customer") == null) {
            User customer = new User();
            customer.username = "customer";
            customer.passwordHash = BCrypt.withDefaults().hashToString(12, "customer123".toCharArray());
            customer.name = "Customer Demo";
            customer.role = "user";
            customer.persist();
        }
    }
}
