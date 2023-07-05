package com.guihvicentini.keycloakconfigtool.services;

import com.guihvicentini.keycloakconfigtool.containers.AbstractIntegrationTest;
import com.guihvicentini.keycloakconfigtool.models.ClientConfig;
import com.guihvicentini.keycloakconfigtool.models.ProtocolMapperConfig;
import com.guihvicentini.keycloakconfigtool.services.export.ClientExportService;
import com.guihvicentini.keycloakconfigtool.utils.JsonMapperUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
public class ClientImportServiceTest extends AbstractIntegrationTest {

    public static final String BROWSER = "browser";
    @Autowired
    ClientImportService importService;

    @Autowired
    ClientExportService exportService;

    @Test
    @Order(1)
    public void getAllClients() {
        List<ClientConfig> clients = exportService.getAllClients(TEST_REALM);
        log.debug("Clients: {}", JsonMapperUtils.objectToJsonPrettyString(clients));
        var maybeTestClient = clients.stream()
                .filter(c -> c.getClientId().equals("test-client")).findFirst();

        assertTrue(maybeTestClient.isPresent());
        var testClient = maybeTestClient.get();

        assertFalse(testClient.getProtocolMappers().isEmpty());
        assertFalse(testClient.getAuthenticationFlowBindingOverrides().isEmpty());
        assertTrue(testClient.getAuthenticationFlowBindingOverrides().containsKey(BROWSER));
        var flowOverriding = testClient.getAuthenticationFlowBindingOverrides().get(BROWSER);
        assertFalse(flowOverriding.matches(UUID_MATCH));
    }

    @Test
    @Order(2)
    public void testDoImport_ClientExistsInTargetButNotInActual_CreateClient() {
        List<ClientConfig> actual = List.of();
        List<ClientConfig> target = List.of(createClient("client1"));
        ProtocolMapperConfig protocolMapper = createProtocolMapper("mapper1");

        target.get(0).getProtocolMappers().add(protocolMapper);

        importService.doImport(TEST_REALM, actual, target);

        List<ClientConfig> importedClients = exportService.getAllClients(TEST_REALM);
        ClientConfig client1 = getClientConfigByClientId(importedClients, "client1");

        assertNotNull(client1);
        assertTrue(client1.getProtocolMappers().contains(protocolMapper));
    }

    @Test
    @Order(3)
    public void testDoImport_ClientExistsInTargetAndInActual_UpdateClient() {

        List<ClientConfig> actual = List.of(createClient("client1"));
        List<ClientConfig> target = List.of(createClient("client1"));

        ProtocolMapperConfig protocolMapper = createProtocolMapper("mapper1");

        actual.get(0).getProtocolMappers().add(protocolMapper);

        protocolMapper.getConfig().put("access.token.claim", "false");
        target.get(0).getProtocolMappers().add(protocolMapper);
        target.get(0).getAuthenticationFlowBindingOverrides().put(BROWSER, BROWSER);

        importService.doImport(TEST_REALM, actual, target);

        List<ClientConfig> importedClients = exportService.getAllClients(TEST_REALM);

        ClientConfig updatedClient1 = getClientConfigByClientId(importedClients, "client1");

        assertNotNull(updatedClient1);
        assertEquals(target.get(0).getAuthenticationFlowBindingOverrides(), updatedClient1.getAuthenticationFlowBindingOverrides());
        assertTrue(updatedClient1.getAuthenticationFlowBindingOverrides().containsKey(BROWSER));
        assertEquals(BROWSER, updatedClient1.getAuthenticationFlowBindingOverrides().get(BROWSER));
        Optional<ProtocolMapperConfig> actualMapper = updatedClient1.getProtocolMappers()
                .stream().filter(m -> m.identifier().equals(m.identifier()))
                .findFirst();

        assertTrue(actualMapper.isPresent());
        assertEquals(protocolMapper, actualMapper.get());
    }

    @Test
    @Order(4)
    public void testDoImport_ClientExistsInActualButNotInTarget_DeleteClient() {

        List<ClientConfig> actual = List.of(createClient("client1"));
        List<ClientConfig> target = List.of();

        // Act
        importService.doImport(TEST_REALM, actual, target);

        // Assert
        List<ClientConfig> importedClients = exportService.getAllClients(TEST_REALM);

        assertNull(getClientConfigByClientId(importedClients, "client1"));
    }

    private ClientConfig getClientConfigByClientId(List<ClientConfig> clients, String clientId) {
        return clients.stream()
                .filter(client -> client.getClientId().equals(clientId))
                .findFirst()
                .orElse(null);
    }

    private ClientConfig createClient(String clientId) {
        ClientConfig config = new ClientConfig();
        config.setClientId(clientId);
        config.setProtocol("openid-connect");
        config.setClientAuthenticatorType("client-secret");
        config.normalize();

        return config;
    }

    private ProtocolMapperConfig createProtocolMapper(String name) {
        ProtocolMapperConfig protocolMapper = new ProtocolMapperConfig();
        protocolMapper.setName(name);
        protocolMapper.setProtocol("openid-connect");
        protocolMapper.setProtocolMapper("oidc-usermodel-property-mapper");

        Map<String, String> config = new HashMap<>();
        config.put("access.token.claim", "true");
        config.put("claim.name", "email");
        config.put("id.token.claim", "true");
        config.put("jsonType.label", "String");
        config.put("user.attribute", "email");
        config.put("userinfo.token.claim", "true");

        protocolMapper.setConfig(config);

        return protocolMapper;
    }

}
