package org.acme.repository;

import org.acme.entity.Hotel;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class HotelRepository implements PanacheRepository<Hotel> {
}