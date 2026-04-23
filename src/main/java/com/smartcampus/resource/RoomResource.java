package com.smartcampus.resource;

import java.util.List;
import java.util.UUID;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.smartcampus.dao.CampusDatabase;
import com.smartcampus.model.Room;

@Path("/rooms")
public class RoomResource {

    // GET /api/v1/rooms: Return all rooms
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Room> getAllRooms() {
        return CampusDatabase.ROOMS;
    }

    // GET /api/v1/rooms/{roomId}: Return a specific room
    @GET
    @Path("/{roomId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getRoomById(@PathParam("roomId") String roomId) {
        for (Room room : CampusDatabase.ROOMS) {
            if (room.getId().equals(roomId)) {
                return Response.ok(room).build();
            }
        }
        return Response.status(Response.Status.NOT_FOUND).entity("Room not found").build();
    }

    // POST /api/v1/rooms: Create a new room
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createRoom(Room newRoom) {
        // If the client didn't provide an ID, generate a unique one
        if (newRoom.getId() == null || newRoom.getId().isEmpty()) {
            newRoom.setId("RM-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        }
        CampusDatabase.ROOMS.add(newRoom);
        return Response.status(Response.Status.CREATED).entity(newRoom).build();
    }

    // DELETE /api/v1/rooms/{roomId}: Delete a room
    @DELETE
    @Path("/{roomId}")
    public Response deleteRoom(@PathParam("roomId") String roomId) {
        for (int i = 0; i < CampusDatabase.ROOMS.size(); i++) {
            Room room = CampusDatabase.ROOMS.get(i);
            if (room.getId().equals(roomId)) {

                // Business Logic Constraint: Block deletion if sensors are assigned
                if (!room.getSensorIds().isEmpty()) {
                    throw new com.smartcampus.exception.RoomNotEmptyException("Cannot delete room: active sensors are still assigned.");
                }

                CampusDatabase.ROOMS.remove(i);
                return Response.noContent().build(); // 204 No Content means success
            }
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }
}
