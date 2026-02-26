package org.acme.config;

import at.favre.lib.crypto.bcrypt.BCrypt;
import io.quarkus.runtime.StartupEvent;
import org.acme.entity.Hotel;
import org.acme.entity.Room;
import org.acme.entity.User;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class AdminSeeder {

    @Transactional
    void onStart(@Observes StartupEvent event) {
        seedUsers();
        seedHotels();
    }

    private void seedUsers() {
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

    private void seedHotels() {
        if (Hotel.count() > 0) return;
        Hotel h1 = new Hotel();
        h1.name = "Hotel Santai";
        h1.location = "Jakarta";
        h1.persist();
        Hotel h2 = new Hotel();
        h2.name = "Hotel Mewah";
        h2.location = "Bandung";
        h2.persist();
        Hotel h3 = new Hotel();
        h3.name = "Hotel Medan";
        h3.location = "Medan";
        h3.persist();
        addRoom(h1, "101", "STANDARD", 500000);
        addRoom(h1, "102", "DELUXE", 800000);
        addRoom(h2, "201", "SUITE", 1200000);
    }

    private void addRoom(Hotel hotel, String roomNumber, String type, double price) {
        Room r = new Room();
        r.roomNumber = roomNumber;
        r.type = type;
        r.price = price;
        r.hotel = hotel;
        r.persist();
    }
}
