package com.guihvicentini.keycloakconfigtool.models;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Objects;

@Getter
@Setter
public class AuthenticationSubFlow implements FlowElement {

    private String alias;
    private String description;
    private String requirement;
    private String providerId;
    private boolean authenticationFlow;
    private List<FlowElement> subFlowsAndExecutions;

    // TODO when deserializing the JSON, the authenticationFlow property is being set to false although in the JSON it is true.
    @Override
    public boolean isAuthenticationFlow(){
        return true;
    }
    @Override
    public void setAuthenticationFlow(boolean authenticationFlow) {
        this.authenticationFlow = true;
    }

    @Override
    public String identifier() {
        return alias;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AuthenticationSubFlow that = (AuthenticationSubFlow) o;
        return authenticationFlow == that.authenticationFlow &&
                Objects.equals(alias, that.alias) &&
                Objects.equals(description, that.description) &&
                Objects.equals(requirement, that.requirement) &&
                Objects.equals(providerId, that.providerId) &&
                Objects.equals(subFlowsAndExecutions, that.subFlowsAndExecutions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(alias, description, requirement, providerId, authenticationFlow, subFlowsAndExecutions);
    }
}
