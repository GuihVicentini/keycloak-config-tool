package com.guihvicentini.keycloakconfigtool.models;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "authenticationFlow")
@JsonSubTypes({
        @JsonSubTypes.Type(value = AuthenticationSubFlow.class, name = "true"),
        @JsonSubTypes.Type(value = AuthenticationExecution.class, name = "false")
})
public interface FlowElement extends Config {

    String getAlias();
    String getRequirement();
    String getProviderId();
    boolean isAuthenticationFlow();
    void setAuthenticationFlow(boolean authenticationFlow);

}
