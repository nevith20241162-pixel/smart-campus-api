package com.smartcampus.exception;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import com.smartcampus.model.ErrorMessage;

@Provider
public class RoomNotEmptyExceptionMapper implements ExceptionMapper<RoomNotEmptyException> {

    @Override
    public Response toResponse(RoomNotEmptyException ex) {
        ErrorMessage errorMessage = new ErrorMessage(ex.getMessage(), 409);
        return Response.status(Response.Status.CONFLICT).entity(errorMessage).build();
    }
}
