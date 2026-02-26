package org.acme.repository;

import io.quarkus.panache.common.Page;
import org.acme.entity.Room;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class RoomRepository implements PanacheRepositoryBase<Room, UUID> {

    public List<Room> findByHotel(UUID hotelId) {
        return list("hotel.id", hotelId);
    }

    public List<Room> findByHotel(UUID hotelId, Page page) {
        return find("hotel.id", hotelId).page(page).list();
    }

    public long countByHotel(UUID hotelId) {
        return count("hotel.id", hotelId);
    }
}