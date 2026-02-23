package org.acme.resource;

import org.acme.entity.Booking;
import org.acme.service.BookingService;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import java.time.LocalDate;

@Path("/bookings")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BookingResource {

    private final BookingService bookingService;

    public BookingResource(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @POST
    public Booking createBooking(BookingRequest request) {
        return bookingService.createBooking(
                request.roomId,
                request.customerName,
                request.checkInDate,
                request.checkOutDate
        );
    }

    @PUT
    @Path("/{id}/cancel")
    public Booking cancelBooking(@PathParam("id") Long id) {
        return bookingService.cancelBooking(id);
    }

    public static class BookingRequest {
        public Long roomId;
        public String customerName;
        public LocalDate checkInDate;
        public LocalDate checkOutDate;
    }
}