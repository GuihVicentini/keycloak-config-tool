package com.guihvicentini.keycloakconfigtool.services;

import com.guihvicentini.keycloakconfigtool.adapters.ClientScopeResourceAdapter;
import com.guihvicentini.keycloakconfigtool.mappers.ClientScopeConfigMapper;
import com.guihvicentini.keycloakconfigtool.mappers.ProtocolMapperConfigMapper;
import com.guihvicentini.keycloakconfigtool.models.ClientScopeConfig;
import com.guihvicentini.keycloakconfigtool.models.ConfigConstants;
import com.guihvicentini.keycloakconfigtool.models.ProtocolMapperConfig;
import com.guihvicentini.keycloakconfigtool.utils.ListUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class ClientScopeImportService {

    private final ClientScopeResourceAdapter resourceAdapter;
    private final ClientScopeConfigMapper clientScopeConfigMapper;
    private final ProtocolMapperConfigMapper protocolMapperConfigMapper;


    public ClientScopeImportService(ClientScopeResourceAdapter resourceAdapter,
                                    ClientScopeConfigMapper clientScopeConfigMapper,
                                    ProtocolMapperConfigMapper protocolMapperConfigMapper) {

        this.resourceAdapter = resourceAdapter;
        this.clientScopeConfigMapper = clientScopeConfigMapper;
        this.protocolMapperConfigMapper = protocolMapperConfigMapper;
    }


    public void doImport(String realm, List<ClientScopeConfig> actual, List<ClientScopeConfig> target) {

        if(target.equals(actual)) {
            log.info(ConfigConstants.UP_TO_DATE_MESSAGE);
            return;
        }
        importClientScopes(realm, actual, target);
    }

    private void importClientScopes(String realm, List<ClientScopeConfig> actual, List<ClientScopeConfig> target) {

        List<ClientScopeConfig> toBeAdded = ListUtil.getMissingConfigElements(target, actual);
        List<ClientScopeConfig> toBeDeleted = ListUtil.getMissingConfigElements(actual, target);
        List<ClientScopeConfig> toBeUpdated = ListUtil.getNonEqualConfigsWithSameIdentifier(target, actual);

        addClientScopes(realm, toBeAdded);
        deleteClientScopes(realm, toBeDeleted);
        updateClientScopes(realm, toBeUpdated);
    }

    private void updateClientScopes(String realm, List<ClientScopeConfig> clientScopes) {
        clientScopes.forEach(clientScope -> updateClientScope(realm, clientScope));
    }

    private void updateClientScope(String realm, ClientScopeConfig clientScope) {
        log.debug("Updating clientScope: {}", clientScope.getName());
        resourceAdapter.update(realm, clientScopeConfigMapper.mapToRepresentation(clientScope));
        importProtocolMappers(realm, clientScope.getName(), clientScope.getProtocolMappers());
    }


    private void deleteClientScopes(String realm, List<ClientScopeConfig> clientScopes) {
        clientScopes.forEach(clientScope -> deleteClientScope(realm, clientScope));
    }

    private void deleteClientScope(String realm, ClientScopeConfig clientScope) {
        log.debug("Deleting clientScope: {}", clientScope.getName());
        // mappers should be deleted when clientScope is deleted
        resourceAdapter.delete(realm, clientScope.getName());
    }

    private void addClientScopes(String realm, List<ClientScopeConfig> clientScopes) {
        clientScopes.forEach(clientScope -> addClientScope(realm, clientScope));
    }

    private void addClientScope(String realm, ClientScopeConfig clientScope) {
        log.debug("Creating clientScope: {}", clientScope.getName());
        String id = resourceAdapter.create(realm, clientScopeConfigMapper.mapToRepresentation(clientScope));
        log.debug("Created clientScope: {} with id: {}",clientScope.getName(), id);
        importProtocolMappers(realm, clientScope.getName(), clientScope.getProtocolMappers());
    }

    private void importProtocolMappers(String realm, String clientScopeName, List<ProtocolMapperConfig> target) {

        List<ProtocolMapperConfig> actual = protocolMapperConfigMapper.mapToConfigList(
                resourceAdapter.getAllProtocolMappers(realm, clientScopeName));

        List<ProtocolMapperConfig> toBeAdded = ListUtil.getMissingConfigElements(target, actual);
        List<ProtocolMapperConfig> toBeDeleted = ListUtil.getMissingConfigElements(actual, target);
        List<ProtocolMapperConfig> toBeUpdated = ListUtil.getNonEqualConfigsWithSameIdentifier(target, actual);

        addProtocolMappers(realm, clientScopeName, toBeAdded);
        deleteProtocolMappers(realm, clientScopeName, toBeDeleted);
        updateProtocolMappers(realm, clientScopeName, toBeUpdated);
    }

    private void updateProtocolMappers(String realm, String name, List<ProtocolMapperConfig> protocolMappers) {
        protocolMappers.forEach(mapper -> updateProtocolMapper(realm, name, mapper));
    }

    private void updateProtocolMapper(String realm, String name, ProtocolMapperConfig mapper) {
        log.debug("Updating protocolMapper: {}", mapper.getName());
        resourceAdapter.updateProtocolMapper(realm, name, protocolMapperConfigMapper.mapToRepresentation(mapper));
    }

    private void addProtocolMappers(String realm, String clientScopeName, List<ProtocolMapperConfig> protocolMappers) {
        protocolMappers.forEach(mapper -> addProtocolMapper(realm, clientScopeName, mapper));
    }

    private void addProtocolMapper(String realm, String clientScopeName, ProtocolMapperConfig mapper) {
        log.debug("Creating protocolMapper: {}", mapper.getName());
        resourceAdapter.createProtocolMapper(realm, clientScopeName, protocolMapperConfigMapper.mapToRepresentation(mapper));
    }

    private void deleteProtocolMappers(String realm, String clientScopeName, List<ProtocolMapperConfig> protocolMappers) {
        protocolMappers.forEach(mapper -> deleteProtocolMapper(realm, clientScopeName, mapper));
    }

    private void deleteProtocolMapper(String realm, String clientScopeName, ProtocolMapperConfig mapper) {
        log.debug("Deleting protocolMapper: {}", mapper.getName());
        resourceAdapter.deleteProtocolMapper(realm, clientScopeName, mapper.getName());
    }
}
