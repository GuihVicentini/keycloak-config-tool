package com.guihvicentini.keycloakconfigtool.models;

import com.guihvicentini.keycloakconfigtool.utils.MapUtil;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Getter
@Setter
public class RequiredActionProviderConfig implements Config {
    private String alias;
    private String name;
    private String providerId;
    private boolean enabled;
    private boolean defaultAction;
    private int priority;
    private Map<String, String> config;

    @Override
    public void normalize() {
        config = config == null ? new HashMap<>() : config;
        config.keySet().removeAll(ConfigConstants.VALUES_TO_OMIT_FROM_CONFIG);
        MapUtil.removeMatchingEntries(config, ConfigConstants.VALUES_TO_OMIT_FROM_CONFIG_BASED_ON_VALUE);
        MapUtil.sortMapByKey(config);
    }

    @Override
    public String identifier() {
        return alias;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RequiredActionProviderConfig other = (RequiredActionProviderConfig) o;
        return enabled == other.enabled &&
                defaultAction == other.defaultAction &&
                priority == other.priority &&
                Objects.equals(alias, other.alias) &&
                Objects.equals(name, other.name) &&
                Objects.equals(providerId, other.providerId) &&
                Objects.equals(config, other.config);
    }

    @Override
    public int hashCode() {
        return Objects.hash(alias, name, providerId, enabled, defaultAction, priority, config);
    }
}
