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
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

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
    public PageResponse<Hotel> getHotels(
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size) {
        Page p = Page.of(page, size);
        List<Hotel> content = hotelRepository.findAll(p);
        long total = hotelRepository.count();
        return new PageResponse<>(content, page, size, total, (int) Math.ceil((double) total / size));
    }

    @GET
    @Path("/{id}")
    public Hotel getHotel(@PathParam("id") Long id) {
        Hotel hotel = hotelRepository.findById(id);
        if (hotel == null) {
            throw new WebApplicationException("Hotel not found", Response.Status.NOT_FOUND);
        }
        return hotel;
    }

    @POST
    public Hotel createHotel(@Valid HotelRequestDto dto) {
        Hotel hotel = new Hotel();
        hotel.name = dto.name();
        hotel.location = dto.location();
        hotelRepository.persist(hotel);
        return hotel;
    }

    @PUT
    @Path("/{id}")
    public Hotel updateHotel(@PathParam("id") Long id, @Valid HotelRequestDto dto) {
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
    public void deleteHotel(@PathParam("id") Long id) {
        if (!hotelRepository.deleteById(id)) {
            throw new WebApplicationException("Hotel not found", Response.Status.NOT_FOUND);
        }
    }

    @GET
    @Path("/{hotelId}/rooms")
    public PageResponse<Room> getRoomsByHotel(
            @PathParam("hotelId") Long hotelId,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size) {
        Page p = Page.of(page, size);
        List<Room> content = roomRepository.findByHotel(hotelId, p);
        long total = roomRepository.countByHotel(hotelId);
        return new PageResponse<>(content, page, size, total, (int) Math.ceil((double) total / size));
    }
}