package com.smartcampus.config;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

// This sets the base URI for all your endpoints to /api/v1 as required by the coursework
@ApplicationPath("/api/v1")
public class ApiConfig extends Application {
    // Left empty on purpose. JAX-RS will automatically discover your resource classes.
}
