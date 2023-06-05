package com.guihvicentini.keycloakconfigtool.models;

import com.guihvicentini.keycloakconfigtool.utils.MapUtil;
import com.guihvicentini.keycloakconfigtool.utils.StringUtil;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

@Getter
@Setter
public class ComponentExportConfig implements Config {

    private String name;
    private String providerId;
    private String subType;
    private Map<String, List<ComponentExportConfig>> subComponents;
    private Map<String, List<String>> config;

    @Override
    public void normalize() {
        normalizeSubComponents();
        normalizeConfig();
    }

    @Override
    public String identifier() {
        return name+providerId;
    }

    public void denormalize(){
        denormalizeSubComponents();
    }

    private void denormalizeSubComponents() {
        subComponents = subComponents == null ? new HashMap<>() : subComponents;
        MapUtil.renameKeys(subComponents, ConfigConstants.KEYCLOAK_PROVIDERS_NAME);
        subComponents.values().forEach(componentExportConfigList ->
                componentExportConfigList.forEach(ComponentExportConfig::denormalize));
    }

    private void normalizeSubComponents() {
        subComponents = subComponents == null ? new HashMap<>() : subComponents;
        MapUtil.renameKeys(subComponents, StringUtil.lastWordSplitByDotsToLower);
        subComponents.values().forEach(list -> {
            list.forEach(ComponentExportConfig::normalize);
            list.sort(Comparator.comparing(ComponentExportConfig::identifier));
        });
        MapUtil.sortMapByKey(subComponents);
    }

    private void normalizeConfig() {
        config = config == null ? new HashMap<>() : config;
        config.keySet().removeAll(ConfigConstants.VALUES_TO_OMIT_FROM_CONFIG);
        MapUtil.sortMapByKey(config);
        config.values().forEach(Collections::sort);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ComponentExportConfig other = (ComponentExportConfig) obj;
        return Objects.equals(name, other.name) &&
                Objects.equals(providerId, other.providerId) &&
                Objects.equals(subType, other.subType) &&
                Objects.equals(subComponents, other.subComponents) &&
                Objects.equals(config, other.config);
    }

    @Override
    public int hashCode(){
        return Objects.hash(name, providerId, subType, subComponents, config);
    }

}
