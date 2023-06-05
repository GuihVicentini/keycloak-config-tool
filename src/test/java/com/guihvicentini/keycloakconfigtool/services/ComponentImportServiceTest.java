package com.guihvicentini.keycloakconfigtool.services;

import com.guihvicentini.keycloakconfigtool.containers.AbstractIntegrationTest;
import com.guihvicentini.keycloakconfigtool.models.ComponentExportConfig;
import com.guihvicentini.keycloakconfigtool.utils.JsonMapperUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ComponentImportServiceTest extends AbstractIntegrationTest {

    @Autowired
    ComponentImportService importService;
    private Map<String, List<ComponentExportConfig>> actual;
    private Map<String, List<ComponentExportConfig>> target;

    @BeforeAll
    public void setUp() {
        actual = new HashMap<>();
        target = new HashMap<>();

        setExisting(actual);
        setExisting(target);
    }

    @Test
    @Order(1)
    public void getAllComponents() {
        Map<String, List<ComponentExportConfig>> components = importService.getAllComponents(TEST_REALM);
        log.info("Components: {}", JsonMapperUtils.objectToJsonPrettyString(components));
    }

    @Test
    @Order(2)
    void testImportService_CreateComponent_WhenComponentInTargetButNotInActual() {

        // Create a new component export config to be added to the target
        ComponentExportConfig componentConfig = new ComponentExportConfig();
        componentConfig.setName("component1");
        componentConfig.setProviderId("rsa-enc-generated");
        componentConfig.normalize();

        target.get("org.keycloak.keys.KeyProvider").add(componentConfig);

        importService.doImport(TEST_REALM, actual, target);

        Map<String, List<ComponentExportConfig>> allComponents = importService.getAllComponents(TEST_REALM);

        assertTrue(allComponents.containsKey("org.keycloak.keys.KeyProvider"));
        assertTrue(allComponents.get("org.keycloak.keys.KeyProvider").contains(componentConfig));

    }

    @Test
    @Order(3)
    void testImportService_UpdateComponent_WhenComponentInTargetAndActualButNotTheSame() {

        // Create an existing component in the actual configuration
        ComponentExportConfig existingComponent = new ComponentExportConfig();
        existingComponent.setName("component1");
        existingComponent.setProviderId("rsa-enc-generated");
        existingComponent.normalize();

        // Create a new component in the target configuration with the same name but different attributes
        ComponentExportConfig updatedComponent = new ComponentExportConfig();
        updatedComponent.setName("component1");
        updatedComponent.setProviderId("rsa-enc-generated");
        updatedComponent.normalize();
        updatedComponent.getConfig().put("priority", List.of("50"));

        actual.get("org.keycloak.keys.KeyProvider").add(existingComponent);
        target.get("org.keycloak.keys.KeyProvider").remove(existingComponent);
        target.get("org.keycloak.keys.KeyProvider").add(updatedComponent);

        importService.doImport(TEST_REALM, actual, target);

        Map<String, List<ComponentExportConfig>> allComponents = importService.getAllComponents(TEST_REALM);
        assertTrue(allComponents.containsKey("org.keycloak.keys.KeyProvider"));
        assertTrue(allComponents.get("org.keycloak.keys.KeyProvider").contains(updatedComponent));

        Optional<ComponentExportConfig> actualComponent = allComponents.get("org.keycloak.keys.KeyProvider").stream()
                .filter(c -> c.getName().equals(updatedComponent.getName())).findFirst();

        assertTrue(actualComponent.isPresent());
        assertEquals(updatedComponent.getConfig(), actualComponent.get().getConfig());
    }

    @Test
    @Order(4)
    void testImportService_RemoveComponent_WhenComponentInActualButNotInTarget() {

        // Create an existing component in the actual configuration
        ComponentExportConfig existingComponent = new ComponentExportConfig();
        existingComponent.setName("component1");
        existingComponent.setProviderId("rsa-enc-generated");
        existingComponent.normalize();
        existingComponent.getConfig().put("priority", List.of("50"));

        // Add the existing component to the actual configuration
        target.get("org.keycloak.keys.KeyProvider").remove(existingComponent);

        importService.doImport(TEST_REALM, actual, target);

        Map<String, List<ComponentExportConfig>> allComponents = importService.getAllComponents(TEST_REALM);

        assertTrue(allComponents.containsKey("org.keycloak.keys.KeyProvider"));
        assertFalse(allComponents.get("org.keycloak.keys.KeyProvider").contains(existingComponent));

        Optional<ComponentExportConfig> actualComponent = allComponents.get("org.keycloak.keys.KeyProvider").stream()
                .filter(c -> c.getName().equals(existingComponent.getName())).findFirst();

        assertTrue(actualComponent.isEmpty());
    }

    private void setExisting(Map<String, List<ComponentExportConfig>> map) {
        map.putAll(importService.getAllComponents(TEST_REALM));
    }
}
