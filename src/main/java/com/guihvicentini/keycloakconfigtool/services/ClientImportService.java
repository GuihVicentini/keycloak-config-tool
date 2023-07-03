package com.guihvicentini.keycloakconfigtool.services;

import com.guihvicentini.keycloakconfigtool.adapters.ClientResourceAdapter;
import com.guihvicentini.keycloakconfigtool.mappers.ClientConfigMapper;
import com.guihvicentini.keycloakconfigtool.mappers.ProtocolMapperConfigMapper;
import com.guihvicentini.keycloakconfigtool.models.ClientConfig;
import com.guihvicentini.keycloakconfigtool.models.ConfigConstants;
import com.guihvicentini.keycloakconfigtool.services.export.AuthenticationFlowExportService;
import com.guihvicentini.keycloakconfigtool.utils.ListUtil;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.representations.idm.ClientRepresentation;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ClientImportService {


    private final ClientResourceAdapter resourceAdapter;
    private final ClientConfigMapper clientConfigMapper;
    private final ProtocolMapperConfigMapper protocolMapperConfigMapper;
    private final AuthenticationFlowExportService authenticationFlowExportService;


    public ClientImportService(ClientResourceAdapter resourceAdapter,
                               ClientConfigMapper clientConfigMapper,
                               ProtocolMapperConfigMapper protocolMapperConfigMapper,
                               AuthenticationFlowExportService authenticationFlowExportService) {

        this.resourceAdapter = resourceAdapter;
        this.clientConfigMapper = clientConfigMapper;
        this.protocolMapperConfigMapper = protocolMapperConfigMapper;
        this.authenticationFlowExportService = authenticationFlowExportService;
    }

    public void doImport(String realm, List<ClientConfig> actual, List<ClientConfig> target) {

        masterRealmSafetyCheck(realm, actual, target);
        if(target.equals(actual)) {
            log.debug(ConfigConstants.UP_TO_DATE_MESSAGE);
            return;
        }

        importClients(realm, actual, target);
    }

    private void masterRealmSafetyCheck(String realm, List<ClientConfig> actual, List<ClientConfig> target) {
        if(ConfigConstants.MASTER_REALM_NAME.equals(realm)) {
            log.debug("Skip safety check for realm: {}", realm);
            return;
        }
        Set<String> actualRealmClients = filterRealmOutOfClients(actual);
        Set<String> targetRealmClients = filterRealmOutOfClients(target);

        if(!targetRealmClients.equals(actualRealmClients)) {
            throw new IllegalStateException(String.format(
                    "Due to safety reasons the master realm clients will not be changed." +
                    "The target configuration contains a different set of realms than the actual one."+
                    "Adjust the list of realms manually if you want to proceed with this configuration" +
                    "This configuration can be applied only if the following realms (and only those) exists: %s" +
                    "instead of %s",
                    String.join(", ", targetRealmClients),
                    String.join(", ", actualRealmClients)
            ));
        }

    }

    private Set<String> filterRealmOutOfClients(List<ClientConfig> clients) {
        return clients.stream().filter(ClientConfig::hasRealmSuffix)
                .map(ClientConfig::clientIdWithoutRealmSuffix)
                .collect(Collectors.toSet());
    }

    private void importClients(String realm, List<ClientConfig> actual, List<ClientConfig> target) {

        List<ClientConfig> toBeAdded = ListUtil.getMissingConfigElements(target, actual);
        List<ClientConfig> toBeDeleted = ListUtil.getMissingConfigElements(actual, target);
        List<ClientConfig> toBeUpdated = ListUtil.getNonEqualConfigsWithSameIdentifier(target, actual);

        addClients(realm, toBeAdded);
        deleteClients(realm, toBeDeleted);
        updateClients(realm, toBeUpdated);
    }

    private void updateClients(String realm, List<ClientConfig> clients) {
        clients.forEach(client -> updateClient(realm, client));
    }

    private void updateClient(String realm, ClientConfig client) {
        log.debug("Updating client: {}", client.getClientId());
        ClientRepresentation representation = clientConfigMapper.mapToRepresentation(client);
        replaceFlowAliasWithFlowUuid(realm, representation);
        resourceAdapter.update(realm, representation);
    }

    private void deleteClients(String realm, List<ClientConfig> clients) {
        clients.forEach(client -> deleteClient(realm, client));
    }

    private void deleteClient(String realm, ClientConfig client) {
        log.debug("Deleting client: {}", client.getClientId());
        // should delete also the protocol mappers
        resourceAdapter.delete(realm, client.getClientId());
    }

    private void addClients(String realm, List<ClientConfig> clients) {
        clients.forEach(client -> addClient(realm, client));
    }

    private void addClient(String realm, ClientConfig client) {
        log.debug("Creating client: {}", client.getClientId());
        ClientRepresentation representation = clientConfigMapper.mapToRepresentation(client);
        replaceFlowAliasWithFlowUuid(realm, representation);
        resourceAdapter.create(realm, representation);
        addDefaultClientScopes(realm, client.getClientId() ,client.getDefaultClientScopes());
        addOptionalClientScopes(realm, client.getClientId() ,client.getOptionalClientScopes());
    }

    private void addDefaultClientScopes(String realm, String clientId ,List<String> defaultClientScopes) {
        defaultClientScopes.forEach(scope -> addDefaultClientScope(realm, clientId, scope));
    }

    private void addDefaultClientScope(String realm, String clientId, String scope) {
        resourceAdapter.addDefaultClientScope(realm, clientId, scope);
    }

    private void addOptionalClientScopes(String realm, String clientId ,List<String> optionalClientScopes) {
        optionalClientScopes.forEach(scope -> addOptionalClientScope(realm, clientId, scope));
    }
    private void addOptionalClientScope(String realm, String clientId, String scope) {
        resourceAdapter.addOptionalClientScope(realm, clientId, scope);
    }

//    private void importProtocolMappers(String realm, String clientId, List<ProtocolMapperConfig> target) {
//
//        List<ProtocolMapperConfig> actual = protocolMapperConfigMapper.mapToConfigList(
//                resourceAdapter.getAllProtocolMappers(realm, clientId));
//
//        List<ProtocolMapperConfig> toBeAdded = ListUtil.getMissingConfigElements(target, actual);
//        List<ProtocolMapperConfig> toBeDeleted = ListUtil.getMissingConfigElements(actual, target);
//        List<ProtocolMapperConfig> toBeUpdated = ListUtil.getNonEqualConfigsWithSameIdentifier(target, actual);
//
//        addProtocolMappers(realm, clientId, toBeAdded);
//        deleteProtocolMappers(realm, clientId, toBeDeleted);
//        updateProtocolMappers(realm, clientId, toBeUpdated);
//    }

//    private void updateProtocolMappers(String realm, String clientId, List<ProtocolMapperConfig> protocolMappers) {
//        protocolMappers.forEach(mapper -> updateProtocolMapper(realm, clientId, mapper));
//    }
//
//    private void updateProtocolMapper(String realm, String clientId, ProtocolMapperConfig mapper) {
//        log.debug("Updating protocolMapper: {}", mapper.getName());
//        resourceAdapter.updateProtocolMapper(realm, clientId, protocolMapperConfigMapper.mapToRepresentation(mapper));
//    }
//
//    private void addProtocolMappers(String realm, String clientId, List<ProtocolMapperConfig> protocolMappers) {
//        protocolMappers.forEach(mapper -> addProtocolMapper(realm, clientId, mapper));
//    }
//
//    private void addProtocolMapper(String realm, String clientId, ProtocolMapperConfig mapper) {
//        log.debug("Creating protocolMapper: {}", mapper.getName());
//        resourceAdapter.createProtocolMapper(realm, clientId, protocolMapperConfigMapper.mapToRepresentation(mapper));
//    }
//
//    private void deleteProtocolMappers(String realm, String clientId, List<ProtocolMapperConfig> protocolMappers) {
//        protocolMappers.forEach(mapper -> deleteProtocolMapper(realm, clientId, mapper));
//    }
//
//    private void deleteProtocolMapper(String realm, String clientId, ProtocolMapperConfig mapper) {
//        log.debug("Deleting protocolMapper: {}", mapper.getName());
//        resourceAdapter.deleteProtocolMapper(realm, clientId, mapper.getName());
//    }

    private void replaceFlowAliasWithFlowUuid(String realm, ClientRepresentation clientRepresentation) {
        clientRepresentation.getAuthenticationFlowBindingOverrides().forEach((key, value) ->
                replaceFlowAliasWithFlowUuid(realm, key, value, clientRepresentation.getAuthenticationFlowBindingOverrides()));
    }

    private void replaceFlowAliasWithFlowUuid(String realm, String key, String flowAlias, Map<String, String> authenticationFlowBindingOverrides) {
        String flowId = authenticationFlowExportService.getFlowIdByAlias(realm, flowAlias);
        authenticationFlowBindingOverrides.put(key, flowId);
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
