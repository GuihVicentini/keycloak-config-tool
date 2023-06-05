package com.guihvicentini.keycloakconfigtool.models;

import com.guihvicentini.keycloakconfigtool.utils.MapUtil;
import lombok.Getter;
import lombok.Setter;

import java.util.Collections;
import java.util.Map;

@Getter
@Setter
public class RequiredActionConfig implements Config {
    private String alias;
    private String name;
    private String providerId;
    private boolean enabled;
    private boolean defaultAction;
    private int priority;
    private Map<String, String> config;

    @Override
    public void normalize() {
        config = config == null ? Collections.emptyMap() : config;
        config.keySet().removeAll(ConfigConstants.VALUES_TO_OMIT_FROM_CONFIG);
        MapUtil.removeMatchingEntries(config, ConfigConstants.VALUES_TO_OMIT_FROM_CONFIG_BASED_ON_VALUE);
        MapUtil.sortMapByKey(config);
    }

    @Override
    public String identifier() {
        return alias;
    }
}
