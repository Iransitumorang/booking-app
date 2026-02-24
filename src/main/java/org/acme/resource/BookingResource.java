package org.acme.resource;

import io.quarkus.panache.common.Page;
import org.acme.dto.BookingRequestDto;
import org.acme.dto.PageResponse;
import org.acme.entity.Booking;
import org.acme.repository.BookingRepository;
import org.acme.service.BookingService;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

@Path("/bookings")
@Tag(name = "Bookings")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BookingResource {

    private final BookingService bookingService;
    private final BookingRepository bookingRepository;

    public BookingResource(BookingService bookingService, BookingRepository bookingRepository) {
        this.bookingService = bookingService;
        this.bookingRepository = bookingRepository;
    }

    @GET
    public PageResponse<Booking> getBookings(
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size,
            @QueryParam("customerName") String customerName) {
        Page p = Page.of(page, size);
        List<Booking> content = customerName != null && !customerName.isBlank()
                ? bookingRepository.find("customerName", customerName).page(p).list()
                : bookingRepository.findAll(p);
        long total = customerName != null && !customerName.isBlank()
                ? bookingRepository.count("customerName", customerName)
                : bookingRepository.count();
        return new PageResponse<>(content, page, size, total, (int) Math.ceil((double) total / size));
    }

    @GET
    @Path("/{id}")
    public Booking getBooking(@PathParam("id") Long id) {
        Booking booking = bookingRepository.findById(id);
        if (booking == null) {
            throw new WebApplicationException("Booking not found", Response.Status.NOT_FOUND);
        }
        return booking;
    }

    @POST
    public Booking createBooking(@Valid BookingRequestDto request) {
        return bookingService.createBooking(
                request.roomId(),
                request.customerName(),
                request.checkInDate(),
                request.checkOutDate()
        );
    }

    @PUT
    @Path("/{id}/cancel")
    public Booking cancelBooking(@PathParam("id") Long id) {
        return bookingService.cancelBooking(id);
    }
}