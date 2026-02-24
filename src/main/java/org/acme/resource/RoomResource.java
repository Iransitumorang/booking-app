package org.acme.resource;

import io.quarkus.panache.common.Page;
import org.acme.dto.PageResponse;
import org.acme.dto.RoomRequestDto;
import org.acme.entity.Hotel;
import org.acme.entity.Room;
import org.acme.repository.BookingRepository;
import org.acme.repository.HotelRepository;
import org.acme.repository.RoomRepository;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Path("/rooms")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Rooms")
public class RoomResource {

    private final RoomRepository roomRepository;
    private final HotelRepository hotelRepository;
    private final BookingRepository bookingRepository;

    public RoomResource(RoomRepository roomRepository, HotelRepository hotelRepository,
                        BookingRepository bookingRepository) {
        this.roomRepository = roomRepository;
        this.hotelRepository = hotelRepository;
        this.bookingRepository = bookingRepository;
    }

    @GET
    @Path("/{id}")
    @PermitAll
    public Room getRoom(@PathParam("id") Long id) {
        Room room = roomRepository.findById(id);
        if (room == null) {
            throw new WebApplicationException("Room not found", Response.Status.NOT_FOUND);
        }
        return room;
    }

    @GET
    @PermitAll
    public PageResponse<Room> getAllRooms(
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size) {
        Page p = Page.of(page, size);
        List<Room> content = roomRepository.findAll().page(p).list();
        long total = roomRepository.count();
        return new PageResponse<>(content, page, size, total, (int) Math.ceil((double) total / size));
    }

    @PUT
    @Path("/{id}")
    @RolesAllowed("admin")
    public Room updateRoom(@PathParam("id") Long id, @Valid RoomRequestDto dto) {
        Room room = roomRepository.findById(id);
        if (room == null) {
            throw new WebApplicationException("Room not found", Response.Status.NOT_FOUND);
        }
        Hotel hotel = hotelRepository.findById(dto.hotelId());
        if (hotel == null) {
            throw new WebApplicationException("Hotel not found", Response.Status.NOT_FOUND);
        }
        room.roomNumber = dto.roomNumber();
        room.type = dto.type();
        room.price = dto.price();
        room.hotel = hotel;
        return room;
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed("admin")
    public void deleteRoom(@PathParam("id") Long id) {
        if (!roomRepository.deleteById(id)) {
            throw new WebApplicationException("Room not found", Response.Status.NOT_FOUND);
        }
    }

    @POST
    @RolesAllowed("admin")
    public Room addRoom(@Valid RoomRequestDto dto) {
        Hotel hotel = hotelRepository.findById(dto.hotelId());
        if (hotel == null) {
            throw new WebApplicationException("Hotel not found", Response.Status.NOT_FOUND);
        }
        Room room = new Room();
        room.roomNumber = dto.roomNumber();
        room.type = dto.type();
        room.price = dto.price();
        room.hotel = hotel;
        roomRepository.persist(room);
        return room;
    }

    @GET
    @Path("/{id}/availability")
    @PermitAll
    public Map<String, Boolean> checkAvailability(
            @PathParam("id") Long roomId,
            @QueryParam("checkIn") LocalDate checkIn,
            @QueryParam("checkOut") LocalDate checkOut) {
        if (checkIn == null || checkOut == null) {
            throw new WebApplicationException("checkIn dan checkOut wajib diisi", Response.Status.BAD_REQUEST);
        }
        boolean available = bookingRepository.isRoomAvailable(roomId, checkIn, checkOut);
        return Map.of("available", available);
    }
}