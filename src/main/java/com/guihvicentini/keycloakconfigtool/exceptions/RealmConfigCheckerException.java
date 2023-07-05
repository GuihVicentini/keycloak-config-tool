package com.guihvicentini.keycloakconfigtool.exceptions;

import com.fasterxml.jackson.databind.JsonNode;

public class RealmConfigCheckerException extends RuntimeException {

    private static final String message = "Actual realm config is not equal to expected realm config\n" +
            "Expected: '%s'\n" +
            "=".repeat(1000)+
            "\nActual: '%s'";
    public RealmConfigCheckerException(JsonNode expected, JsonNode actual) {
        super(String.format(message, expected.toPrettyString(), actual.toPrettyString()));
    }
}
