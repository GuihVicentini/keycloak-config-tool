package com.guihvicentini.keycloakconfigtool.models;

import com.guihvicentini.keycloakconfigtool.utils.MapUtil;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class IdentityProviderMapperConfig implements Config {
    private String name;
    private String identityProviderAlias;
    private String identityProviderMapper;
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
        return name;
    }
}
