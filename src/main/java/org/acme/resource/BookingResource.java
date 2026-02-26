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
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

import java.util.List;
import java.util.UUID;

@Path("/bookings")
@Tag(name = "Bookings")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed({"User", "Admin"})
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

    private boolean isAdmin() {
        return securityContext.isUserInRole("Admin");
    }

    @GET
    @Transactional
    public PageResponse<Booking> getBookings(
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size,
            @QueryParam("customerName") String customerName) {
        String username = jwt.getName();
        boolean isAdmin = isAdmin();
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
    @Transactional
    public Booking getBooking(@PathParam("id") UUID id) {
        Booking booking = bookingRepository.findById(id);
        if (booking == null) {
            throw new WebApplicationException("Booking not found", Response.Status.NOT_FOUND);
        }
        if (!isAdmin() && !booking.customerName.equals(jwt.getName())) {
            throw new WebApplicationException("Forbidden", Response.Status.FORBIDDEN);
        }
        return booking;
    }

    @POST
    @RolesAllowed({"User", "Admin"})
    public Booking createBooking(@Valid BookingRequestDto request) {
        String customerName = request.customerName();
        if (isAdmin() && customerName != null && !customerName.isBlank()) {
            customerName = customerName.trim();
        } else {
            customerName = jwt.getName();
        }
        return bookingService.createBooking(
                request.roomId(),
                customerName,
                request.checkInDate(),
                request.checkOutDate()
        );
    }

    @PUT
    @Path("/{id}/cancel")
    public Booking cancelBooking(@PathParam("id") UUID id) {
        Booking booking = bookingRepository.findById(id);
        if (booking == null) {
            throw new WebApplicationException("Booking not found", Response.Status.NOT_FOUND);
        }
        if (!isAdmin() && !booking.customerName.equals(jwt.getName())) {
            throw new WebApplicationException("Hanya bisa membatalkan booking sendiri", Response.Status.FORBIDDEN);
        }
        return bookingService.cancelBooking(id);
    }
}