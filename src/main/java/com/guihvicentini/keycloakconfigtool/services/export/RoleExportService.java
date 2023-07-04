package com.guihvicentini.keycloakconfigtool.services.export;

import com.guihvicentini.keycloakconfigtool.adapters.ClientResourceAdapter;
import com.guihvicentini.keycloakconfigtool.adapters.RoleResourceAdapter;
import com.guihvicentini.keycloakconfigtool.mappers.RolesConfigMapper;
import com.guihvicentini.keycloakconfigtool.models.RolesConfig;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.RolesRepresentation;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
public class RoleExportService {

    private final RoleResourceAdapter resourceAdapter;

    private final ClientResourceAdapter clientResourceAdapter;
    private final RolesConfigMapper configMapper;


    public RoleExportService(RoleResourceAdapter resourceAdapter,
                             ClientResourceAdapter clientResourceAdapter,
                             RolesConfigMapper configMapper) {
        this.resourceAdapter = resourceAdapter;
        this.clientResourceAdapter = clientResourceAdapter;
        this.configMapper = configMapper;
    }


    public RolesConfig getRealmAndClientRoles(String realm) {
        RolesRepresentation representation = new RolesRepresentation();
        representation.setRealm(getRealmRoles(realm));
        representation.setClient(getClientRoles(realm));
        RolesConfig config = configMapper.mapToConfig(representation);
        config.normalize(realm);
        return config;
    }


    private Map<String, List<RoleRepresentation>> getClientRoles(String realm) {
        var clientRoles = resourceAdapter.getAllClientRoles(realm);
        clientRoles.forEach((clientId, roles) -> {
            roles.forEach(role -> {
                role.setComposites(new RoleRepresentation.Composites());
                setRealmComposites(realm, clientId, role);
                setClientComposites(realm, clientId, role);
            });
        });

        return clientRoles;
    }

    private void setRealmComposites(String realm, String clientId, RoleRepresentation role) {
        String clientUuid = clientResourceAdapter.getClientByClientId(realm, clientId).getId();
        role.getComposites().setRealm(resourceAdapter.getClientRoleRealmComposites(realm, clientUuid, role.getName())
                .stream().map(RoleRepresentation::getName)
                .collect(Collectors.toSet()));
    }

    private void setClientComposites(String realm, String clientId, RoleRepresentation role) {

        String clientUuid = clientResourceAdapter.getClientByClientId(realm, clientId).getId();

        if(role.getComposites().getClient() == null) {
            role.getComposites().setClient(new HashMap<>());
        }

        clientResourceAdapter.getClients(realm).forEach(client -> {
            Map<String, List<String>> composites = Collections.singletonMap(client.getClientId(),
                    resourceAdapter.getClientRoleClientComposites(realm, clientUuid, role.getName(), client.getClientId())
                            .stream()
                            .map(RoleRepresentation::getName)
                            .collect(Collectors.toList()));

            composites.forEach((key, value) -> {
                if(!value.isEmpty()) {
                    role.getComposites().getClient().put(key, value);
                }
            });
        });

    }


    private List<RoleRepresentation> getRealmRoles(String realm) {
        return resourceAdapter.getRealmRoles(realm).stream()
                .peek(role -> {
                    role.setComposites(new RoleRepresentation.Composites());
                    role.getComposites().setRealm(resourceAdapter.getRoleRealmComposites(realm, role.getName()));
                    role.getComposites().setClient(getRealmRoleClientComposites(realm, role.getName()));
                })
                .collect(Collectors.toList());
    }

    public Map<String, List<String>> getRealmRoleClientComposites(String realm, String roleName) {
        return clientResourceAdapter.getClients(realm).stream()
                .flatMap(client -> {
                    List<String> clientCompositesName = resourceAdapter.getRoleClientComposites(realm, roleName, client.getId())
                            .stream().map(RoleRepresentation::getName)
                            .collect(Collectors.toList());

                    return clientCompositesName.isEmpty()
                            ? Stream.empty()
                            : Stream.of(new AbstractMap.SimpleEntry<>(client.getClientId(), clientCompositesName));
                })
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

}
