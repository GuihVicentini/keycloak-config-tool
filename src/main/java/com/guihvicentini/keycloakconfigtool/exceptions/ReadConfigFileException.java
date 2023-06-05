package com.guihvicentini.keycloakconfigtool.exceptions;

public class ReadConfigFileException extends RuntimeException {

    public ReadConfigFileException(String path, Throwable cause){
        super(String.format("Unable to read configuration definition file: %s.\nFile may not exist", path), cause);
    }
}
