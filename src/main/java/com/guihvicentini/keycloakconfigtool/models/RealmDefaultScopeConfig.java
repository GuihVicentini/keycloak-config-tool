package com.guihvicentini.keycloakconfigtool.models;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RealmDefaultScopeConfig implements Config {
    private String name;

    @Override
    public String identifier() {
        return name;
    }
}
