package org.acme.resource;

import io.quarkus.panache.common.Page;
import org.acme.dto.BookingRequestDto;
import org.acme.dto.PageResponse;
import org.acme.entity.Booking;
import org.acme.repository.BookingRepository;
import org.acme.service.BookingService;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import java.util.List;

@Path("/bookings")
@Tag(name = "Bookings")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed({"user", "admin"})
public class BookingResource {

    private final BookingService bookingService;
    private final BookingRepository bookingRepository;

    @Inject
    JsonWebToken jwt;

    @Inject
    SecurityContext securityContext;

    public BookingResource(BookingService bookingService, BookingRepository bookingRepository) {
        this.bookingService = bookingService;
        this.bookingRepository = bookingRepository;
    }

    @GET
    public PageResponse<Booking> getBookings(
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size,
            @QueryParam("customerName") String customerName) {
        String username = jwt.getName();
        boolean isAdmin = securityContext.isUserInRole("admin");
        String filterBy = isAdmin ? (customerName != null && !customerName.isBlank() ? customerName : null) : username;

        Page p = Page.of(page, size);
        List<Booking> content = filterBy != null
                ? bookingRepository.find("customerName", filterBy).page(p).list()
                : bookingRepository.findAll(p);
        long total = filterBy != null
                ? bookingRepository.count("customerName", filterBy)
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
        if (!securityContext.isUserInRole("admin") && !booking.customerName.equals(jwt.getName())) {
            throw new WebApplicationException("Forbidden", Response.Status.FORBIDDEN);
        }
        return booking;
    }

    @POST
    @RolesAllowed("user")
    public Booking createBooking(@Valid BookingRequestDto request) {
        return bookingService.createBooking(
                request.roomId(),
                jwt.getName(),
                request.checkInDate(),
                request.checkOutDate()
        );
    }

    @PUT
    @Path("/{id}/cancel")
    public Booking cancelBooking(@PathParam("id") Long id) {
        Booking booking = bookingRepository.findById(id);
        if (booking == null) {
            throw new WebApplicationException("Booking not found", Response.Status.NOT_FOUND);
        }
        if (!securityContext.isUserInRole("admin") && !booking.customerName.equals(jwt.getName())) {
            throw new WebApplicationException("Hanya bisa membatalkan booking sendiri", Response.Status.FORBIDDEN);
        }
        return bookingService.cancelBooking(id);
    }
}