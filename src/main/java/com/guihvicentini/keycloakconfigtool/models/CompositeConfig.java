package com.guihvicentini.keycloakconfigtool.models;

import com.guihvicentini.keycloakconfigtool.exceptions.ConfigNotComparableException;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

@Getter
@Setter
public class CompositeConfig implements Config {
    private Set<String> realm;
    private Map<String, List<String>> client;

    public void normalize() {
        normalizeRealm();
        normalizeClient();
    }

    @Override
    public String identifier() {
        throw new ConfigNotComparableException("CompositeConfig");
    }

    private void normalizeClient() {
        client = client == null ? new HashMap<>() : client;
        client.values().forEach(Collections::sort);
    }

    private void normalizeRealm() {
        realm = realm == null ? new HashSet<>() : realm;
    }

}