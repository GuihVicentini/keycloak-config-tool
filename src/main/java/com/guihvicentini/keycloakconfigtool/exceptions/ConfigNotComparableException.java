package com.guihvicentini.keycloakconfigtool.exceptions;

public class ConfigNotComparableException extends RuntimeException {

    public ConfigNotComparableException(String configObject) {
        super(String.format("Config object: %s does not have an identifier, therefore it is not comparable", configObject));
    }
}
