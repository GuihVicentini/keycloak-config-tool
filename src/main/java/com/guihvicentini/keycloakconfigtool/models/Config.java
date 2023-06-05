package com.guihvicentini.keycloakconfigtool.models;

public interface Config {

    default void normalize(){
        // default implementation does nothing.
    }
    default void normalize(String name) {
        // default implementation does nothing.
    }
    String identifier();
}
