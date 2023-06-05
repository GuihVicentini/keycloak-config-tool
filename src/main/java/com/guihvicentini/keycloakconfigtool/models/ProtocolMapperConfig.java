package com.guihvicentini.keycloakconfigtool.models;

import com.guihvicentini.keycloakconfigtool.utils.MapUtil;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Getter
@Setter
public class ProtocolMapperConfig implements Config {
    private String name;
    private String protocol;
    private String protocolMapper;
    private Map<String, String> config;

    public void normalize() {
        config = config == null ? new HashMap<>() : config;
        config.keySet().removeAll(ConfigConstants.VALUES_TO_OMIT_FROM_CONFIG);
        MapUtil.removeMatchingEntries(config,ConfigConstants.VALUES_TO_OMIT_FROM_CONFIG_BASED_ON_VALUE);
        MapUtil.sortMapByKey(config);
    }

    @Override
    public String identifier() {
        return name;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ProtocolMapperConfig other = (ProtocolMapperConfig) obj;
        return Objects.equals(name, other.name)
                && Objects.equals(protocol, other.protocol)
                && Objects.equals(protocolMapper, other.protocolMapper)
                && Objects.equals(config, other.config);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, protocol, protocolMapper, config);
    }
}
