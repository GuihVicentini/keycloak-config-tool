package com.guihvicentini.keycloakconfigtool.models;

import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
public class AuthenticationExecutionExportConfig implements Config {

    private String authenticatorConfig;
    private String authenticator;
    private boolean authenticatorFlow;
    private String requirement;
    private int priority;
    private String flowAlias;
    private boolean userSetupAllowed;

    @Override
    public String identifier() {
        return authenticatorFlow ? flowAlias : authenticator ;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        AuthenticationExecutionExportConfig other = (AuthenticationExecutionExportConfig) obj;
        return Objects.equals(authenticatorConfig, other.authenticatorConfig) &&
                Objects.equals(authenticator, other.authenticator) &&
                Objects.equals(authenticatorFlow, other.authenticatorFlow) &&
                Objects.equals(requirement, other.requirement) &&
                Objects.equals(priority, other.priority) &&
                Objects.equals(flowAlias, other.flowAlias) &&
                Objects.equals(userSetupAllowed, other.userSetupAllowed);
    }

    @Override
    public int hashCode(){
        return Objects.hash(authenticatorConfig, authenticator, authenticatorFlow, requirement, priority, flowAlias, userSetupAllowed);
    }
}
