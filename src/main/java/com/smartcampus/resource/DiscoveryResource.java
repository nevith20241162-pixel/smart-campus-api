package com.smartcampus.resource;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

// Maps to the very base of our API (/api/v1)
@Path("/")
public class DiscoveryResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Object> getDiscoveryInfo() {
        Map<String, Object> metadata = new HashMap<>();

        // Essential API metadata
        metadata.put("version", "v1");
        metadata.put("admin_contact", "admin@smartcampus.edu");
        metadata.put("description", "Smart Campus Sensor & Room Management API");

        // Hypermedia map of primary resource collections
        Map<String, String> collections = new HashMap<>();
        collections.put("rooms", "/api/v1/rooms");
        collections.put("sensors", "/api/v1/sensors");

        metadata.put("collections", collections);

        return metadata;
    }
}
