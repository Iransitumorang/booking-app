package org.acme.resource;

import org.acme.entity.Hotel;
import org.acme.entity.Room;
import org.acme.repository.HotelRepository;
import org.acme.repository.RoomRepository;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import java.util.List;

@Path("/hotels")
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
    public List<Hotel> getHotels() {
        return hotelRepository.listAll();
    }

    @GET
    @Path("/{hotelId}/rooms")
    public List<Room> getRoomsByHotel(@PathParam("hotelId") Long hotelId) {
        return roomRepository.findByHotel(hotelId);
    }
}