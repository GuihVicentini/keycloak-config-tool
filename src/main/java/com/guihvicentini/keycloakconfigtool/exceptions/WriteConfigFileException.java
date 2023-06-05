package com.guihvicentini.keycloakconfigtool.exceptions;

public class WriteConfigFileException extends RuntimeException {

    public WriteConfigFileException(String path, Throwable cause){
        super(String.format("Unable to write configuration definition to file: %s.\nFile path may not exist", path), cause);
    }

}
