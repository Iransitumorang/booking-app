package org.acme.service;

import org.acme.entity.Booking;
import org.acme.entity.Room;
import org.acme.repository.BookingRepository;
import org.acme.repository.RoomRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import java.time.LocalDate;
import java.util.List;

@ApplicationScoped
public class BookingService {

    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;

    public BookingService(BookingRepository bookingRepository, RoomRepository roomRepository) {
        this.bookingRepository = bookingRepository;
        this.roomRepository = roomRepository;
    }

    @Transactional
    public Booking createBooking(Long roomId, String customerName,
                                 LocalDate checkIn, LocalDate checkOut) {

        Room room = roomRepository.findById(roomId);
        if (room == null) {
            throw new WebApplicationException("Room not found", 404);
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
    public Booking cancelBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId);
        if (booking == null) {
            throw new WebApplicationException("Booking not found", 404);
        }

        booking.status = "CANCELLED";
        return booking;
    }
}