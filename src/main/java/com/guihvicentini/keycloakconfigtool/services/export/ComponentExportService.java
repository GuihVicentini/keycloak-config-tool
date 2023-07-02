package com.guihvicentini.keycloakconfigtool.services.export;

import com.guihvicentini.keycloakconfigtool.adapters.ComponentsResourceAdapter;
import com.guihvicentini.keycloakconfigtool.mappers.ComponentMapper;
import com.guihvicentini.keycloakconfigtool.models.Component;
import com.guihvicentini.keycloakconfigtool.models.ConfigConstants;
import org.keycloak.representations.idm.ComponentRepresentation;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ComponentExportService {

    private final ComponentsResourceAdapter adapter;

    private final ComponentMapper mapper;


    public ComponentExportService(ComponentsResourceAdapter adapter, ComponentMapper mapper) {
        this.adapter = adapter;
        this.mapper = mapper;
    }

    public List<Component> getAll(String realm) {
        List<Component> components = getAll(realm, ConfigConstants.USER_STORAGE_PROVIDER_TYPE);
        components.addAll(getAll(realm, ConfigConstants.KEY_PROVIDER_TYPE));
        return components;
    }

    public List<Component> getAll(String realm, String providerType) {
        List<ComponentRepresentation> allComponents = adapter.getAll(realm);
        List<ComponentRepresentation> filteredComponents = allComponents.stream()
                .filter(c -> c.getProviderType().equals(providerType))
                .toList();
        List<Component> components = filteredComponents.stream().map(mapper::mapToConfig)
                .collect(Collectors.toList());
        filteredComponents.forEach(c -> {
            List<Component> subComponents = allComponents.stream()
                    .filter(subComponent -> c.getId().equals(subComponent.getParentId()))
                    .map(mapper::mapToConfig)
                    .collect(Collectors.toList());
            components.stream().filter(config -> c.getName().equals(config.getName()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("component not found"))
                    .setSubComponents(subComponents);
        });
        return components;
    }
}
