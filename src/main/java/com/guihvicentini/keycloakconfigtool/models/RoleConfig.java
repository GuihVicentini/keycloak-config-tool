package com.guihvicentini.keycloakconfigtool.models;

import com.guihvicentini.keycloakconfigtool.utils.MapUtil;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class RoleConfig implements Config {
    private String name;
    private String description;
    private boolean composite;
    private CompositeConfig composites;
    private Boolean clientRole;
    private String containerId;
    protected Map<String, List<String>> attributes;

    @Override
    public void normalize(String containerName) {
        setContainerId(containerName);
        normalizeComposites();
        normalizeAttributes();
    }

    @Override
    public String identifier() {
        return name;
    }

    private void normalizeAttributes() {
        attributes = attributes == null ? new HashMap<>() : attributes;
        attributes.keySet().removeAll(ConfigConstants.VALUES_TO_OMIT_FROM_CONFIG);
        MapUtil.sortMapByKey(attributes);
    }

    private void normalizeComposites() {
        composites = composites == null ? new CompositeConfig() : composites;
        composites.normalize();
    }


}
