package com.smartcampus.resource;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.smartcampus.dao.CampusDatabase;
import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;

@Path("/sensors")
public class SensorResource {

    // GET /api/v1/sensors?type={optional_type} : Get sensors, optionally filtered by type
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSensors(@QueryParam("type") String type) {
        // If no filter is provided, return all sensors
        if (type == null || type.trim().isEmpty()) {
            return Response.ok(CampusDatabase.SENSORS).build();
        }

        // If a type filter is provided (e.g., ?type=CO2), filter the list
        List<Sensor> filteredSensors = new ArrayList<>();
        for (Sensor sensor : CampusDatabase.SENSORS) {
            if (sensor.getType().equalsIgnoreCase(type)) {
                filteredSensors.add(sensor);
            }
        }
        return Response.ok(filteredSensors).build();
    }

    // POST /api/v1/sensors : Register a new sensor
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createSensor(Sensor newSensor) {
        // 1. Verify the room actually exists
        boolean roomExists = false;
        Room targetRoom = null;

        for (Room room : CampusDatabase.ROOMS) {
            if (room.getId().equals(newSensor.getRoomId())) {
                roomExists = true;
                targetRoom = room;
                break;
            }
        }

        // If the room doesn't exist, reject the request
        if (!roomExists) {
            throw new com.smartcampus.exception.LinkedResourceNotFoundException("Cannot register sensor: Room ID '" + newSensor.getRoomId() + "' does not exist.");
        }

        // 2. Generate an ID and add the sensor
        if (newSensor.getId() == null || newSensor.getId().isEmpty()) {
            newSensor.setId("SENS-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        }

        CampusDatabase.SENSORS.add(newSensor);

        // 3. Link the sensor to the room's list of active sensors
        targetRoom.getSensorIds().add(newSensor.getId());

        return Response.status(Response.Status.CREATED).entity(newSensor).build();
    }

    // SUB-RESOURCE LOCATOR: Delegates /sensors/{sensorId}/readings to another class
    @Path("/{sensorId}/readings")
    public SensorReadingResource getSensorReadingResource(@PathParam("sensorId") String sensorId) {
        // We pass the sensorId into the new class so it knows which sensor it is handling
        return new SensorReadingResource(sensorId);
    }
}
