package com.guihvicentini.keycloakconfigtool.models;

import lombok.Getter;
import lombok.Setter;

import java.util.Collections;
import java.util.Set;

@Getter
@Setter
public class ScopeMappingConfig implements Config {
    private String client;
    private String clientScope;
    private Set<String> roles;

    @Override
    public void normalize() {
        roles = roles == null ? Collections.emptySet() : roles;
    }

    @Override
    public String identifier() {
        return client == null ? clientScope : client;
    }
}
