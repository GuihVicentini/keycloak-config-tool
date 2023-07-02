package com.guihvicentini.keycloakconfigtool.models;

import com.guihvicentini.keycloakconfigtool.utils.MapUtil;
import lombok.Getter;
import lombok.Setter;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

@Getter
@Setter
public class AuthenticatorConfigConfig implements Config {

    private String alias;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AuthenticatorConfigConfig)) return false;
        AuthenticatorConfigConfig that = (AuthenticatorConfigConfig) o;
        return Objects.equals(alias, that.alias) &&
                Objects.equals(config, that.config);
    }

    @Override
    public int hashCode() {
        return Objects.hash(alias, config);
    }
}
