package com.guihvicentini.keycloakconfigtool.models;

import lombok.Getter;
import lombok.Setter;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
public class AuthenticationFlowConfig implements Config {

    private String alias;
    private String description;
    private String providerId;
    private boolean topLevel;
    private boolean builtIn;
    private List<AuthenticationExecutionExportConfig> authenticationExecutions;

    @Override
    public void normalize() {
        authenticationExecutions = authenticationExecutions == null ? Collections.emptyList() : authenticationExecutions;
        authenticationExecutions.forEach(AuthenticationExecutionExportConfig::normalize);
        authenticationExecutions.sort(Comparator.comparing(AuthenticationExecutionExportConfig::getPriority));
    }

    @Override
    public String identifier() {
        return alias;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        AuthenticationFlowConfig other = (AuthenticationFlowConfig) obj;
        return Objects.equals(alias, other.alias) &&
                Objects.equals(providerId, other.providerId) &&
                Objects.equals(description, other.description) &&
                Objects.equals(topLevel, other.topLevel) &&
                Objects.equals(builtIn, other.builtIn) &&
                Objects.equals(authenticationExecutions, other.authenticationExecutions);
    }

    @Override
    public int hashCode(){
        return Objects.hash(alias, providerId, description, topLevel, builtIn, authenticationExecutions);
    }
}
