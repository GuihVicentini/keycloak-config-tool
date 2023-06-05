package com.guihvicentini.keycloakconfigtool.services;

import com.guihvicentini.keycloakconfigtool.adapters.ComponentsResourceAdapter;
import com.guihvicentini.keycloakconfigtool.mappers.ComponentExportConfigMapper;
import com.guihvicentini.keycloakconfigtool.mappers.ComponentRepresentationMapper;
import com.guihvicentini.keycloakconfigtool.models.ComponentExportConfig;
import com.guihvicentini.keycloakconfigtool.models.ConfigConstants;
import com.guihvicentini.keycloakconfigtool.models.RealmConfig;
import com.guihvicentini.keycloakconfigtool.services.export.RealmExportService;
import com.guihvicentini.keycloakconfigtool.utils.JsonMapperUtils;
import com.guihvicentini.keycloakconfigtool.utils.MapUtil;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.common.util.MultivaluedHashMap;
import org.keycloak.representations.idm.ComponentExportRepresentation;
import org.keycloak.representations.idm.ComponentRepresentation;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ComponentImportService {

    private final ComponentsResourceAdapter resourceAdapter;
    private final ComponentRepresentationMapper representationMapper;

    private final ComponentExportConfigMapper componentExportConfigMapper;
    private final RealmExportService exportService;

    public ComponentImportService(ComponentsResourceAdapter resourceAdapter,
                                  ComponentRepresentationMapper representationMapper,
                                  ComponentExportConfigMapper componentExportConfigMapper, RealmExportService exportService) {
        this.resourceAdapter = resourceAdapter;
        this.representationMapper = representationMapper;
        this.componentExportConfigMapper = componentExportConfigMapper;
        this.exportService = exportService;
    }

    public void doImport(String realm, Map<String, List<ComponentExportConfig>> actual,
                         Map<String, List<ComponentExportConfig>> target) {

        if(actual == null) {
            createComponents(realm, target);
            return;
        }
        if (target.equals(actual)) {
            log.info(ConfigConstants.UP_TO_DATE_MESSAGE);
            return;
        }

        importComponents(realm, target);
        Map<String, List<ComponentExportConfig>> updatedComponents = getUpdatedComponents(realm);
        removeComponents(realm, MapUtil.getMissingElements(updatedComponents, target));
    }

    private Map<String, List<ComponentExportConfig>> getUpdatedComponents(String realm) {
        RealmConfig realmConfig = exportService.getRealm(realm);
        realmConfig.denormalize();
        return realmConfig.getComponents();
    }

    private void removeComponents(String realm, Map<String, List<ComponentExportConfig>> toBeDeleted) {
        toBeDeleted.forEach((key, values) -> removeComponents(realm, values));
    }

    private void removeComponents(String realm, List<ComponentExportConfig> components) {
        components.forEach(component -> removeComponent(realm, component));
    }

    private void removeComponent(String realm, ComponentExportConfig component) {
        Optional<ComponentRepresentation> representation = resourceAdapter.getByName(realm, component.getName());
        representation.ifPresent(componentRepresentation -> {
            log.debug("Deleting component: {}/{}", componentRepresentation.getProviderType(), componentRepresentation.getName());
            resourceAdapter.delete(realm, componentRepresentation);
        });
    }

    private void importComponents(String realm, Map<String, List<ComponentExportConfig>> components) {
        components.forEach((key, values) -> importComponents(realm, key, values));
    }

    private void importComponents(String realm, String providerType ,List<ComponentExportConfig> components) {
        components.forEach(component -> importComponent(realm, providerType, component));
    }

    private void importComponent(String realm, String providerType, ComponentExportConfig component) {
        Optional<ComponentRepresentation> existingComponent = resourceAdapter.getByName(realm, component.getName());

        if(existingComponent.isPresent()) {
            updateComponent(realm, providerType, component, existingComponent.get().getId(), existingComponent.get().getParentId());
        } else {
            // when parentId is null then the parent becomes the realm.
            createComponent(realm, providerType, component, null);
        }
    }

    private void updateComponent(String realm, String providerType, ComponentExportConfig component, String componentId, String parentId) {
        ComponentRepresentation representation = representationMapper.mapToRepresentation(component);
        representation.setProviderType(providerType);
        representation.setId(componentId);
        representation.setParentId(parentId);

        log.debug("Updating component: {}/{}", representation.getProviderType(), representation.getName());
        log.debug("Component: {}", JsonMapperUtils.objectToJsonPrettyString(representation));
        resourceAdapter.update(realm, representation);

        if(!component.getSubComponents().isEmpty()) {
            importSubComponents(realm, component.getSubComponents(), componentId);
        }
    }

    private void importSubComponents(String realm, Map<String, List<ComponentExportConfig>> subComponents, String parentId) {
        subComponents.forEach((key, values) -> importSubComponents(realm, key, values, parentId));

    }

    private void importSubComponents(String realm, String providerType, List<ComponentExportConfig> components, String parentId) {
        components.forEach(component -> importSubComponent(realm, providerType, component, parentId));
    }

    private void importSubComponent(String realm, String providerType, ComponentExportConfig component, String parentId) {
        Optional<ComponentRepresentation> representation = resourceAdapter.getByName(realm, component.getName());

        if(representation.isPresent()) {
            updateComponent(realm, providerType, component, representation.get().getId(), parentId);
        } else {
            createComponent(realm, providerType, component, parentId);
        }
    }

    private void createComponents(String realm, Map<String, List<ComponentExportConfig>> components) {
        components.forEach((key, value) -> createComponents(realm, key, value));
    }

    private void createComponents(String realm, String providerType, List<ComponentExportConfig> components) {
        // when parentId is null then the parent becomes the realm.
        components.forEach(component -> createComponent(realm, providerType, component, null));
    }

    private void createComponent(String realm, String providerType, ComponentExportConfig component, String parentId) {
        ComponentRepresentation representation = representationMapper.mapToRepresentation(component);
        representation.setProviderType(providerType);
        representation.setParentId(parentId);

        log.debug("Creating component: {}/{}", representation.getProviderType(), representation.getName());
        log.debug("Component: {}", JsonMapperUtils.objectToJsonString(component));
        String componentId = resourceAdapter.create(realm, representation);

        if(!component.getSubComponents().isEmpty()) {
            importSubComponents(realm, component.getSubComponents(), componentId);
        }
    }

//    private void createSubComponents(String realm, Map<String, List<ComponentExportConfig>> components, String parentId){
//        components.forEach((key, value) -> createSubComponents(realm, key, value, parentId));
//    }
//
//    private void createSubComponents(String realm, String providerType, List<ComponentExportConfig> components, String parentId) {
//        components.forEach(component -> createComponent(realm, providerType, component, parentId));
//    }


    // TODO migrate this logic to a export service class
    public Map<String, List<ComponentExportConfig>> getAllComponents(String realm) {
        String realmId =  resourceAdapter.getRealmId(realm);
        List<ComponentRepresentation> components = resourceAdapter.getAll(realm);
        List<ComponentRepresentation> subComponents = components.stream()
                .filter(component -> !component.getParentId().equals(realmId)).toList();
        components.removeAll(subComponents);
        Map<String, List<ComponentExportRepresentation>> componentExport = filterSubComponentsWithSameType(components);

        componentExport.values().forEach(list -> list.forEach(c -> {
            List<ComponentRepresentation> exportSubComponents = subComponents.stream()
                    .filter(s -> c.getId().equals(s.getParentId())).toList();
            Map<String, List<ComponentExportRepresentation>> filtered =  filterSubComponentsWithSameType(exportSubComponents);
            c.setSubComponents(new MultivaluedHashMap<>(filtered));
        }));

        Map<String, List<ComponentExportConfig>> componentConfigMap = new HashMap<>();

        componentExport.forEach((key, value) -> {
            componentConfigMap.put(key, componentExportConfigMapper.mapToConfigList(value));
        });

//        componentConfigMap.values().forEach(list -> list.forEach(ComponentExportConfig::normalize));

        return componentConfigMap;
    }

    private Map<String, List<ComponentExportRepresentation>> filterSubComponentsWithSameType(List<ComponentRepresentation> exportSubComponents) {
        return exportSubComponents.stream().collect(
                Collectors.groupingBy(ComponentRepresentation::getProviderType,
                        Collectors.mapping(this::mapExport, Collectors.toList())));
    }

    private ComponentExportRepresentation mapExport(ComponentRepresentation representation) {
        ComponentExportRepresentation export = new ComponentExportRepresentation();
        export.setId(representation.getId());
        export.setName(representation.getName());
        export.setProviderId(representation.getProviderId());
        export.setSubType(representation.getSubType());
        export.setConfig(representation.getConfig());
        export.setSubComponents(new MultivaluedHashMap<>());
        return export;
    }

}
