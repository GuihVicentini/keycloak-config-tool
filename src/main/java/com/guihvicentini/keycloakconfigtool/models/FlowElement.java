package com.guihvicentini.keycloakconfigtool.models;

public interface FlowElement extends Config {

    String getAlias();
    String getRequirement();
    String getProviderId();
    boolean isAuthenticationFlow();

}
