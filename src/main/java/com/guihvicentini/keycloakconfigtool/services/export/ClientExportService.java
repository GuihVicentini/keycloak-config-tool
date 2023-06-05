package com.guihvicentini.keycloakconfigtool.services.export;

import com.guihvicentini.keycloakconfigtool.adapters.ClientResourceAdapter;
import com.guihvicentini.keycloakconfigtool.mappers.ClientConfigMapper;
import com.guihvicentini.keycloakconfigtool.models.ClientConfig;
import org.keycloak.representations.idm.ClientRepresentation;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ClientExportService {

    private final ClientResourceAdapter resourceAdapter;
    private final AuthenticationFlowExportService authenticationFlowExportService;

    private final ClientConfigMapper configMapper;

    public ClientExportService(ClientResourceAdapter resourceAdapter,
                               AuthenticationFlowExportService authenticationFlowExportService,
                               ClientConfigMapper configMapper) {
        this.resourceAdapter = resourceAdapter;
        this.authenticationFlowExportService = authenticationFlowExportService;
        this.configMapper = configMapper;
    }

    public List<ClientConfig> getAllClients(String realm) {
        return resourceAdapter.getClients(realm).stream()
                .peek(client -> replaceFlowUuidWithFlowAlias(realm, client))
                .map(configMapper::mapToConfig)
                .collect(Collectors.toList());
    }

    private void replaceFlowUuidWithFlowAlias(String realm, ClientRepresentation clientRepresentation) {
        clientRepresentation.getAuthenticationFlowBindingOverrides().forEach((key, value) ->
                replaceFlowUuidWithFlowAlias(realm, key, value, clientRepresentation.getAuthenticationFlowBindingOverrides()));
    }

    private void replaceFlowUuidWithFlowAlias(String realm, String key, String flowUuid, Map<String, String> authenticationFlowBindingOverrides) {
        String flowAlias = authenticationFlowExportService.getFlowAliasById(realm, flowUuid);
        authenticationFlowBindingOverrides.put(key, flowAlias);
    }
}
