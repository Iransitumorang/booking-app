package org.acme.resource;

import io.quarkus.panache.common.Page;
import org.acme.dto.PageResponse;
import org.acme.entity.Hotel;
import org.acme.entity.Room;
import org.acme.repository.HotelRepository;
import org.acme.repository.RoomRepository;
import org.acme.dto.HotelRequestDto;
import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.UUID;

@OpenAPIDefinition(info = @Info(title = "Hotel Booking API", version = "1.0"))
@Path("/hotels")
@Tag(name = "Hotels")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class HotelResource {

    private final HotelRepository hotelRepository;
    private final RoomRepository roomRepository;

    public HotelResource(HotelRepository hotelRepository, RoomRepository roomRepository) {
        this.hotelRepository = hotelRepository;
        this.roomRepository = roomRepository;
    }

    @GET
    @PermitAll
    @Transactional
    public PageResponse<Hotel> getHotels(
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size) {
        int safeSize = size <= 0 ? 20 : size;
        Page p = Page.of(page, safeSize);
        List<Hotel> content = hotelRepository.findAll(p);
        long total = hotelRepository.count();
        return new PageResponse<>(content, page, safeSize, total, (int) Math.ceil((double) total / safeSize));
    }

    @GET
    @Path("/{id}")
    @PermitAll
    public Hotel getHotel(@PathParam("id") UUID id) {
        Hotel hotel = hotelRepository.findById(id);
        if (hotel == null) {
            throw new WebApplicationException("Hotel not found", Response.Status.NOT_FOUND);
        }
        return hotel;
    }

    @POST
    @RolesAllowed("Admin")
    @Transactional
    public Hotel createHotel(@Valid HotelRequestDto dto) {
        Hotel hotel = new Hotel();
        hotel.name = dto.name();
        hotel.location = dto.location();
        hotelRepository.persist(hotel);
        return hotel;
    }

    @PUT
    @Path("/{id}")
    @RolesAllowed("Admin")
    @Transactional
    public Hotel updateHotel(@PathParam("id") UUID id, @Valid HotelRequestDto dto) {
        Hotel hotel = hotelRepository.findById(id);
        if (hotel == null) {
            throw new WebApplicationException("Hotel not found", Response.Status.NOT_FOUND);
        }
        hotel.name = dto.name();
        hotel.location = dto.location();
        return hotel;
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed("Admin")
    @Transactional
    public void deleteHotel(@PathParam("id") UUID id) {
        if (!hotelRepository.deleteById(id)) {
            throw new WebApplicationException("Hotel not found", Response.Status.NOT_FOUND);
        }
    }

    @GET
    @Path("/{hotelId}/rooms")
    @PermitAll
    @Transactional
    public PageResponse<Room> getRoomsByHotel(
            @PathParam("hotelId") UUID hotelId,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size) {
        Page p = Page.of(page, size);
        List<Room> content = roomRepository.findByHotel(hotelId, p);
        long total = roomRepository.countByHotel(hotelId);
        return new PageResponse<>(content, page, size, total, (int) Math.ceil((double) total / size));
    }
}