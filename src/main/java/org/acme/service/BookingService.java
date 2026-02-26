package org.acme.service;

import org.acme.entity.Booking;
import org.acme.entity.Room;
import org.acme.repository.BookingRepository;
import org.acme.repository.RoomRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class BookingService {

    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;
    private final EntityManager entityManager;

    public BookingService(BookingRepository bookingRepository, RoomRepository roomRepository,
                          EntityManager entityManager) {
        this.bookingRepository = bookingRepository;
        this.roomRepository = roomRepository;
        this.entityManager = entityManager;
    }

    @Transactional
    public Booking createBooking(UUID roomId, String customerName,
                                 LocalDate checkIn, LocalDate checkOut) {
        Room room = entityManager.find(Room.class, roomId, LockModeType.PESSIMISTIC_WRITE);
        if (room == null) {
            throw new WebApplicationException("Room not found", 404);
        }
        if (checkOut.isBefore(checkIn) || checkOut.equals(checkIn)) {
            throw new WebApplicationException("checkOutDate harus setelah checkInDate", 400);
        }

        List<Booking> conflicts =
                bookingRepository.findActiveBookings(roomId, checkIn, checkOut);

        if (!conflicts.isEmpty()) {
            throw new WebApplicationException("Room already booked for selected dates", 400);
        }

        Booking booking = new Booking();
        booking.room = room;
        booking.customerName = customerName;
        booking.checkInDate = checkIn;
        booking.checkOutDate = checkOut;
        booking.status = "BOOKED";

        bookingRepository.persist(booking);

        return booking;
    }

    @Transactional
    public Booking cancelBooking(UUID bookingId) {
        Booking booking = bookingRepository.findById(bookingId);
        if (booking == null) {
            throw new WebApplicationException("Booking not found", 404);
        }

        booking.status = "CANCELLED";
        return booking;
    }
}