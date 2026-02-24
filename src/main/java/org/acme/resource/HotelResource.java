package org.acme.resource;

import org.acme.entity.Hotel;
import org.acme.entity.Room;
import org.acme.repository.HotelRepository;
import org.acme.repository.RoomRepository;
import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
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
    public List<Hotel> getHotels() {
        return hotelRepository.listAll();
    }

    @GET
    @Path("/{hotelId}/rooms")
    public List<Room> getRoomsByHotel(@PathParam("hotelId") Long hotelId) {
        return roomRepository.findByHotel(hotelId);
    }
}