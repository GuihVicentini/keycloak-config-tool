package com.guihvicentini.keycloakconfigtool.services;

import com.guihvicentini.keycloakconfigtool.containers.AbstractIntegrationTest;
import com.guihvicentini.keycloakconfigtool.exceptions.KeycloakAdapterException;
import com.guihvicentini.keycloakconfigtool.models.Component;
import com.guihvicentini.keycloakconfigtool.models.ConfigConstants;
import com.guihvicentini.keycloakconfigtool.services.export.ComponentExportService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
public class ComponentImportServiceTest extends AbstractIntegrationTest {

    @Autowired
    ComponentImportService importService;
    @Autowired
    ComponentExportService exportService;

    @Test
    @Order(1)
    public void testDoImport_ComponentExistInTargetButNotInActual_CreateLdapComponent() {
        List<Component> actual = List.of();
        List<Component> target = List.of(createLdapComponent("someLdap"));
        target.get(0).setSubComponents(List.of(createLdapSubComponent("someUserMapper")));

        importService.doImport(TEST_REALM, actual, target);

        List<Component> allComponents = exportService.getAll(TEST_REALM);
        Optional<Component> addedComponent = findComponentByName(allComponents, "someLdap");

        assertTrue(addedComponent.isPresent());
        assertFalse(addedComponent.get().getSubComponents().isEmpty());
        Optional<Component> subComponent = addedComponent.get().getSubComponents().stream()
                .filter(c -> c.getName().equals("someUserMapper")).findFirst();
        assertTrue(subComponent.isPresent());
        assertEquals(List.of("class"), subComponent.get().getConfig().get("ldap.attribute"));
    }

    @Test
    @Order(2)
    public void testDoImport_ComponentExistInTargetAndInActual_UpdateLdapComponent() {
        List<Component> actual = List.of(createLdapComponent("someLdap"));
        List<Component> target = List.of(createLdapComponent("someLdap"));
        target.get(0).setSubComponents(List.of(createLdapSubComponent("someUserMapper")));

        Component targetSubComponent = target.get(0).getSubComponents().stream()
                .filter(c -> c.getName().equals("someUserMapper")).findFirst().orElseThrow();
        targetSubComponent.getConfig().put("ldap.attribute",List.of("profession"));

        target.get(0).getConfig().put("connectionUrl", List.of("ldap:newLdapUrl.com:386"));

        importService.doImport(TEST_REALM, actual, target);

        List<Component> allComponents = exportService.getAll(TEST_REALM);
        Optional<Component> updatedComponent = findComponentByName(allComponents, "someLdap");

        assertTrue(updatedComponent.isPresent());

        assertTrue(updatedComponent.get().getConfig().containsKey("connectionUrl"));

        assertEquals(List.of("ldap:newLdapUrl.com:386"), updatedComponent.get().getConfig().get("connectionUrl"));

        assertFalse(updatedComponent.get().getSubComponents().isEmpty());
        Optional<Component> subComponent = updatedComponent.get().getSubComponents().stream()
                .filter(c -> c.getName().equals("someUserMapper")).findFirst();
        assertTrue(subComponent.isPresent());
        assertEquals(List.of("profession"), subComponent.get().getConfig().get("ldap.attribute"));
    }

    @Test
    @Order(3)
    public void testDoImport_ComponentExistInActualButNotInTarget_DeleteLdapComponent() {
        List<Component> actual = List.of(createLdapComponent("someLdap"));
        List<Component> target = List.of();

        Exception exception = assertThrows(KeycloakAdapterException.class,
                () -> importService.doImport(TEST_REALM, actual, target));

        assertTrue(exception.getMessage().contains("It is not allowed to delete the LDAP provider."));

    }

    @Test
    @Order(4)
    public void testDoImport_ComponentExistInTargetButNotInActual_CreateKeyComponent() {
        List<Component> actual = List.of();
        List<Component> target = List.of(createRsaKeyComponent("someKey"));

        importService.doImport(TEST_REALM, actual, target);

        List<Component> allComponents = exportService.getAll(TEST_REALM);
        Optional<Component> addedComponent = findComponentByName(allComponents, "someKey");

        assertTrue(addedComponent.isPresent());

    }

    @Test
    @Order(5)
    public void testDoImport_ComponentExistInTargetAndInActual_UpdateKeyComponent() {
        List<Component> actual = List.of(createRsaKeyComponent("someKey"));
        List<Component> target = List.of(createRsaKeyComponent("someKey"));

        target.get(0).getConfig().put("enabled", List.of("true"));
        target.get(0).getConfig().put("active", List.of("true"));


        importService.doImport(TEST_REALM, actual, target);

        List<Component> allComponents = exportService.getAll(TEST_REALM);
        Optional<Component> addedComponent = findComponentByName(allComponents, "someKey");

        assertTrue(addedComponent.isPresent());
        assertTrue(addedComponent.get().getConfig().containsKey("enabled"));
        assertEquals(List.of("true"), addedComponent.get().getConfig().get("enabled"));
        assertEquals(List.of("true"), addedComponent.get().getConfig().get("active"));

    }

    @Test
    @Order(6)
    public void testDoImport_ComponentExistInActualButNotInTarget_DeleteKeyComponent() {
        List<Component> actual = List.of(createRsaKeyComponent("someKey"));
        List<Component> target = List.of();

        importService.doImport(TEST_REALM, actual, target);

        List<Component> allComponents = exportService.getAll(TEST_REALM);
        Optional<Component> addedComponent = findComponentByName(allComponents, "someKey");

        assertFalse(addedComponent.isPresent());
    }

    private Optional<Component> findComponentByName(List<Component> allComponents, String name) {
        return allComponents.stream().filter(c -> name.equals(c.getName()))
                .findFirst();
    }

    private Component createLdapComponent(String name) {
        Component ldap = createComponent(name, "ldap", ConfigConstants.USER_STORAGE_PROVIDER_TYPE);
        ldap.setConfig(createConfigurationMap());
        return ldap;
    }

    private Component createLdapSubComponent(String name) {
        Component subComponent = createComponent(name, "user-attribute-ldap-mapper", ConfigConstants.LDAP_MAPPER_TYPE);
        subComponent.setConfig(createSubComponentConfig());
        return subComponent;
    }

    private Map<String, List<String>> createSubComponentConfig() {
        Map<String, List<String>> configMap = new HashMap<>();

        configMap.put("always.read.value.from.ldap", new ArrayList<>(List.of("false")));
        configMap.put("attribute.default.value", new ArrayList<>(List.of("wierd")));
        configMap.put("is.binary.attribute", new ArrayList<>(List.of("false")));
        configMap.put("is.mandatory.in.ldap", new ArrayList<>(List.of("true")));
        configMap.put("ldap.attribute", new ArrayList<>(List.of("class")));
        configMap.put("read.only", new ArrayList<>(List.of("false")));
        configMap.put("user.model.attribute", new ArrayList<>(List.of("class")));

        return configMap;
    }

    private Component createRsaKeyComponent(String name) {
        Component component = createComponent(name, "rsa-generated", ConfigConstants.KEY_PROVIDER_TYPE);
        component.setConfig(createKeyConfig());
        return component;
    }

    private Map<String, List<String>> createKeyConfig() {
        Map<String, List<String>> configMap = new HashMap<>();

        configMap.put("active", new ArrayList<>(List.of("false")));
        configMap.put("algorithm", new ArrayList<>(List.of("RS512")));
        configMap.put("enabled", new ArrayList<>(List.of("false")));
        configMap.put("keySize", new ArrayList<>(List.of("2048")));
        configMap.put("priority", new ArrayList<>(List.of("0")));

        return configMap;
    }

    private Component createComponent(String name, String providerId, String providerType) {
        Component component = new Component();
        component.setName(name);
        component.setProviderId(providerId);
        component.setProviderType(providerType);
        component.setSubComponents(new ArrayList<>());
        component.setConfig(new HashMap<>());
        return component;
    }

    private Map<String, List<String>> createConfigurationMap() {
        Map<String, List<String>> configurationMap = new TreeMap<>();

        configurationMap.put("pagination", Collections.singletonList("false"));
        configurationMap.put("fullSyncPeriod", Collections.singletonList("-1"));
        configurationMap.put("startTls", Collections.singletonList("false"));
        configurationMap.put("usersDn", Collections.singletonList("ou=Users,ou=Tenant,dc=domain,dc=com"));
        configurationMap.put("connectionPooling", Collections.singletonList("false"));
        configurationMap.put("cachePolicy", Collections.singletonList("DEFAULT"));
        configurationMap.put("useKerberosForPasswordAuthentication", Collections.singletonList("false"));
        configurationMap.put("importEnabled", Collections.singletonList("true"));
        configurationMap.put("enabled", Collections.singletonList("true"));
        configurationMap.put("bindDn", Collections.singletonList("uid=ServiceUser,ou=ServiceGroup,dc=somedc,dc=com"));
        configurationMap.put("changedSyncPeriod", Collections.singletonList("-1"));
        configurationMap.put("usernameLDAPAttribute", Collections.singletonList("uid"));
        configurationMap.put("vendor", Collections.singletonList("other"));
        configurationMap.put("uuidLDAPAttribute", Collections.singletonList("entryUUID"));
        configurationMap.put("allowKerberosAuthentication", Collections.singletonList("false"));
        configurationMap.put("connectionUrl", Collections.singletonList("ldaps://somedirectory.com"));
        configurationMap.put("syncRegistrations", Collections.singletonList("true"));
        configurationMap.put("authType", Collections.singletonList("simple"));
        configurationMap.put("useTruststoreSpi", Collections.singletonList("ldapsOnly"));
        configurationMap.put("usePasswordModifyExtendedOp", Collections.singletonList("false"));
        configurationMap.put("trustEmail", Collections.singletonList("false"));
        configurationMap.put("userObjectClasses", Arrays.asList("inetOrgPerson", "organizationalPerson"));
        configurationMap.put("rdnLDAPAttribute", Collections.singletonList("uid"));
        configurationMap.put("editMode", Collections.singletonList("WRITABLE"));
        configurationMap.put("validatePasswordPolicy", Collections.singletonList("false"));

        return configurationMap;
    }

}
