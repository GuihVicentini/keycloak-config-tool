package com.guihvicentini.keycloakconfigtool.exceptions;

public class JsonParsingException extends RuntimeException {

    public JsonParsingException(String path, Throwable cause){
        super(String.format("Unable to parse configuration definition from file: %s to realmConfig object", path), cause);
    }

    public JsonParsingException(Throwable cause){
        super("Unable to parse object", cause);
    }

}
