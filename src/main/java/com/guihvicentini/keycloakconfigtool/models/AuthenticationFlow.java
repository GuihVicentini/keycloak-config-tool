package com.guihvicentini.keycloakconfigtool.models;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Objects;

@Getter
@Setter
public class AuthenticationFlow implements Config {

    private String alias;
    private String description;
    private String providerId;
    private boolean builtIn;
    private boolean topLevel;
    private List<FlowElement> subFlowsAndExecutions;

    @Override
    public String identifier() {
        return alias;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AuthenticationFlow that = (AuthenticationFlow) o;
        return builtIn == that.builtIn &&
                topLevel == that.topLevel &&
                Objects.equals(alias, that.alias) &&
                Objects.equals(description, that.description) &&
                Objects.equals(providerId, that.providerId) &&
                Objects.equals(subFlowsAndExecutions, that.subFlowsAndExecutions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(alias, description, providerId, builtIn, topLevel, subFlowsAndExecutions);
    }

}
