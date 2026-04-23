package com.smartcampus.resource;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.smartcampus.dao.CampusDatabase;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;

public class SensorReadingResource {

    private String parentSensorId;

    // Constructor receives the ID from the Sub-Resource Locator
    public SensorReadingResource(String parentSensorId) {
        this.parentSensorId = parentSensorId;
    }

    // GET /api/v1/sensors/{sensorId}/readings : Fetch history
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getReadings() {
        List<SensorReading> history = CampusDatabase.READINGS_HISTORY.getOrDefault(parentSensorId, new ArrayList<>());
        return Response.ok(history).build();
    }

    // POST /api/v1/sensors/{sensorId}/readings : Add a new reading
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addReading(SensorReading newReading) {
        // 1. Find the parent sensor to update its currentValue
        Sensor targetSensor = null;
        for (Sensor sensor : CampusDatabase.SENSORS) {
            if (sensor.getId().equals(parentSensorId)) {
                targetSensor = sensor;
                break;
            }
        }

        if (targetSensor == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Parent sensor not found.").build();
        }

        if ("MAINTENANCE".equalsIgnoreCase(targetSensor.getStatus())) {
            throw new com.smartcampus.exception.SensorUnavailableException("Sensor is in maintenance.");
        }

        // 2. Setup the new reading
        if (newReading.getId() == null) {
            newReading.setId("RD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        }
        if (newReading.getTimestamp() == 0) {
            newReading.setTimestamp(System.currentTimeMillis());
        }

        // 3. Save the reading to history
        CampusDatabase.READINGS_HISTORY.putIfAbsent(parentSensorId, new ArrayList<>());
        CampusDatabase.READINGS_HISTORY.get(parentSensorId).add(newReading);

        // 4. Update the parent sensor's current value (Side Effect required by spec)
        targetSensor.setCurrentValue(newReading.getValue());

        return Response.status(Response.Status.CREATED).entity(newReading).build();
    }
}
