package com.smartcampus.exception;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import com.smartcampus.model.ErrorMessage;

@Provider
public class SensorUnavailableExceptionMapper implements ExceptionMapper<SensorUnavailableException> {

    @Override
    public Response toResponse(SensorUnavailableException ex) {
        ErrorMessage errorMessage = new ErrorMessage(ex.getMessage(), 403);
        return Response.status(Response.Status.FORBIDDEN).entity(errorMessage).build();
    }
}
