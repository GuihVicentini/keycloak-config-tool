package com.guihvicentini.keycloakconfigtool.services.export;

import com.guihvicentini.keycloakconfigtool.adapters.ClientScopeResourceAdapter;
import com.guihvicentini.keycloakconfigtool.mappers.ClientScopeConfigMapper;
import com.guihvicentini.keycloakconfigtool.models.ClientScopeConfig;
import org.keycloak.representations.idm.ProtocolMapperRepresentation;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ClientScopeExportService {

    private final ClientScopeResourceAdapter resourceAdapter;
    private final ClientScopeConfigMapper clientScopeConfigMapper;

    public ClientScopeExportService(ClientScopeResourceAdapter resourceAdapter,
                                    ClientScopeConfigMapper clientScopeConfigMapper) {
        this.resourceAdapter = resourceAdapter;
        this.clientScopeConfigMapper = clientScopeConfigMapper;
    }

    public List<ClientScopeConfig> getAllClientScopes(String realm) {
        return resourceAdapter.getAllClientScopes(realm)
                .stream()
                .peek(clientScope -> {
                    List<ProtocolMapperRepresentation> mappers = resourceAdapter.getAllProtocolMappers(realm, clientScope.getName());
                    clientScope.setProtocolMappers(mappers);
                })
                .map(clientScopeConfigMapper::mapToConfig)
                .collect(Collectors.toList());
    }
}
