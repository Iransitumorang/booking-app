package org.acme.repository;

import org.acme.entity.Room;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class RoomRepository implements PanacheRepository<Room> {

    public List<Room> findByHotel(Long hotelId) {
        return list("hotel.id", hotelId);
    }
}