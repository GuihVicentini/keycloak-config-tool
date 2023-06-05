package com.guihvicentini.keycloakconfigtool.utils;

import org.jboss.resteasy.client.jaxrs.internal.ClientResponse;

import javax.ws.rs.WebApplicationException;

public class ResponseUtil {
    ResponseUtil() {
        throw new IllegalStateException("Utility class");
    }

    public static String getErrorMessage(WebApplicationException error) {
        String errorBody = !((ClientResponse) error.getResponse()).isClosed() ? error.getResponse().readEntity(String.class).trim() : "";
        return error.getMessage() + errorBody;
    }
}
