package com.guihvicentini.keycloakconfigtool.models;

import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
public class AuthenticationExecution implements FlowElement {

    private String alias;
    private String providerId;
    private String requirement;
    private boolean authenticationFlow;
    private AuthenticatorConfigConfig config;

    @Override
    public boolean isAuthenticationFlow(){
        return false;
    }

    @Override
    public void setAuthenticationFlow(boolean authenticationFlow) {
        this.authenticationFlow = false;
    }
    @Override
    public String getAlias() {
        return providerId;
    }

    @Override
    public String identifier() {
        return providerId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AuthenticationExecution that = (AuthenticationExecution) o;
        return authenticationFlow == that.authenticationFlow &&
                Objects.equals(providerId, that.providerId) &&
                Objects.equals(requirement, that.requirement) &&
                Objects.equals(config, that.config);
    }

    @Override
    public int hashCode() {
        return Objects.hash(providerId, requirement, authenticationFlow, config);
    }

}
