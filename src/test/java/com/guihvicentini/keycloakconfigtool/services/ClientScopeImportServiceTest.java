package com.guihvicentini.keycloakconfigtool.services;

import com.guihvicentini.keycloakconfigtool.containers.AbstractIntegrationTest;
import com.guihvicentini.keycloakconfigtool.models.ClientScopeConfig;
import com.guihvicentini.keycloakconfigtool.models.ProtocolMapperConfig;
import com.guihvicentini.keycloakconfigtool.services.export.ClientScopeExportService;
import com.guihvicentini.keycloakconfigtool.utils.JsonMapperUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
public class ClientScopeImportServiceTest extends AbstractIntegrationTest {


    @Autowired
    ClientScopeImportService importService;

    @Autowired
    ClientScopeExportService exportService;

    @Test
    @Order(1)
    public void getAllClientScopes(){
        List<ClientScopeConfig> clientScopes = exportService.getAllClientScopes(TEST_REALM);
        log.info("ClientScopes: {}", JsonMapperUtils.objectToJsonPrettyString(clientScopes));
        clientScopes.forEach(scope -> assertNotNull(scope.getProtocolMappers()));
    }

    @Test
    @Order(2)
    void testDoImport_ClientScopeExistsInTargetButNotInActual_CreateClientScope() {
        // Existing client scopes in the realm
        List<ClientScopeConfig> actualClientScopes = Collections.emptyList();

        // Target client scopes to import/update (scope1 exists, scope2 is missing)
        List<ClientScopeConfig> targetClientScopes = Arrays.asList(
                createClientScopeConfig("scope1"),
                createClientScopeConfig("scope2")
        );

        importService.doImport(TEST_REALM, actualClientScopes, targetClientScopes);

        List<ClientScopeConfig> importedClientScopes = exportService.getAllClientScopes(TEST_REALM);

        Optional<ClientScopeConfig> createdClientScopeOne = importedClientScopes.stream()
                .filter(group -> group.getName().equals("scope1"))
                .findFirst();

        assertTrue(createdClientScopeOne.isPresent());
        assertEquals("scope1", createdClientScopeOne.get().getName());

        Optional<ClientScopeConfig> createdClientScopeTwo = importedClientScopes.stream()
                .filter(group -> group.getName().equals("scope2"))
                .findFirst();

        assertTrue(createdClientScopeTwo.isPresent());
        assertEquals("scope2", createdClientScopeTwo.get().getName());
    }

    @Test
    @Order(3)
    void testDoImport_ClientScopeExistsInTargetAndInActual_UpdateClientScope() {
        // Existing client scopes in the realm
        List<ClientScopeConfig> actualClientScopes = Arrays.asList(
                createClientScopeConfig("scope1", "mapper1"), // Existing client scope with different mapper
                createClientScopeConfig("scope2", "mapper2") // Existing client scope with same mapper
        );

        // Target client scopes to import/update
        List<ClientScopeConfig> targetClientScopes = Arrays.asList(
                createClientScopeConfig("scope1", "updatedMapper1"), // Updated client scope
                createClientScopeConfig("scope2", "mapper2") // Existing client scope
        );

        importService.doImport(TEST_REALM, actualClientScopes, targetClientScopes);

        List<ClientScopeConfig> importedClientScopes = exportService.getAllClientScopes(TEST_REALM);

        Optional<ClientScopeConfig> updatedClientScope = importedClientScopes.stream()
                .filter(group -> group.getName().equals("scope1"))
                .findFirst();

        assertTrue(updatedClientScope.isPresent());

        ClientScopeConfig updated = updatedClientScope.get();
        ClientScopeConfig expected = targetClientScopes.get(0);

        log.debug("Expected: {}", JsonMapperUtils.objectToJsonString(expected));
        log.debug("Actual: {}", JsonMapperUtils.objectToJsonString(updated));

        assertEquals(expected, updated);
        assertEquals(expected.getProtocolMappers(), updated.getProtocolMappers());


    }

    @Test
    @Order(4)
    void testDoImport_ClientScopeExistsInActualButNotInTarget_DeleteClientScope() {
        // Existing client scopes in the realm
        List<ClientScopeConfig> actualClientScopes = Arrays.asList(
                createClientScopeConfig("scope1"),
                createClientScopeConfig("scope2")
        );

        // Target client scopes to import/update (scope1 exists, scope2 is missing)
        List<ClientScopeConfig> targetClientScopes = Collections.singletonList(
                createClientScopeConfig("scope1")
        );

        importService.doImport(TEST_REALM, actualClientScopes, targetClientScopes);

        List<ClientScopeConfig> importedClientScopes = exportService.getAllClientScopes(TEST_REALM);

        Optional<ClientScopeConfig> deletedClientScope = importedClientScopes.stream()
                .filter(group -> group.getName().equals("scope2"))
                .findFirst();

        assertTrue(deletedClientScope.isEmpty());
    }

    private ClientScopeConfig createClientScopeConfig(String name) {
        ClientScopeConfig config = new ClientScopeConfig();
        config.setName(name);
        config.normalize();
        return config;
    }

    private ClientScopeConfig createClientScopeConfig(String name, String mapperName) {
        ClientScopeConfig config = new ClientScopeConfig();
        config.setName(name);
        ProtocolMapperConfig protocolMapperConfig = createProtocolMapper(mapperName);
        config.setProtocolMappers(Collections.singletonList(protocolMapperConfig));
        config.normalize();
        return config;
    }

    private ProtocolMapperConfig createProtocolMapper(String name) {
        ProtocolMapperConfig mapper = new ProtocolMapperConfig();
        mapper.setName(name);
        mapper.setProtocol("openid-connect");
        mapper.setProtocolMapper("oidc-hardcoded-role-mapper");
        mapper.normalize();
        return mapper;
    }
}
