package com.guihvicentini.keycloakconfigtool.models;

import com.guihvicentini.keycloakconfigtool.utils.MapUtil;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

@Getter
@Setter
public class Component implements Config{

    private String name;
    private String providerId;
    private String providerType;
    private String subType;
    private List<Component> subComponents;
    private Map<String, List<String>> config;


    @Override
    public String identifier() {
        return name;
    }

    @Override
    public void normalize(){
        normalizeConfig();
        normalizeSubComponents();
    }

    private void normalizeSubComponents() {
        subComponents = subComponents == null ? new ArrayList<>() : subComponents;
        subComponents.forEach(Component::normalize);
        subComponents.sort(Comparator.comparing(Component::getName));
    }

    private void normalizeConfig() {
        config = config == null ? new HashMap<>() : config;
        config.keySet().removeAll(ConfigConstants.VALUES_TO_OMIT_FROM_CONFIG);
        MapUtil.sortMapByKey(config);
        config.values().forEach(Collections::sort);
    }
}
