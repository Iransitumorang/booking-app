package org.acme.repository;

import io.quarkus.panache.common.Page;
import org.acme.entity.Room;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class RoomRepository implements PanacheRepository<Room> {

    public List<Room> findByHotel(Long hotelId) {
        return list("hotel.id", hotelId);
    }

    public List<Room> findByHotel(Long hotelId, Page page) {
        return find("hotel.id", hotelId).page(page).list();
    }

    public long countByHotel(Long hotelId) {
        return count("hotel.id", hotelId);
    }
}