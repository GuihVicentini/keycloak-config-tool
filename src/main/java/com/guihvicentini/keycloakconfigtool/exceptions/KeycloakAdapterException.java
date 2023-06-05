package com.guihvicentini.keycloakconfigtool.exceptions;

public class KeycloakAdapterException extends RuntimeException {

    public KeycloakAdapterException(String format, Object... args) {
        super(String.format(format, args));
    }

    public KeycloakAdapterException(String message, Throwable cause) {
        super(message, cause);
    }

    public KeycloakAdapterException(String format, Throwable cause, Object... args) {
        super(String.format(format, args), cause);
    }
}
