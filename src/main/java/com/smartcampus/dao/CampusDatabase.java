package com.smartcampus.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;

public class CampusDatabase {

    public static final List<Room> ROOMS = new ArrayList<>();
    public static final List<Sensor> SENSORS = new ArrayList<>();

    // Maps a Sensor ID to its history of readings
    public static final Map<String, List<SensorReading>> READINGS_HISTORY = new HashMap<>();

    static {
        // 1. Create the room
        Room library = new Room("LIB-301", "Library Quiet Study", 50);

        // 2. THIS IS THE MISSING LINK: Manually assign the mock sensor ID to the room
        library.getSensorIds().add("SENS-123");

        // 3. Add to database
        ROOMS.add(library);
        SENSORS.add(new Sensor("SENS-123", "CO2", "ACTIVE", "LIB-301"));
        READINGS_HISTORY.put("SENS-123", new ArrayList<>());
    }
}
