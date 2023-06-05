package com.guihvicentini.keycloakconfigtool.models;

import com.guihvicentini.keycloakconfigtool.utils.MapUtil;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

@Getter
@Setter
public class ClientScopeConfig implements Config {
    @NotNull
    private String name;
    private String description;
    private String protocol;
    private Map<String, String> attributes;
    private List<ProtocolMapperConfig> protocolMappers;

    @Override
    public void normalize() {
        normalizeAttributes();
        normalizeProtocolMappers();
    }

    @Override
    public String identifier() {
        return name;
    }

    private void normalizeProtocolMappers() {
        protocolMappers = protocolMappers == null ? new ArrayList<>() : protocolMappers;
        protocolMappers.sort(Comparator.comparing(ProtocolMapperConfig::getName));
    }

    private void normalizeAttributes() {
        attributes = attributes == null ? new HashMap<>() : attributes;

        // remove any unwanted value
        attributes.keySet().removeAll(ConfigConstants.VALUES_TO_OMIT_FROM_CONFIG);
        MapUtil.removeMatchingEntries(attributes, ConfigConstants.VALUES_TO_OMIT_FROM_CONFIG_BASED_ON_VALUE);

        MapUtil.sortMapByKey(attributes);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ClientScopeConfig other = (ClientScopeConfig) obj;
        return Objects.equals(name, other.name)
                && Objects.equals(description, other.description)
                && Objects.equals(protocol, other.protocol)
                && Objects.equals(attributes, other.attributes)
                && Objects.equals(protocolMappers, other.protocolMappers);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, description, protocol, attributes, protocolMappers);
    }
}
