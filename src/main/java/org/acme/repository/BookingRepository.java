package org.acme.repository;

import org.acme.entity.Booking;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.LocalDate;
import java.util.List;

@ApplicationScoped
public class BookingRepository implements PanacheRepository<Booking> {

    public List<Booking> findActiveBookings(Long roomId, LocalDate checkIn, LocalDate checkOut) {
        return list(
                "room.id = ?1 and status = 'BOOKED' and " +
                        "(checkInDate <= ?3 and checkOutDate >= ?2)",
                roomId, checkIn, checkOut
        );
    }
}