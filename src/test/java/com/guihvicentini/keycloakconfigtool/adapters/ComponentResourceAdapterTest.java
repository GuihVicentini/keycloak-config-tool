package com.guihvicentini.keycloakconfigtool.adapters;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.guihvicentini.keycloakconfigtool.containers.AbstractIntegrationTest;
import com.guihvicentini.keycloakconfigtool.exceptions.KeycloakAdapterException;
import com.guihvicentini.keycloakconfigtool.utils.ListUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.keycloak.common.util.MultivaluedHashMap;
import org.keycloak.representations.idm.ComponentRepresentation;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
public class ComponentResourceAdapterTest extends AbstractIntegrationTest {

    private static final String INPUT_FOLDER = "src/test/resources/input/adapters/components/";
    public static final String TEST_REALM_COMPONENTS = INPUT_FOLDER + "test-realm-components.json";

    @Autowired
    private ComponentsResourceAdapter resourceAdapter;

    @Autowired
    private ObjectMapper mapper;

    private static final String REALM = "test";
    private static final String REALM_ID = "8b11c53b-fb43-4e63-85cd-118df6b66386";

    List<ComponentRepresentation> expectedComponents;

    @BeforeEach
    public void setup() throws IOException {
        expectedComponents = Arrays.asList(mapper.readValue(new File(TEST_REALM_COMPONENTS),
                        ComponentRepresentation[].class));
    }

    @Test
    @Order(1)
    public void queryComponents(){
        List<ComponentRepresentation> components = resourceAdapter.getAll(REALM);
        // ComponentRepresentation is equal if the ids are the same
        assertEquals(expectedComponents, components);
    }

    @Test
    @Order(2)
    public void updateComponent(){
        ComponentRepresentation toBeUpdated = ListUtil.getRandomElement(expectedComponents);
        toBeUpdated.setName("new-component-name");
        resourceAdapter.update(REALM, toBeUpdated);
        assertEquals(resourceAdapter.getById(REALM, toBeUpdated.getId()), toBeUpdated);
    }


    @Test
    @Order(3)
    public void getComponentByName(){
        ComponentRepresentation component = ListUtil.getRandomElement(expectedComponents);
        Optional<ComponentRepresentation> foundedComponent = resourceAdapter.getByName(REALM, component.getName());
        assertTrue(foundedComponent.isPresent());
        assertEquals(foundedComponent.get(), component);

    }

    @Test
    @Order(4)
    public void whenComponentNotLdap_thenDeleteComponent(){
        ComponentRepresentation toBeDeleted = expectedComponents
                .stream().filter(component -> !component.getProviderId().equals("ldap"))
                .findFirst().orElseThrow(() -> new RuntimeException("there is no component for testing"));
        resourceAdapter.delete(REALM, toBeDeleted);
        Exception exception = assertThrows(KeycloakAdapterException.class,
                () -> resourceAdapter.getById(REALM, toBeDeleted.getId()));
        assertTrue(exception.getMessage().contains(toBeDeleted.getId()));
    }

    @Test
    @Order(5)
    public void whenComponentLdap_andDeleteComponent_thenThrowException(){
        ComponentRepresentation toBeDeleted = expectedComponents
                .stream().filter(component -> component.getProviderId().equals("ldap"))
                .findFirst().orElseThrow(() -> new RuntimeException("there is no ldap component for testing"));
        Exception exception = assertThrows(KeycloakAdapterException.class,() -> resourceAdapter.delete(REALM, toBeDeleted));
        assertTrue(exception.getMessage().contains(toBeDeleted.getName()));
    }

    @Test
    @Order(5)
    public void createComponent(){
        List<ComponentRepresentation> previousComponents = resourceAdapter.getAll(REALM);

        ComponentRepresentation toBeAdded = new ComponentRepresentation();
        toBeAdded.setName("new-component");
        toBeAdded.setProviderId("rsa-generated");
        toBeAdded.setProviderType("org.keycloak.keys.KeyProvider");
        toBeAdded.setParentId(null);
        toBeAdded.setConfig(new MultivaluedHashMap<>());

        String newId = resourceAdapter.create(REALM, toBeAdded);

        assertTrue(previousComponents.stream().filter(component -> component.getId().equals(newId)).findFirst().isEmpty());

        ComponentRepresentation representation = resourceAdapter.getById(REALM, newId);
        assertEquals(REALM_ID, representation.getParentId());
    }


    @Test
    @Order(6)
    public void whenRealmDoesntExist_andGetComponentByName_thenTrowException(){
        Exception exception = assertThrows(KeycloakAdapterException.class,
                () -> resourceAdapter.getByName("no-realm", "component-name"));
        assertTrue(exception.getMessage().contains("no-realm"));
    }

}
