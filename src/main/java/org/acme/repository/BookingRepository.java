package org.acme.repository;

import org.acme.entity.Booking;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class BookingRepository implements PanacheRepositoryBase<Booking, UUID> {

    public List<Booking> findActiveBookings(UUID roomId, LocalDate checkIn, LocalDate checkOut) {
        return list(
                "room.id = ?1 and status = 'BOOKED' and " +
                        "(checkInDate <= ?3 and checkOutDate >= ?2)",
                roomId, checkIn, checkOut
        );
    }

    public boolean isRoomAvailable(UUID roomId, LocalDate checkIn, LocalDate checkOut) {
        return findActiveBookings(roomId, checkIn, checkOut).isEmpty();
    }

    public List<Booking> findAll(io.quarkus.panache.common.Page page) {
        return findAll().page(page).list();
    }
}