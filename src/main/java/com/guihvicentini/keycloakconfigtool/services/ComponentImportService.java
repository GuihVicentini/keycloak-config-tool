package com.guihvicentini.keycloakconfigtool.services;

import com.guihvicentini.keycloakconfigtool.adapters.ComponentsResourceAdapter;
import com.guihvicentini.keycloakconfigtool.mappers.ComponentMapper;
import com.guihvicentini.keycloakconfigtool.models.Component;
import com.guihvicentini.keycloakconfigtool.models.ConfigConstants;
import com.guihvicentini.keycloakconfigtool.utils.JsonMapperUtils;
import com.guihvicentini.keycloakconfigtool.utils.ListUtil;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.representations.idm.ComponentRepresentation;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ComponentImportService {

    private final ComponentsResourceAdapter resourceAdapter;
    private final ComponentMapper componentMapper;

    public ComponentImportService(ComponentsResourceAdapter resourceAdapter, ComponentMapper componentMapper) {
        this.resourceAdapter = resourceAdapter;
        this.componentMapper = componentMapper;
    }

    public void doImport(String realm, List<Component> actual, List<Component> target) {

        if (actual.equals(target)) {
            log.debug(ConfigConstants.UP_TO_DATE_MESSAGE);
            return;
        }

        List<Component> toBeAdded = ListUtil.getMissingConfigElements(target, actual);
        List<Component> toDeleted = ListUtil.getMissingConfigElements(actual, target);
        List<Component> toBeUpdated = ListUtil.getNonEqualConfigsWithSameIdentifier(target, actual);

        addComponents(realm, resourceAdapter.getRealmId(realm), toBeAdded);
        updateComponents(realm, toBeUpdated);
        deleteComponents(realm, toDeleted);


    }


    private void updateComponents(String realm, List<Component> components) {
        components.forEach(component -> updateComponent(realm, component));
    }

    private void updateComponent(String realm, Component component) {
        Optional<ComponentRepresentation> maybeComponent = resourceAdapter.getByName(realm, component.getName());
        if(maybeComponent.isPresent()) {
            ComponentRepresentation existingComponent = maybeComponent.get();
            ComponentRepresentation updatedComponent = componentMapper.mapToRepresentation(component);
            updatedComponent.setId(existingComponent.getId());
            log.debug("Updating Component: {} ", JsonMapperUtils.objectToJsonPrettyString(component));
            resourceAdapter.update(realm, updatedComponent);
            List<ComponentRepresentation> actualSubComponents = resourceAdapter.getAll(realm)
                    .stream().filter(subComponent -> existingComponent.getId().equals(subComponent.getParentId()))
                    .collect(Collectors.toList());

            updateSubComponents(realm, updatedComponent.getId(), actualSubComponents, component.getSubComponents());
        }
    }

    private void updateSubComponents(String realm, String parentId, List<ComponentRepresentation> actualSubComponents,
                                     List<Component> subComponents) {
        List<Component> actualSubComponentsConfig = actualSubComponents.stream()
                .map(componentMapper::mapToConfig).collect(Collectors.toList());

        List<Component> toBeAdded = ListUtil.getMissingConfigElements(subComponents, actualSubComponentsConfig);
        List<Component> toDeleted = ListUtil.getMissingConfigElements(actualSubComponentsConfig, subComponents);
        List<Component> toUpdated = ListUtil.getNonEqualConfigsWithSameIdentifier(subComponents, actualSubComponentsConfig);

        addComponents(realm, parentId, toBeAdded);
        deleteComponents(realm, toDeleted);
        updateComponents(realm, toUpdated);

    }

    private void deleteComponents(String realm, List<Component> components) {
        components.forEach(component -> deleteComponent(realm, component));
    }

    private void deleteComponent(String realm, Component component) {
        Optional<ComponentRepresentation> actualComponent = resourceAdapter.getByName(realm, component.getName());
        log.debug("Deleting Component: {} ", JsonMapperUtils.objectToJsonPrettyString(component));
        actualComponent.ifPresent(representation -> resourceAdapter.delete(realm, representation));
    }

    private void addComponents(String realm, String parentId, List<Component> components) {
        components.forEach(component -> addComponent(realm, parentId, component));
    }

    private void addComponent(String realm, String parentId, Component component) {
        ComponentRepresentation representation = componentMapper.mapToRepresentation(component);
        representation.setParentId(parentId);
        log.debug("Adding Component: {} with parentId: {}", JsonMapperUtils.objectToJsonPrettyString(component), parentId);
        String createdComponentId = resourceAdapter.create(realm, representation);

        // check if subcomponents were created automatically
        List<Component> actualSubComponents = resourceAdapter.getAll(realm).stream()
                .filter(subComponent -> createdComponentId.equals(subComponent.getParentId()))
                .map(componentMapper::mapToConfig)
                .collect(Collectors.toList());

        List<Component> missingSubComponents = ListUtil.getMissingConfigElements(component.getSubComponents(), actualSubComponents);

        // create subComponents recursively
        addComponents(realm, createdComponentId, missingSubComponents);
    }

}
